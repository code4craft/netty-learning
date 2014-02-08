package us.codecraft.netty.reactor;

import java.io.IOException;

/**
 * @author yihua.huang@dianping.com
 */
public class Main {

    public static void main(String[] args) throws IOException {
        Reactor reactor = new Reactor();
        new Handler(reactor.selector);
        reactor.run();
    }
}
