netty那点事(三) NioServerChannel
--------
终于来到了最重要的Channel部分。

Bind: `NioServerSocketChannel`

Accept:`NioServerSocketPipelineSink`



实际上Channel部分没有太多内容，

![channel in Netty][1]

>TODO

ServerChannel

SocketChannel

DatagramChannel

LocalChannel

----------

ChannelHandler

ChannelUpstreamHandler

ChannelDownstreamHandler

Config Parent & Child

通过handler把底层隔离了

Netty channel部分比较复杂，一时可能难以入手。我们先结合一些NIO Server的知识，从`NioServerSocketChannel`入手，讲讲一个基于NIO的服务器的流程。

[http://rdc.taobao.com/team/jm/archives/423](http://rdc.taobao.com/team/jm/archives/423)

  [1]: http://static.oschina.net/uploads/space/2013/0929/174705_47Rr_190591.png
  
Channels部分事件流转静态方法
1．fireChannelOpen 2．fireChannelBound 3．fireChannelConnected 4．fireMessageReceived 5．fireWriteComplete 6．fireChannelInterestChanged
7．fireChannelDisconnected 8．fireChannelUnbound 9．fireChannelClosed 10.fireExceptionCaught 11.fireChildChannelStateChanged


http://en.wikipedia.org/wiki/Sink_(computing)