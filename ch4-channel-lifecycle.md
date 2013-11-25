Channel的生命周期
--------

上篇文章讲到了Netty的Channel内部的运作机制，这篇文章详细分析Channel的生命周期。

## 一、Java中的Socket生命周期

Server:

Open=>Bind=>Accept

Client:

Open=>Connect


无论是NIO还是OIO，都是这个原理，NIO最大的改变是引入了Selector机制，来解决IO等待的问题。

## 二、Netty中Channel的生命周期

我们在使用Netty时，总是需要指定一个`ChannelFactory`，这个就是

Open:

Bind:

Accept:

Connect:

Read:

Write:

## 三、Reactor模式及Netty中的实现





那么在Netty中呢？实际上，Server部分的业务基本不涉及到逻辑，建立连接后


## 一、连接的创建

### 上层世界：Netty中Channel的类型及创建



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


![Multiple Reactors][1]

  [1]: http://static.oschina.net/uploads/space/2013/1125/130828_uKWD_190591.jpeg

参考资料：

* Scalable IO in Java [http://gee.cs.oswego.edu/dl/cpjslides/nio.pdf](http://gee.cs.oswego.edu/dl/cpjslides/nio.pdf)