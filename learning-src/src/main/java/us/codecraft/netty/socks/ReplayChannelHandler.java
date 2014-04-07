package us.codecraft.netty.socks;

import org.jboss.netty.channel.*;

/**
 * @author code4crafter@gmail.com
 */
public class ReplayChannelHandler extends SimpleChannelUpstreamHandler{

    private Channel outgoingChannel;

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        outgoingChannel.write(e.getMessage());
        super.messageReceived(ctx, e);
    }

    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        super.channelOpen(ctx, e);
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        super.channelConnected(ctx, e);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        super.channelClosed(ctx, e);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
