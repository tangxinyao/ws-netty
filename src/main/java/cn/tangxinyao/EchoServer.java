package cn.tangxinyao;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class EchoServer implements Runnable {
    private static int port = 8080;

    public void run() {
        NioEventLoopGroup group = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(group);
        serverBootstrap.channel(NioServerSocketChannel.class);
        serverBootstrap.localAddress(new InetSocketAddress(port));
        serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast(new EchoServerHandler());
            }
        });
        try {
            ChannelFuture future = serverBootstrap.bind().sync();
            log.info(EchoServer.class.getName() + " started and listen on " + future.channel().localAddress());
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static class EchoServerHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf in = (ByteBuf) msg;
            log.info("Server received: " + in.toString(CharsetUtil.UTF_8));
            ctx.write(in);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.writeAndFlush(ChannelFutureListener.CLOSE).addListener(ChannelFutureListener.CLOSE);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
