package ru.ndg.cloud.storage.v3.server.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ndg.cloud.storage.v3.server.handlers.AuthenticationHandler;
import ru.ndg.cloud.storage.v3.server.handlers.ServerFileHandler;

import java.util.concurrent.CountDownLatch;

public class Server {

    private static final Logger logger = LogManager.getLogger(Server.class);
    private int port;

    public Server() {}

    public void setPort(int port) {
        this.port = port;
    }

    public void run(CountDownLatch latch) {
        EventLoopGroup bossGroup = new NioEventLoopGroup(2);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline().addLast(
                                    new AuthenticationHandler(),
                                    new ServerFileHandler()
                            );
                        }
                    });
            ChannelFuture sync = bootstrap.bind(port).sync();
            latch.countDown();
            sync.channel().closeFuture().sync();
        } catch (Exception e) {
            logger.debug(e.getMessage());
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
