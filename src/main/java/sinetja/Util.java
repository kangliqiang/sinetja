package sinetja;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

class Util {
  //----------------------------------------------------------------------------
  // Utilities to write.

  public static ChannelFuture keepAliveWriteAndFlush(ChannelHandlerContext ctx, HttpRequest req, HttpResponse res) {
    if (!HttpHeaders.isKeepAlive(req)) {
      return ctx.writeAndFlush(res).addListener(ChannelFutureListener.CLOSE);
    } else {
      res.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
      return ctx.writeAndFlush(res);
    }
  }

  public static ChannelFuture keepAliveWriteAndFlush(Channel ch, HttpRequest req, HttpResponse res) {
    if (!HttpHeaders.isKeepAlive(req)) {
      return ch.writeAndFlush(res).addListener(ChannelFutureListener.CLOSE);
    } else {
      res.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
      return ch.writeAndFlush(res);
    }
  }
}
