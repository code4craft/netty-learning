Netty那点事（五）讲讲Handler
-------
至上部分为止，我觉得Netty的架构部分已经差不多说完了，还有些细节，可以在实践中慢慢掌握。

但是对于实践来说，Netty还有不容忽视的一部分：Netty提供了大量的ChannelHandler，可以完成不同的任务。用好它们，会使Netty在你手里更加得心应手！

## 业务多线程执行

`OrderedMemoryAwareThreadPoolExecutor`

`ExecutionHandler`


## 粘包/分包

## 编码/解码

ReplayingDecoder

OneToOneEncoder

...