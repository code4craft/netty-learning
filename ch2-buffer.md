Netty那点事（二）-Netty中的buffer
--------
>尚未完成

上一篇文章我们概要介绍了Netty的原理及结构，下面几篇文章我们开始对Netty的各个模块进行比较详细的分析。Netty的结构最底层是Buffer机制，这部分也相对独立，我们就先从Buffer讲起。

## What：Buffer二三事

Buffer中文名又叫缓冲区，

NIO中的ByteBuffer。

关于ByteBuffer的吐槽：[http://www.javacodegeeks.com/2012/12/the-java-bytebuffer-a-crash-course.html](http://www.javacodegeeks.com/2012/12/the-java-bytebuffer-a-crash-course.html)

## When & Where：TCP/IP协议与Buffer

MTU与Nagle算法

## Why：Buffer中的分层思想

在讲解具体代码前，我又要卖个关子了。

关于Zero-Copy-Capable，我觉得理解为什么需要"Zero-Copy-Capable Rich Byte Buffer"，比理解它是怎么实现的，可能要更重要一点。我们先回到之前的`messageReceived`方法：

    public void messageReceived(
            ChannelHandlerContext ctx, MessageEvent e) {
        // Send back the received message to the remote peer.
        transferredBytes.addAndGet(((ChannelBuffer) e.getMessage()).readableBytes());
        e.getChannel().write(e.getMessage());
    }
    
这里`MessageEvent.getMessage()`默认的返回值是一个`ChannelBuffer`。我们知道，业务中需要的"Message"，其实是一条应用层级别的完整消息，而一般的buffer工作在传输层，与"Message"是不能对应上的。那么这个ChannelBuffer是什么呢？

来一个官方User Guide里的图，我想更能说明这个问题：

![virtual buffer in Netty][3]

这里看到，TCP层HTTP报文被分成了两个ChannelBuffer，这两个Buffer对我们上层的逻辑(HTTP处理)是没有意义的。但是两个ChannelBuffer被组合起来，就成为了一个有意义的HTTP报文，这个报文对应一个ChannelBuffer，这才是能称之为"Message"的东西。这里用到了一个词"Virtual Buffer"，也就是所谓的"Zero-Copy-Capable Rich Byte Buffer"了。

所以，**如果说NIO的Buffer和Netty的ChannelBuffer最大的区别的话，就是前者仅仅是网络传输上的Buffer，而后者是传输Buffer和抽象后的逻辑Buffer的结合。**延伸开来说，NIO仅仅是一个网络传输框架，而Netty是一个网络应用框架，包括网络以及应用的分层结构。

当然，在Netty里，默认使用`ChannelBuffer`表示"Message"，不失为一个比较实用的方法，但是`MessageEvent.getMessage()`是可以存放一个POJO的，这样子抽象程度又高了一些，这个我们在以后讲到`ChannelPipeline`的时候会说明。

## How：Netty中的ChannelBuffer及实现

好了，最后我们来看一下具体的实现，满足一下程序员的求知欲吧。Netty 3.7的buffer实现还是比较简单的，没有太多费脑细胞的地方。

ChannelBuffers是所有ChannelBuffer实现类的入口，它提供了很多静态的工具方法来创建不同的Buffer，靠“顺藤摸瓜”式读代码方式，大致能把各种ChannelBuffer的实现类摸个遍。

![channel buffer in Netty][1]


### ChannelBuffer中的readerIndex和writerIndex

开始以为Netty的ChannelBuffer是对NIO ByteBuffer的一个封装，其实不是的，**它是把ByteBuffer重新实现了一遍**。

以最常用的`HeapChannelBuffer`为例，其底层也是一个byte[]，与ByteBuffer不同的是，它是可以同时进行读和写的，而不需要使用flip()进行读写切换。ChannelBuffer读写的核心代码在`AbstactChannelBuffer`里，这里通过readerIndex和writerIndex两个整数，分别指向当前读的位置和当前写的位置，并且，readerIndex总是小于writerIndex的。贴两段代码，让大家能看的更明白一点：

```java
    public void writeByte(int value) {
        setByte(writerIndex ++, value);
    }

    public byte readByte() {
        if (readerIndex == writerIndex) {
            throw new IndexOutOfBoundsException("Readable byte limit exceeded: "
                    + readerIndex);
        }
        return getByte(readerIndex ++);
    }

    public int writableBytes() {
        return capacity() - writerIndex;
    }
    
    public int readableBytes() {
        return writerIndex - readerIndex;
    }
```

我倒是觉得这样的方式非常自然，比单指针与flip()要更加好理解一些。AbstactChannelBuffer还有两个相应的mark指针`markedReaderIndex`和`markedWriterIndex`，跟NIO的原理是一样的，这里不再赘述了。

### 字节序Endianness

在创建Buffer时，我们注意到了这样一个方法：`public static ChannelBuffer buffer(ByteOrder endianness, int capacity);`，其中`ByteOrder`是什么意思呢？

这里有个很基础的概念：字节序(ByteOrder/Endianness)。它规定了多余一个字节的数字(int啊long什么的)，如何在内存中表示。BIG_ENDIAN(大端序)表示高位在前，整型数`12`会被存储为`0 0 0 12`四字节，而LITTLE_ENDIAN则正好相反。可能搞C/C++的程序员对这个会比较熟悉，而Javaer则比较陌生一点，因为Java已经把内存给管理好了。但是在网络编程方面，根据协议的不同，不同的字节序也可能会被用到。目前大部分协议还是采用大端序，可参考[RFC1700](http://tools.ietf.org/html/rfc1700)。

了解了这些知识，我们也很容易就知道为什么会有`BigEndianHeapChannelBuffer`和`LittleEndianHeapChannelBuffer`了！

### DynamicChannelBuffer

DynamicChannelBuffer是一个很方便的Buffer，之所以叫Dynamic是因为它的长度会根据内容的长度来扩充，你可以像使用ArrayList一样，无须关心其容量。实现自动扩容的核心在于`ensureWritableBytes`方法，算法很简单：在写入前做容量检查，容量不够时，新建一个容量x2的buffer，跟ArrayList的扩容是相同的。贴一段代码吧(为了代码易懂，这里我删掉了一些边界检查，只保留主逻辑)：

```java
    public void writeByte(int value) {
        ensureWritableBytes(1);
        super.writeByte(value);
    }

    public void ensureWritableBytes(int minWritableBytes) {
        if (minWritableBytes <= writableBytes()) {
            return;
        }

        int newCapacity = capacity();
        int minNewCapacity = writerIndex() + minWritableBytes;
        while (newCapacity < minNewCapacity) {
            newCapacity <<= 1;
        }

        ChannelBuffer newBuffer = factory().getBuffer(order(), newCapacity);
        newBuffer.writeBytes(buffer, 0, writerIndex());
        buffer = newBuffer;
    }
```

### CompositeChannelBuffer

`CompositeChannelBuffer`是由多个ChannelBuffer组合而成的，可以看做一个整体进行读写。这里有一个技巧：CompositeChannelBuffer并不会开辟新的内存并直接复制所有ChannelBuffer内容，而是直接保存了所有ChannelBuffer的引用，并在子ChannelBuffer里进行读写，从而实现了"Zero-Copy-Capable"了。来段简略版的代码吧：

```java
	public class CompositeChannelBuffer{

	    //components保存所有内部ChannelBuffer
	    private ChannelBuffer[] components;
	    //indices记录在整个CompositeChannelBuffer中，每个components的起始位置
	    private int[] indices;
	    //缓存上一次读写的componentId
	    private int lastAccessedComponentId;

	    public byte getByte(int index) {
	        //通过indices中记录的位置索引到对应第几个子Buffer
	        int componentId = componentId(index);
	        return components[componentId].getByte(index - indices[componentId]);
	    }

	    public void setByte(int index, int value) {
	        int componentId = componentId(index);
	        components[componentId].setByte(index - indices[componentId], value);
	    }

	}		
```

查找componentId的算法再次不作介绍了，大家自己实现起来也不会太难。值得一提的是，基于ChannelBuffer连续读写的特性，使用了顺序查找(而不是二分查找)，并且用`lastAccessedComponentId`来进行缓存。

### WrappedChannelBuffer

![virtual buffer in Netty][2]


writerIndex

readerIndex

HeapBuffer

DirectBuffer

trivial

CompositeChannelBuffer gathering

4.0之后ChannelBuffer改名ByteBuf，成了单独项目，为了性能优化，加入了BufferPool之类的机制，已经变得比较复杂了，但是本质倒没怎么变。性能优化是个很复杂的事情，研究源码时，建议先避开这些东西，除非你对算法情有独钟。举个例子，Netty4.0里为了优化，将Map换成了Java 8里6000行的[ConcurrentHashMapV8](https://github.com/netty/netty/blob/master/common/src/main/java/io/netty/util/internal/chmv8/ConcurrentHashMapV8.java)，你们感受一下…

  [1]: http://static.oschina.net/uploads/space/2013/0925/081551_v8pK_190591.png
  [2]: http://static.oschina.net/uploads/space/2013/0925/074748_oSkl_190591.png
  [3]: http://netty.io/3.7/guide/images/combine-slice-buffer.png
  

参考资料：

* Endianness [http://en.wikipedia.org/wiki/Endianness](http://en.wikipedia.org/wiki/Endianness)
