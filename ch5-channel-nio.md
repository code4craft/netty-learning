层层分析Netty中的Channel(下)
--------

上篇文章讲到了Netty的Channel内部的运作机制和生命周期。这篇文章会走进Netty处理的内部，结合NIO，讲讲Netty中是如何实现Reactor模式的。

## 一、连接的创建

### 上层世界：Netty中Channel的类型及创建

我们在使用Netty时，总是需要指定一个`ChannelFactory`，这个就是

Bind: `NioServerSocketChannel`

Accept:`NioServerBoss`

Read:`NioWorker.read`

Write:`AbstractNioWorker`

### 下层世界：NIO中的

|NIO |Netty |
|-|-|
|SelectionKey.OP_READ | | 
|SelectionKey.OP_WRITE | | 
|SelectionKey.OP_CONNECT | | 
|SelectionKey.OP_ACCEPT | | 




## 二、Interest与Selector

## 三、服务器端的多线程

## 回到现实：几种与对应的实现

## NIO:

	Selector	->Boss
			  	->Worker


实际上Channel部分没有太多内容，

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

![Multiple Reactors][1]

  [1]: http://static.oschina.net/uploads/space/2013/1125/130828_uKWD_190591.jpeg

参考资料：

* Scalable IO in Java [http://gee.cs.oswego.edu/dl/cpjslides/nio.pdf](http://gee.cs.oswego.edu/dl/cpjslides/nio.pdf)