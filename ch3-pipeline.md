Netty中的管道
--------
>尚未完成

Pipeline是理解和使用Netty的核心。之前在概述中粗略讲到了事件驱动机制，在这篇文章中，我们将详细分析Pipeline及其的实现。

Netty的Pipeline包含两条线路：Upstream和Downstream。这部分Netty的`ChannelPipeline`接口的javadoc里有一个非常形象的图，我就直接引用了(我对原图进行了一点修改，加上了`ChannelSink`，因为我觉得这部分对理解代码流程会有些帮助)：

![channel pipeline][1]

这里的pipeline机制是这样的：

首先handler分为Upstream和Downstream两类。然后每个handler会接收到一个事件，如果需要继续处理，那么**它会发起一个事件**，这个事件只有它之后的handler会接收到。**如果它不再发起事件，那么处理就到此结束。**




![universal API][2]



  [1]: http://static.oschina.net/uploads/space/2013/1109/075339_Kjw6_190591.png
  [2]: http://static.oschina.net/uploads/space/2013/1108/234357_DeN0_190591.png
  [3]: http://static.oschina.net/uploads/space/2013/1108/234411_gvSE_190591.png
