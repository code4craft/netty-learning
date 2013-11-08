Netty中的Pipeline
--------
>>尚未完成

Pipeline是netty中比较有特色的一个部分。

这里的pipeline机制是这样的：

首先handler分为Upstream和Downstream两类。然后每个handler会接收到一个事件，如果需要继续处理，那么**它会发起一个事件**，这个事件只有它之后的handler会接收到。**如果它不再发起事件，那么处理就到此结束。**

![channel pipeline][1]

  [1]: http://static.oschina.net/uploads/space/2013/1022/115030_XzuA_190591.png