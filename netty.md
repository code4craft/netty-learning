netty那点事-倚天剑屠龙刀
-----

一直都听说netty或者mina很牛逼，是Java世界通讯框架的倚天剑屠龙刀，大有“号令天下，莫敢不从”的意思。netty和mina两个都出自一个大师之手，Mina诞生略早，早已是武林正统apache的门客，而netty开始在另一大帮派-Jboss门下，后来出来自立门户netty.io。关于mina已有@FrankHui的[Mina系列文章](http://my.oschina.net/ielts0909/blog/92716)，我就斗胆来写一份netty攻略，来分享给各位江湖猿友了。

学习netty，必须先修得Java内功，并发和NIO两门内功自然是必不可少的，不然大侠还是重新来过吧。如果还有一些TCP/IP的修为，那是再好不过了。

本系列文章针对netty 4.0.0 final，并且只包括netty核心，即buffer、channel以及common部分。

## 起：netty是什么

大概用netty的，无论新手还是高手，都知道它是一个“异步通讯框架”。比起NIO，它多了一些东西：

连接管理和复用

线程管理和复用



## 承：netty的结构

Java世界的框架大多追求大而全，如果逐个阅读，难免迷失方向，鄙人以为，抓住几个重点对象，理解其领域概念，从而理清其脉络，相当于打通了任督二脉，以后的阅读就不再困难了。等到

对代码的第一印象来看，netty的作者大概是“重复发明轮子”教的教主，netty有一半以上的类，都是跟JDK的概念直接对应，甚至连名字都不曾修改。例如NIO的两大组件：Buffer和Channel，netty里分别有两个包`io.netty.buffer`和`io.netty.channel`来对应；而并发框架的ExecutorService，netty也有类似的概念`EventExecutor`。

Channel

Buffer

EventExecutorGroup

ChannelPipeline

ChannelHandler



## 转：

## 合：


多线程及线程管理

NIO 

连接及连接管理

协议解析

有了这些还不行，你还必须得封装一套简便的API给使用者，要不然怎么叫框架呢？



Channel

EventExecutor

ChannelPipeline

ChannelHandler

只记得几个名字，除了在面试时忽悠忽悠，是没有任何意义的。

netty做了NIO之外的事：

-----------------

