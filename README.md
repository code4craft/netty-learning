netty那点事
-----

Netty和Mina是Java世界通讯框架的首选。它们都出自同一个作者，Mina诞生略早，属于Apache基金会，而Netty开始在Jboss名下，后来出来自立门户netty.io。关于Mina已有@FrankHui的[Mina系列文章](http://my.oschina.net/ielts0909/blog/92716)，我正好最近也要做一些网络方面的开发，就研究一下Netty的源码，顺便分享出来了。

Netty目前有两个分支：4.x和3.x。4.0分支重写了很多东西，并对项目进行了分包，规模比较庞大，入手会困难一些，而3.x版本则已经被广泛使用。本系列文章针对netty 3.7.0 final。3.x和4.0的区别可以参考这篇文章：[http://www.oschina.net/translate/netty-4-0-new-and-noteworthy?print](http://www.oschina.net/translate/netty-4-0-new-and-noteworthy?print)。

## 提纲

### [1.概述](https://github.com/code4craft/netty-learning/blob/master/ch1-overview.md)
### [2.Buffer](https://github.com/code4craft/netty-learning/blob/master/ch2-buffer.md)
### 3.Channel
### 4.ChannelHandler及Pipeline
### 5.协议支持