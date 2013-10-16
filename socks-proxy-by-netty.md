【netty实战】使用netty构建一个socks proxy
---
最近在做的项目，需要自己搭建一个socks代理。netty4.0附带了一个socks代理的样例，但是3.x就没有这个东西了，碰巧使用的又是3.7，就只能自己摸索并实现一遍，也算是对netty和socks协议的一个熟悉。
socks代理涉及到协议解析、server、client等功能，是一个比较复杂的网络程序，对于学习netty的使用也是非常好的例子。

socks是在传输层之上的一层协议，主要功能是提供代理认证等功能。socks协议虽然是应用层协议(在TCP/IP4层协议栈里)，本身可以理解为一个信道，可以传输任何TCP/UDP内容。例如著名的科学上网软件就是基于socks协议，对通信内容进行加密实现的。

TCP/IP协议栈的结构中，下层协议总会在上层协议内容之前加上自己的头。而socks协议稍微不同，其实它对比TCP协议，仅仅是多了验证部分，验证之后，完全是使用TCP来进行传输，而没有socks报文头。socks协议的具体内容可以参考[rfc1928](http://www.ietf.org/rfc/rfc1928.txt)。这一点来说，其实将socks理解成与其他应用层协议平级也没什么问题。

一个最基本的socks连接流程是这样的：
![socks][1]

那么我们开始netty之旅吧。

首先我们需要建立一个server：

```java
    public void run() {

        // 新建线程池
        Executor executor = Executors.newCachedThreadPool();
        Executor executorWorker = Executors.newCachedThreadPool();
        ServerBootstrap sb = new ServerBootstrap(
                new NioServerSocketChannelFactory(executor, executorWorker));

        // 初始化代理部分使用的client
        ClientSocketChannelFactory cf =
                new NioClientSocketChannelFactory(executor, executorWorker);

        //设置处理逻辑
        sb.setPipelineFactory(
                new SocksProxyPipelineFactory(cf));

        // Start up the server.
        sb.bind(new InetSocketAddress(1080));
    }
```

如你所见，主要的处理逻辑以SocksProxyPipelineFactory的形式提供。SocksProxyPipelineFactory的代码包括几部分：

```java
public class SocksProxyPipelineFactory implements ChannelPipelineFactory {

	private final ClientSocketChannelFactory cf;

	public SocksProxyPipelineFactory(ClientSocketChannelFactory cf) {
		this.cf = cf;
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
        pipeline.addLast(SocksInitRequestDecoder.getName(),new SocksInitRequestDecoder());
        pipeline.addLast(SocksMessageEncoder.getName(),new SocksMessageEncoder());
        pipeline.addLast(SocksServerHandler.getName(),new SocksServerHandler(cf));
		return pipeline;
	}
}

```
这里要详细解释一下几个handler的作用：

`ChannelUpstreamHandler`用于接收之后的处理，而`ChannelDownstreamHandler`则相反，用于写入数据之后的处理。这两个都可以附加到`ChannelPipeline`中。偷个懒，直接附上netty的ChannelPipeline中的一段很有爱的javadoc：


                                          I/O Request
                                        via {@link Channel} or
                                    {@link ChannelHandlerContext}
                                              |
     +----------------------------------------+---------------+
     |                  ChannelPipeline       |               |
     |                                       \|/              |
     |  +----------------------+  +-----------+------------+  |
     |  | Upstream Handler  N  |  | Downstream Handler  1  |  |
     |  +----------+-----------+  +-----------+------------+  |
     |            /|\                         |               |
     |             |                         \|/              |
     |  +----------+-----------+  +-----------+------------+  |
     |  | Upstream Handler N-1 |  | Downstream Handler  2  |  |
     |  +----------+-----------+  +-----------+------------+  |
     |            /|\                         .               |
     |             .                          .               |
     |     [ sendUpstream() ]        [ sendDownstream() ]     |
     |     [ + INBOUND data ]        [ + OUTBOUND data  ]     |
     |             .                          .               |
     |             .                         \|/              |
     |  +----------+-----------+  +-----------+------------+  |
     |  | Upstream Handler  2  |  | Downstream Handler M-1 |  |
     |  +----------+-----------+  +-----------+------------+  |
     |            /|\                         |               |
     |             |                         \|/              |
     |  +----------+-----------+  +-----------+------------+  |
     |  | Upstream Handler  1  |  | Downstream Handler  M  |  |
     |  +----------+-----------+  +-----------+------------+  |
     |            /|\                         |               |
     +-------------+--------------------------+---------------+
                   |                         \|/
     +-------------+--------------------------+---------------+
     |             |                          |               |
     |     [ Socket.read() ]          [ Socket.write() ]      |
     |                                                        |
     |  Netty Internal I/O Threads (Transport Implementation) |
     +--------------------------------------------------------+


`SocksInitRequestDecoder`用于对socks的请求进行解码。你可能会说，为什么没有SocksCmdRequest的解码？别急，netty的handler是可以动态添加的，这里我们先解码一个初始化的请求。SocksInitRequestDecoder是一个`ChannelUpstreamHandler`，即接收流的处理器。

`SocksMessageEncoder`是一个`ChannelDownstreamHandler`，即输出时的编码器，有了它，我们可以很开心的在channel.write()里直接传入一个对象，而无需自己去写buffer了。

`SocksServerHandler`是处理的重头。这里会根据请求的不同类型，做不同的处理。

```java
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
```

前面两种INIT和AUTH就不做赘述了，后面当CMD为Connect时，添加一个处理连接的`SocksServerConnectHandler`，它会起到client与外部server的桥梁作用。

这里我们先实现一个纯转发的handler-`OutboundHandler`:

```java
    private class OutboundHandler extends SimpleChannelUpstreamHandler {

        private final Channel inboundChannel;

        OutboundHandler(Channel inboundChannel) {
            this.inboundChannel = inboundChannel;
        }

        @Override
        public void messageReceived(ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
            final ChannelBuffer msg = (ChannelBuffer) e.getMessage();
            synchronized (trafficLock) {
                inboundChannel.write(msg);

            }
        }
    }    

```
它会把收到的内容，写入到`inboundChannel`中，其他转发的作用。最后就是我们的`SocksServerConnectHandler`了:

```java
    public class SocksServerConnectHandler extends SimpleChannelUpstreamHandler {

        private final ClientSocketChannelFactory cf;

        private volatile Channel outboundChannel;

        final Object trafficLock = new Object();

        public SocksServerConnectHandler(ClientSocketChannelFactory cf) {
            this.cf = cf;
        }

        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
            final SocksCmdRequest socksCmdRequest = (SocksCmdRequest) e.getMessage();
            final Channel inboundChannel = e.getChannel();
            inboundChannel.setReadable(false);

            // Start the connection attempt.
            final ClientBootstrap cb = new ClientBootstrap(cf);
            cb.setOption("keepAlive", true);
            cb.setOption("tcpNoDelay", true);
            cb.setPipelineFactory(new ChannelPipelineFactory() {
                @Override
                public ChannelPipeline getPipeline() throws Exception {
                    ChannelPipeline pipeline = Channels.pipeline();
                    // 外部server数据转发到client
                    pipeline.addLast("outboundChannel", new OutboundHandler(inboundChannel, "out"));
                    return pipeline;
                }
            });

            ChannelFuture f = cb.connect(new InetSocketAddress(socksCmdRequest.getHost(), socksCmdRequest.getPort()));

            outboundChannel = f.getChannel();
            ctx.getPipeline().remove(getName());
            f.addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        // client数据转发到外部server
                        inboundChannel.getPipeline().addLast("inboundChannel", new OutboundHandler(outboundChannel, "in"));
                        inboundChannel.write(new SocksCmdResponse(SocksMessage.CmdStatus.SUCCESS, socksCmdRequest
                                .getAddressType()));
                        inboundChannel.setReadable(true);
                    } else {
                        inboundChannel.write(new SocksCmdResponse(SocksMessage.CmdStatus.FAILURE, socksCmdRequest
                                .getAddressType()));
                        inboundChannel.close();
                    }
                }
            });
        }
    }
```

好了，完工！输入`curl --socks5 127.0.0.1:1080 http://www.oschina.net/`测试一下吧？但是测试时发现，怎么老是无法接收到响应？

使用wiredshark抓包之后，发现对外请求完全正常，但是对客户端的响应，则完全没有http响应部分？

一步步debug下去，才发现`SocksMessageEncoder`出了问题！

```java
	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		ChannelBuffer buffer = null;
		if (msg instanceof SocksMessage) {
			buffer = ChannelBuffers.buffer(DEFAULT_ENCODER_BUFFER_SIZE);
			((SocksMessage) msg).encodeAsByteBuf(buffer);
		} 
		return buffer;
	}
```
这里只有SocksMessage才会被处理，其他的message全部被丢掉了！于是我们加上一行:

```java
	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		ChannelBuffer buffer = null;
		if (msg instanceof SocksMessage) {
			buffer = ChannelBuffers.buffer(DEFAULT_ENCODER_BUFFER_SIZE);
			((SocksMessage) msg).encodeAsByteBuf(buffer);
		} else if (msg instanceof ChannelBuffer) {
			//直接转发是ChannelBuffer类型
			buffer = (ChannelBuffer) msg;
		}
		return buffer;
	}
```

至此，一个代理完成！点这里查看代码：[https://github.com/code4craft/netty-learning/tree/master/learning-src/socksproxy](https://github.com/code4craft/netty-learning/tree/master/learning-src/socksproxy)

  [1]: http://static.oschina.net/uploads/space/2013/1016/174446_CK7D_190591.png
  
  