netty那点事
-----

一直都听说netty或者mina很牛逼，是Java世界通讯框架的倚天剑屠龙刀，近年来大有一统江湖的趋势。netty和mina两个都出自一个大师之手，Mina诞生略早，早已是武林正统apache的门客，而netty开始在另一大帮派-Jboss门下，后来出来自立门户netty.io。关于mina已有@FrankHui的[Mina系列文章](http://my.oschina.net/ielts0909/blog/92716)，我就斗胆来写一份netty攻略，来分享给各位江湖猿友了。

学习netty，必须先修得Java内功，并发和NIO两门内功自然是必不可少的，不然大侠还是重新来过吧。如果还有一些TCP/IP的修为，那是再好不过了。

netty目前有两个分支，4.x和3.x。4.0分支重写了很多东西，并对项目进行了分包，规模比较庞大，入手会困难一些，而3.x版本则已经被广泛使用。3.x和4.0的区别可以参考这篇文章：[http://www.oschina.net/translate/netty-4-0-new-and-noteworthy?print](http://www.oschina.net/translate/netty-4-0-new-and-noteworthy?print)。本系列文章针对netty 3.7.0 final。

## 提纲

### 1.概述
### 2.Buffer
### 3.Channel
### 4.ChannelHandler及Pipeline
### 5.协议支持