netty实战之使用netty构建一个socks proxy
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
        sb.bind(new InetSocketAddress(1030));
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

```java


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

```

`SocksInitRequestDecoder`用于对socks的请求进行解码。你可能会说，为什么没有SocksCmdRequest的解码？别急，netty的handler是可以动态添加的，这里我们先解码一个初始化的请求。SocksInitRequestDecoder是一个`ChannelUpstreamHandler`，即接收流的处理器。

SocksMessageEncoder显然是




  [1]: http://static.oschina.net/uploads/space/2013/1016/161647_wYsq_190591.png
  
  