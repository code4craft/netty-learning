Netty中的管道
--------
>尚未完成

Pipeline是理解Netty的核心。之前在概述中粗略讲到了事件驱动机制，在这篇文章中，我们将详细分析Pipeline的实现。

Netty的Pipeline包含两条流线路：Upstream和Downstream

这里的pipeline机制是这样的：

首先handler分为Upstream和Downstream两类。然后每个handler会接收到一个事件，如果需要继续处理，那么**它会发起一个事件**，这个事件只有它之后的handler会接收到。**如果它不再发起事件，那么处理就到此结束。**

                                          I/O Request
                                        via {@link Channel} or
                                    {@link ChannelHandlerContext}
                                              |
     +----------------------------------------+---------------+
     |                  ChannelPipeline       |               |
     |                                       \|/              |
     |  +----------------------+  +-----------+------------+  |
     |  | Upstream Handler  N  |  | Downstream Handler  1  |  |
     |  +----------+-----------+  +-----------+------------+  |
     |            /|\                         |               |
     |             |                         \|/              |
     |  +----------+-----------+  +-----------+------------+  |
     |  | Upstream Handler N-1 |  | Downstream Handler  2  |  |
     |  +----------+-----------+  +-----------+------------+  |
     |            /|\                         .               |
     |             .                          .               |
     |     [ sendUpstream() ]        [ sendDownstream() ]     |
     |     [ + INBOUND data ]        [ + OUTBOUND data  ]     |
     |             .                          .               |
     |             .                         \|/              |
     |  +----------+-----------+  +-----------+------------+  |
     |  | Upstream Handler  2  |  | Downstream Handler M-1 |  |
     |  +----------+-----------+  +-----------+------------+  |
     |            /|\                         |               |
     |             |                         \|/              |
     |  +----------+-----------+  +-----------+------------+  |
     |  | Upstream Handler  1  |  | Downstream Handler  M  |  |
     |  +----------+-----------+  +-----------+------------+  |
     |            /|\                         |               |
     +-------------+--------------------------+---------------+
                   |                         \|/
     +-------------+--------------------------+---------------+
     |             |                          |               |
     |     [ Socket.read() ]          [ Socket.write() ]      |
     |                                                        |
     |  Netty Internal I/O Threads (Transport Implementation) |
     +--------------------------------------------------------+


![universal API][1]

![channel pipeline][2]

  [1]: http://static.oschina.net/uploads/space/2013/1109/073639_dgV1_190591.png
  [2]: http://static.oschina.net/uploads/space/2013/1108/234357_DeN0_190591.png
  [3]: http://static.oschina.net/uploads/space/2013/1108/234411_gvSE_190591.png
