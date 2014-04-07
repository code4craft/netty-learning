/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.dianping.dproxy.socks;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.ClientSocketChannelFactory;
import org.jboss.netty.handler.codec.socks.*;

public final class SocksServerHandler extends SimpleChannelUpstreamHandler {
	private static final String name = "SOCKS_SERVER_HANDLER";

	public static String getName() {
		return name;
	}

    private final ClientSocketChannelFactory cf;

    public SocksServerHandler(ClientSocketChannelFactory cf) {
        this.cf = cf;
    }

    @Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        SocksRequest socksRequest = (SocksRequest) e.getMessage();
        switch (socksRequest.getSocksRequestType()) {
		case INIT:
            //添加cmd解码器
            ctx.getPipeline().addFirst(SocksCmdRequestDecoder.getName(), new SocksCmdRequestDecoder());
            //简单起见，无需认证
            ctx.getChannel().write(new SocksInitResponse(SocksMessage.AuthScheme.NO_AUTH));
            break;
		case AUTH:
            ctx.getPipeline().addFirst(SocksCmdRequestDecoder.getName(), new SocksCmdRequestDecoder());
            //直接成功
            ctx.getChannel().write(new SocksAuthResponse(SocksMessage.AuthStatus.SUCCESS));
            break;
		case CMD:
            SocksCmdRequest req = (SocksCmdRequest) socksRequest;
            if (req.getCmdType() == SocksMessage.CmdType.CONNECT) {
                //添加处理连接的handler
                ctx.getPipeline().addLast(SocksServerConnectHandler.getName(), new SocksServerConnectHandler(cf));
                ctx.getPipeline().remove(this);
            } else {
                ctx.getChannel().close();
            }
            break;
		case UNKNOWN:
            break;
		}
		super.messageReceived(ctx, e);
	}

}
