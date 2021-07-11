package org.maiter.com.week3.filter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

public class HeaderHttpRequestFilter implements HttpRequestFilter {

    @Override
    public void filter(FullHttpRequest fullRequest, ChannelHandlerContext ctx) {
        if(fullRequest.headers().contains("netty")) {
            fullRequest.headers().set("sign", "ok");
        } else {
            fullRequest.headers().set("sign", "error");
        }
    }
}
