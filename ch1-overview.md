netty那点事-概述
-----

一直都听说netty或者mina很牛逼，是Java世界通讯框架的倚天剑屠龙刀，近年来大有一统江湖的趋势。netty和mina两个都出自一个大师之手，Mina诞生略早，早已是武林正统apache的门客，而netty开始在另一大帮派-Jboss门下，后来出来自立门户netty.io。关于mina已有@FrankHui的[Mina系列文章](http://my.oschina.net/ielts0909/blog/92716)，我就斗胆来写一份netty攻略，来分享给各位江湖猿友了。

学习netty，必须先修得Java内功，并发和NIO两门内功自然是必不可少的，不然大侠还是重新来过吧。如果还有一些TCP/IP的修为，那是再好不过了。

netty目前有两个分支，4.x和3.x。4.0分支重写了很多东西，并对项目进行了分包，规模比较庞大，入手会困难一些，而3.x版本则已经被广泛使用。3.x和4.0的区别可以参考这篇文章：[http://www.oschina.net/translate/netty-4-0-new-and-noteworthy?print](http://www.oschina.net/translate/netty-4-0-new-and-noteworthy?print)。本系列文章针对netty 3.7.0 final。

## 起：netty是什么

大概用netty的，无论新手还是高手，都知道它是一个“异步通讯框架”。所谓框架，基本上都是一个作用：基于底层API，提供更便捷的编程模型。那么"通讯框架"到底做了什么事情呢？回答这个问题并不太容易，我们不妨反过来看看，不使用netty，直接基于NIO编写网络程序，你需要做什么(以Server端TCP连接为例，这里我们使用Reactor模型)：

1. 监听端口，建立Socket连接
2. 建立线程，处理内容
	1. 读取Socket内容，并对协议进行解析
	2. 进行逻辑处理
	3. 回写响应内容
	4. 如果是多次交互的应用(SMTP、FTP)，则需要保持连接多进行几次交互
3. 关闭连接

建立线程是一个比较耗时的操作，幸好JDK已经有很方便的多线程框架了，这里我们不需要花很多心思。
	
此外，因为TCP连接的特性，我们还要使用连接池来进行管理。

1. 建立TCP连接是比较耗时的操作，对于频繁的通讯，保持连接效果更好
2. 对于并发请求，可能需要建立多个连接
3. 维护多个连接后，每次通讯，需要选择某一可用连接
4. 连接超时和关闭机制

想想就觉得很复杂了！实际上，基于NIO直接实现这部分东西，即使是老手也容易出现错误，而使用netty之后，你只需要关注逻辑处理部分就可以了。


## 承：netty的使用体验

以example里的EchoServer为例，其主要代码如下：

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

## 转：如何实现

## 合：开启netty源码之门
Java世界的框架大多追求大而全，如果逐个阅读，难免迷失方向，鄙人以为，抓住几个重点对象，理解其领域概念，从而理清其脉络，相当于打通了任督二脉，以后的阅读就不再困难了。等到

对代码的第一印象来看，netty的作者大概是“重复发明轮子”教的教主，netty有一半以上的类，都是跟JDK的概念直接对应，甚至连名字都不曾修改。例如NIO的两大组件：Buffer和Channel，netty里分别有两个包`io.netty.buffer`和`io.netty.channel`来对应；而并发框架的ExecutorService，netty也有类似的概念`EventExecutor`。

Channel

Buffer

EventExecutorGroup

ChannelPipeline

ChannelHandler
