理解Netty中的异步
====
首先，事件驱动的Handler机制本身就是异步的。其次，`ChannelFuture`保证了操作的异步性。

