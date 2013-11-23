层层分析Netty中的Channel(上)
--------
Channel是理解和使用Netty的核心。之前在概述中粗略讲到了事件驱动机制，在这篇文章中，我们将详细分析Channel及其的实现。ChannelPipeline的涉及内容较多，这里我使用由浅入深的介绍方法。为了避免枯燥，借用一下《盗梦空间》的“梦境”概念，希望大家喜欢。

## 一层梦境：Channel机制概览

在Netty里，`Channel`是通讯的载体，而`ChannelHandler`负责Channel中的逻辑处理。

那么`ChannelPipeline`是什么呢？我觉得可以理解为ChannelHandler的容器：一个Channel包含一个ChannelPipeline，所有ChannelHandler都会注册到ChannelPipeline中，并按顺序组织起来。

在Netty中，`ChannelEvent`是数据或者状态的载体，例如传输的数据对应`MessageEvent`，状态的改变对应`ChannelStateEvent`。当对Channel进行操作时，会产生一个ChannelEvent，并发送到`ChannelPipeline`。ChannelPipeline会选择一个ChannelHandler进行处理。这个ChannelHandler处理之后，可能会产生新的ChannelEvent，并流转到下一个ChannelHandler。

![channel pipeline][1]

例如，一个数据最开始是一个`MessageEvent`，它附带了一个未解码的原始二进制消息`ChannelBuffer`，然后某个Handler将其解码成了一个数据对象，并生成了一个新的`MessageEvent`，并传递给下一步进行处理。

到了这里，可以看到，其实Channel的核心流程位于`ChannelPipeline`中。于是我们进入ChannelPipeline的深层梦境里，来看看它具体的实现。

## 二层梦境：ChannelPipeline的主流程

Netty的ChannelPipeline包含两条线路：Upstream和Downstream。Upstream对应上行，接收到的消息、被动的状态改变，都属于Upstream。Downstream则对应下行，发送的消息、主动的状态改变，都属于Downstream。

对应的，ChannelPipeline里包含的ChannelHandler也包含两类：`ChannelUpstreamHandler`和`ChannelDownstreamHandler`。每条线路的Handler是互相独立的。

Netty官方的javadoc里有一张图(`ChannelPipeline`接口里)，非常形象的说明了这个机制(我对原图进行了一点修改，加上了`ChannelSink`，因为我觉得这部分对理解代码流程会有些帮助)：

![channel pipeline][2]

什么叫`ChannelSink`呢？ChannelSink包含一个重要方法`ChannelSink.eventSunk`，可以接受任意ChannelEvent。"sink"的意思是"下沉"，那么"ChannelSink"好像可以理解为"Channel下沉的地方"？实际上，它的作用确实是这样，也可以换个说法："处于末尾的万能Handler"。最初读到这里，也有些困惑，这么理解之后，就感觉简单许多。**只有Downstream包含`ChannelSink`**，这里会做一些建立连接、绑定端口等重要操作。为什么UploadStream没有ChannelSink呢？我只能认为，一方面，不符合"sink"的意义，另一方面，也没有什么处理好做的吧！

下面我们从代码层面来对这里面发生的事情进行深入分析，这部分涉及到一些细节，需要打开项目源码，对照来看，会比较有收获。

## 三层梦境：深入ChannelPipeline内部

### DefaultChannelPipeline的内部结构

`ChannelPipeline`的主要的实现代码在`DefaultChannelPipeline`类里。列一下DefaultChannelPipeline的主要字段：

```java
    public class DefaultChannelPipeline implements ChannelPipeline {
    
        private volatile Channel channel;
        private volatile ChannelSink sink;
        private volatile DefaultChannelHandlerContext head;
        private volatile DefaultChannelHandlerContext tail;
        private final Map<String, DefaultChannelHandlerContext> name2ctx =
            new HashMap<String, DefaultChannelHandlerContext>(4);
    }
```

这里需要介绍一下`ChannelHandlerContext`这个接口。顾名思义，ChannelHandlerContext保存了Netty与Handler相关的的上下文信息。而咱们这里的`DefaultChannelHandlerContext`，则是对`ChannelHandler`的一个包装。一个`DefaultChannelHandlerContext`内部，除了包含一个`ChannelHandler`，还保存了"next"和"prev"两个指针，从而形成一个双向链表。

因此，在`DefaultChannelPipeline`中，我们看到的是对`DefaultChannelHandlerContext`的引用，而不是对`ChannelHandler`的直接引用。这里包含"head"和"tail"两个引用，分别指向链表的头和尾。而name2ctx则是一个按名字索引DefaultChannelHandlerContext用户的一个map，主要在按照名称删除或者添加ChannelHandler时使用。

### sendUpstream和sendDownstream

`ChannelPipeline`接口包含了两个重要的方法：`sendUpstream(ChannelEvent e)`和`sendDownstream(ChannelEvent e)`，对应Upstream和Downstream。**所有事件**的发起都是基于这两个方法进行的。`Channels`类有一系列`fireChannelBound`之类的`fireXXXX`方法，其实都是对这两个方法的facade包装。

简单贴一下这两个方法的实现，来帮助理解(对代码做了一些简化，保留主逻辑)：

```java
    public void sendUpstream(ChannelEvent e) {
        DefaultChannelHandlerContext head = getActualUpstreamContext(this.head);
        head.getHandler().handleUpstream(head, e);
    }
    
    private DefaultChannelHandlerContext     getActualDownstreamContext(DefaultChannelHandlerContext ctx) {
        DefaultChannelHandlerContext realCtx = ctx;
        while (!realCtx.canHandleDownstream()) {
            realCtx = realCtx.prev;
            if (realCtx == null) {
                return null;
            }
        }
        return realCtx;
    }
```

例如这里用到的`ChannelHandlerContext.getHandler()`，就会获取当前应该使用哪个handler来进行处理。

这里的Pipeline机制是这样的：

首先handler分为Upstream和Downstream两类。然后每个handler会接收到一个事件，如果需要继续处理，那么**它会发起一个事件**，这个事件只有它之后的handler会接收到。**如果它不再发起事件，那么处理就到此结束。**

## 回到现实：Pipeline解决的问题

理清了ChannelPipeline的主流程，我们对Channel部分的大致结构算是弄清楚了。可是到了这里，我们依然对一个连接具体怎么处理没有什么概念，下面我们从ChannelEvent开始来分析一下，具体Netty在连接的建立、数据的传输过程中，究竟做了什么事情。

![universal API][3]

Pipeline这部分拖了两个月，终于写完了。中间写的实在缓慢，但是仍不忍心这部分就此烂尾。中间参考了一些优秀的文章，还自己使用netty开发了一些应用。以后这类文章，还是要集中时间来写完好了。

下一篇文章会详细分析一下Netty中已有的handler。

  [1]: http://static.oschina.net/uploads/space/2013/0921/174032_18rb_190591.png
  [2]: http://static.oschina.net/uploads/space/2013/1109/075339_Kjw6_190591.png
  [3]: http://static.oschina.net/uploads/space/2013/1108/234357_DeN0_190591.png
  [4]: http://static.oschina.net/uploads/space/2013/1108/234411_gvSE_190591.png
