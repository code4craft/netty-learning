层层分析Netty中的Channel
--------
Channel是理解和使用Netty的核心。之前在概述中粗略讲到了事件驱动机制，在这篇文章中，我们将详细分析Pipeline及其的实现。ChannelPipeline的涉及内容较多，这里我使用由浅入深的介绍方法。

## 一层梦境：Channel的基本机制

在Netty里，`Channel`是通讯的载体，而`ChannelHandler`负责Channel中的逻辑处理。

那么`ChannelPipeline`是什么呢？我觉得可以理解为ChannelHandler的容器：一个Channel包含一个ChannelPipeline，所有ChannelHandler都会注册到ChannelPipeline中，并按顺序组织起来。

在Netty中，`ChannelEvent`是数据或者状态的载体，例如传输的数据对应`MessageEvent`，状态的改变对应`ChannelStateEvent`。当对Channel进行操作时，会产生一个ChannelEvent，并发送到`ChannelPipeline`。ChannelPipeline会选择一个ChannelHandler进行处理。这个ChannelHandler处理之后，可能会产生新的ChannelEvent，并流转到下一个ChannelHandler。例如，一个数据最开始是一个`MessageEvent`，它附带了一个未解码的原始二进制消息`ChannelBuffer`，然后某个Handler将其解码成了一个数据对象，并生成了一个新的`MessageEvent`，并传递给下一步进行处理。

到了这里，可以看到，其实Channel的核心流程位于`ChannelPipeline`中。于是我们进入ChannelPipeline的深层梦境里，来看看它具体的实现。

![channel pipeline][1]

## 二层梦境：ChannelPipeline主流程的实现方式

Netty的ChannelPipeline包含两条线路：Upstream和Downstream。Upstream对应上行，接收到的消息、被动的状态改变，都属于Upstream。Downstream则对应下行，发送的消息、主动的状态改变，都属于Downstream。

对应的，ChannelPipeline里包含的ChannelHandler也包含两类：`ChannelUpstreamHandler`和`ChannelDownstreamHandler`。每条线路的handler是互相独立的。

Netty的`ChannelPipeline`接口的javadoc里有一张图，非常形象的说明这个机制(我对原图进行了一点修改，加上了`ChannelSink`，因为我觉得这部分对理解代码流程会有些帮助)：

![channel pipeline][2]

这里的Pipeline机制是这样的：

首先handler分为Upstream和Downstream两类。然后每个handler会接收到一个事件，如果需要继续处理，那么**它会发起一个事件**，这个事件只有它之后的handler会接收到。**如果它不再发起事件，那么处理就到此结束。**

理清了ChannelPipeline的主流程，我们对Channel部分的大致结构算是弄清楚了。可是到了这里，我们依然对一个连接具体怎么处理没有什么概念，下面我们从ChannelEvent开始来分析一下，具体Netty在连接的建立、数据的传输过程中，究竟做了什么事情。

## 三层梦境：Netty中的几种事件类型



## 四层梦境：几种事件对应的NIO模型




## 回到现实：Pipeline解决的问题





![universal API][3]

Pipeline这部分拖了两个月，终于写完了。中间写的实在缓慢，但是仍不忍心这部分就此烂尾。中间参考了一些优秀的文章，还自己使用netty开发了一些应用。以后这类文章，还是要集中时间来写完好了。

下一篇文章会详细分析一下Netty中已有的handler。

  [1]: http://static.oschina.net/uploads/space/2013/0921/174032_18rb_190591.png
  [2]: http://static.oschina.net/uploads/space/2013/1109/075339_Kjw6_190591.png
  [3]: http://static.oschina.net/uploads/space/2013/1108/234357_DeN0_190591.png
  [4]: http://static.oschina.net/uploads/space/2013/1108/234411_gvSE_190591.png
