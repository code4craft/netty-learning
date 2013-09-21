Netty那点事-概述
-----

## 起：Netty是什么

大概用Netty的，无论新手还是高手，都知道它是一个“网络通讯框架”。所谓框架，基本上都是一个作用：基于底层API，提供更便捷的编程模型。那么"通讯框架"到底做了什么事情呢？回答这个问题并不太容易，我们不妨反过来看看，不使用netty，直接基于NIO编写网络程序，你需要做什么(以Server端TCP连接为例，这里我们使用Reactor模型)：

1. 监听端口，建立Socket连接
2. 建立线程，处理内容
	1. 读取Socket内容，并对协议进行解析
	2. 进行逻辑处理
	3. 回写响应内容
	4. 如果是多次交互的应用(SMTP、FTP)，则需要保持连接多进行几次交互
3. 关闭连接

建立线程是一个比较耗时的操作，同时维护线程本身也有一些开销，所以我们会需要多线程机制，幸好JDK已经有很方便的多线程框架了，这里我们不需要花很多心思。
	
此外，因为TCP连接的特性，我们还要使用连接池来进行管理：

1. 建立TCP连接是比较耗时的操作，对于频繁的通讯，保持连接效果更好
2. 对于并发请求，可能需要建立多个连接
3. 维护多个连接后，每次通讯，需要选择某一可用连接
4. 连接超时和关闭机制

想想就觉得很复杂了！实际上，基于NIO直接实现这部分东西，即使是老手也容易出现错误，而使用Netty之后，你只需要关注逻辑处理部分就可以了。


## 承：体验Netty的使用

这里我们引用Netty的example包里的一个例子，一个简单的EchoServer，它接受客户端输入，并将输入原样返回。其主要代码如下：

```java
    public void run() {
        // Configure the server.
        ServerBootstrap bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        // Set up the pipeline factory.
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(new EchoServerHandler());
            }
        });

        // Bind and start to accept incoming connections.
        bootstrap.bind(new InetSocketAddress(port));
    }
```

这里`EchoServerHandler`是其业务逻辑的实现者，大致代码如下：

```java
	public class EchoServerHandler extends SimpleChannelUpstreamHandler {

	    @Override
	    public void messageReceived(
	            ChannelHandlerContext ctx, MessageEvent e) {
	        // Send back the received message to the remote peer.
	        e.getChannel().write(e.getMessage());
	    }
	}
```
	
还是挺简单的，不是吗？

## 转：Netty的事件驱动

完成了以上一段代码，我们算是与Netty进行了第一次亲密接触。

Java世界的框架大多追求大而全，功能完备，如果逐个阅读，难免迷失方向。相反，抓住几个重点对象，理解其领域概念及设计思想，从而理清其脉络，相当于打通了任督二脉，以后的阅读就不再困难了。

Netty的一个设计思想就是事件驱动。什么叫事件驱动？我们回头看看`EchoServerHandler`的代码，其中的参数：`public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)`，MessageEvent就是一个事件。这个事件携带了一些信息，例如这里`e.getMessage()`就是消息的内容。

在Netty里，所有事件都来自`ChannelEvent`接口，这些事件涵盖监听端口、建立连接、读写数据等网络通讯的各个阶段。而事件的处理者就是`ChannelHandler`，这样，不但是业务逻辑，连网络通讯流程中底层的处理，都可以通过实现`ChannelHandler`来完成了。例如，最基本的监听连接就是通过`ServerBootstrap.Binder`来完成的，这个Binder则继承自`SimpleChannelUpstreamHandler`。

下图描述了Netty进行事件处理的流程。`Channel`是连接的通道，是ChannelEvent的产生者，而`ChannelPipeline`可以理解为ChannelHandler的集合。

![event driven in Netty][1]


## 合：开启Netty源码之门

前面已经讲到了netty的事件驱动机制，

Channel

Buffer

EventExecutorGroup

ChannelPipeline

ChannelHandler

参考资料：

* What is Netty? [http://ayedo.github.io/netty/2013/06/19/what-is-netty.html](http://ayedo.github.io/netty/2013/06/19/what-is-netty.html)

  [1]: http://static.oschina.net/uploads/space/2013/0921/174032_18rb_190591.png

