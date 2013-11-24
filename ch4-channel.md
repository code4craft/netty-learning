层层分析Netty中的Channel(下)
--------

这篇文章会走进Netty处理的内部。这里只介绍NIO部分。

## 第一层：Channel的类型及创建

我们在使用Netty时，总是需要指定一个`ChannelFactory`，这个就是

Bind: `NioServerSocketChannel`

Accept:`NioServerBoss`

Read:`NioWorker.read`

Write:`AbstractNioWorker`

## 第二层：Channel与底层IO的结合

## 第三层：从NioServerSocketChannel看NIO与Reactor模式

## 回到现实：几种与对应的实现

## NIO:

	Selector	->Boss
			  	->Worker


实际上Channel部分没有太多内容，

![channel in Netty][1]

>TODO

ServerChannel

SocketChannel

DatagramChannel

LocalChannel

----------

ChannelHandler

ChannelUpstreamHandler

ChannelDownstreamHandler

Config Parent & Child

通过handler把底层隔离了

Netty channel部分比较复杂，一时可能难以入手。我们先结合一些NIO Server的知识，从`NioServerSocketChannel`入手，讲讲一个基于NIO的服务器的流程。

[http://rdc.taobao.com/team/jm/archives/423](http://rdc.taobao.com/team/jm/archives/423)

[http://cqupt123.iteye.com/blog/1706902](http://cqupt123.iteye.com/blog/1706902)

[http://www.coderli.com/category/open-source/distributed/netty](http://www.coderli.com/category/open-source/distributed/netty)

twitter关于3.0与4.0中Channel Event的说明：
[https://blog.twitter.com/2013/netty-4-at-twitter-reduced-gc-overhead](https://blog.twitter.com/2013/netty-4-at-twitter-reduced-gc-overhead)

  [1]: http://static.oschina.net/uploads/space/2013/0929/174705_47Rr_190591.png
  
Channels部分事件流转静态方法
1．fireChannelOpen 2．fireChannelBound 3．fireChannelConnected 4．fireMessageReceived 5．fireWriteComplete 6．fireChannelInterestChanged
7．fireChannelDisconnected 8．fireChannelUnbound 9．fireChannelClosed 10.fireExceptionCaught 11.fireChildChannelStateChanged


http://en.wikipedia.org/wiki/Sink_(computing)