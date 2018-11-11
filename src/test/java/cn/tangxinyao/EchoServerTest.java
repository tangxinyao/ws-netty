package cn.tangxinyao;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
public class EchoServerTest {

    private static final String host = "localhost";
    private static final int port = 8080;

    @Test
    public void assertNoException() throws InterruptedException {
        Executor executor = Executors.newFixedThreadPool(4);
        executor.execute(new EchoServer());
        executor.execute(new EchoClient());
        Thread.sleep(10000);
    }

    public static class EchoClient implements Runnable {
        public void run() {
            // initialize
            EventLoopGroup group = new NioEventLoopGroup();
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.remoteAddress(new InetSocketAddress(host, port));
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel channel) throws Exception {
                    channel.pipeline().addLast(new EchoClientHandler());
                }
            });

            // start server
            try {
                ChannelFuture future = bootstrap.connect().sync();
                future.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                log.error(e.toString());
            } finally {
                group.shutdownGracefully();
            }
        }
    }

    @ChannelHandler.Sharable
    public static class EchoClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            ctx.writeAndFlush(Unpooled.copiedBuffer("Netty rocks!", CharsetUtil.UTF_8));
        }

        protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
            log.info("Client received: " + byteBuf.toString(CharsetUtil.UTF_8));
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.close();
        }
    }
}
