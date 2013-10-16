使用netty构建一个socks proxy
---
最近在做的项目，需要自己搭建一个socks代理。netty4.0附带了一个socks代理的样例，但是3.x就没有这个东西了，碰巧使用的又是3.7，就只能自己摸索并实现一遍，也算是对netty和socks协议的一个熟悉。
socks代理涉及到server、client、协议解析等功能，是一个比较复杂的网络程序，对于学习netty的使用也是非常好的例子。

socks是在传输层之上的一层协议，主要功能是提供代理认证等功能。socks协议虽然是应用层协议(在TCP/IP4层协议栈里)，本身可以理解为一个信道，可以传输任何TCP/UDP内容。例如著名的科学上网软件就是基于socks协议，对通信内容进行加密实现的。

TCP/IP协议栈的结构中，下层协议总会在上层协议内容之前加上自己的头。而socks协议稍微不同，其实它对比TCP协议，仅仅是多了验证部分，验证之后，完全是使用TCP来进行传输，而没有socks报文头。socks协议的具体内容可以参考[rfc1928](http://www.ietf.org/rfc/rfc1928.txt)。这一点来说，其实将socks理解成与其他应用层协议平级也没什么问题。



一个最基本的socks连接流程是这样的：
![socks][1]

  [1]: http://static.oschina.net/uploads/space/2013/1016/161647_wYsq_190591.png
  
  