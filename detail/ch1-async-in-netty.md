理解Netty中的异步
====
首先，事件驱动的Handler机制本身就是异步的。其次，`ChannelFuture`为异步操作提供了同步的方式。与Future对应的还有`ChannelListener`。

Future可以理解为化异步为同步的一个方式。JDK里的`Future`就是如此。