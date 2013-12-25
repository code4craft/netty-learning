Channel与NIO
--------

上篇文章讲到了Netty的Channel内部的运作机制，这篇文章详细分析Channel的生命周期，以及其与NIO的对应关系。

## 一、Socket的生命周期

在TCP/IP协议中，网络传输层分为TCP和UDP两种。UDP没有连接的概念，双方都是对等的，只存在send&receive，相对简单。对于TCP，乃至于所有有连接的传输层协议，都可以简单的概括为三个步骤：建立连接、传输、关闭连接。

在Java里，无论是OIO还是NIO，都是使用[Socket](http://en.wikipedia.org/wiki/Network_socket)编程方式。在OIO中有`ServerSocket`、`Socket`和`DatagramSocket`，前两个对应TCP，后一个对应UDP。在NIO中，我们改用了`ServerSocketChannel`、`SocketChannel`和`DatagramChannel`，并拥有了基于Selector的通知机制，但是Socket使用的方式可以说没有什么变化。

而对于TCP socket，则需要经过建立连接的过程。

在建立连接之前，存在两个角色：ServerSocketChannel和SocketChannel。

Client端的流程是这样：

open()=>connect()

Server端的流程是这样：

open()=>bind()=>(listen)=>accept()

建立连接之后，C/S双方都会拿到一个对等的`SocketChannel`，通过它可以进行`read()`/`write()`等数据交互，并可以使用`close()`关闭连接。

而Netty里的Channel，虽然对Java的API做了一层封装，但是仍然沿用了这些状态和概念。下面我们来看看其中具体的实现。

## 二、Netty中Channel的生命周期

我们在使用Netty时，总是需要指定一个`ChannelFactory`，这个就是Channel的入口。

Open:

Bind:

Accept:

Connect:

Read:

Write:

Netty中一切操作都通过ChannelEvent来体现。关于ChannelEvent的具体类型，官方的Javadoc已经介绍得很详细了：[http://netty.io/3.8/api/org/jboss/netty/channel/ChannelEvent.html](http://netty.io/3.8/api/org/jboss/netty/channel/ChannelEvent.html)

parent和child

Boss和Worker

Bind: `NioServerBoss`

Accept:`NioServerBoss.process`

Read:`NioWorker.read`

Write:`AbstractNioWorker`

## 三、Netty与NIO

|NIO |Netty |
|-|-|
|SelectionKey.OP_READ | | 
|SelectionKey.OP_WRITE | | 
|SelectionKey.OP_CONNECT | | 
|SelectionKey.OP_ACCEPT | | 


