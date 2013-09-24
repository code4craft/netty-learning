Netty那点事(二)-Netty中的buffer
--------
>尚未完成

上一篇文章我们概要介绍了Netty的原理及结构，下面几篇文章我们开始对Netty的各个模块进行比较详细的分析。Netty的结构最底层是Buffer机制，这部分也相对独立，我们就先从Buffer讲起。

## What：Buffer二三事

Buffer中文名又叫缓冲区，


关于ByteBuffer的吐槽：[http://www.javacodegeeks.com/2012/12/the-java-bytebuffer-a-crash-course.html](http://www.javacodegeeks.com/2012/12/the-java-bytebuffer-a-crash-course.html)

## How：Netty中的ChannelBuffer及实现

ChannelBuffers是所有ChannelBuffer实现类的入口，它提供了很多静态的工具方法来创建不同的Buffer，靠“顺藤摸瓜”式读代码方式，大致能把各种ChannelBuffer的实现类摸个遍。

### ChannelBuffer中的readerIndex和writerIndex

开始以为Netty的ChannelBuffer是对NIO ByteBuffer的一个封装，其实不是的，**它是把ByteBuffer重新实现了一遍**。不熟悉ByteBuffer的可以看看我的博文[图解ByteBuffer](http://my.oschina.net/flashsword/blog/159613)

### 字节序

这里有个很基础的概念：ByteOrder(字节序)。可能搞C/C++的程序员比较熟悉，而Javaer则比较陌生一点。它规定了多余一个字节的数字(int啊long什么的)，如何在内存中表示。BIG_ENDIAN(大端序)表示高位在前，整型数`12`会被存储为`0 0 0 12`四字节，而LITTLE_ENDIAN则正好相反。因为Java已经把内存给管理好了，所以不用关心到底是大端序还是小端序，但是在网络编程方面，因为可能涉及异构的系统，所以还是要注意一下的。主流还是大端序[http://tools.ietf.org/html/rfc1700](http://tools.ietf.org/html/rfc1700)。

### DynamicChannelBuffer

DynamicChannelBuffer是一个很方便的Buffer，之所以叫Dynamic是因为它的长度会根据内容的长度来扩充，你可以像使用ArrayList一样，无须关心其容量。实现自动扩容的核心在于`ensureWritableBytes`方法，算法很简单：容量不够时，新建一个容量x2的buffer，跟ArrayList的扩容是相同的。

### Wrapper




writerIndex

readerIndex

HeapBuffer

DirectBuffer

trivial

CompositeChannelBuffer gathering

## When & Where：TCP/IP协议与Buffer

MTU与Nagle算法

## Why：代码背后的设计思想

分层

![virtual buffer in Netty][1]

  [1]: http://netty.io/3.7/guide/images/combine-slice-buffer.png

参考资料：

* Endianness [http://en.wikipedia.org/wiki/Endianness](http://en.wikipedia.org/wiki/Endianness)
