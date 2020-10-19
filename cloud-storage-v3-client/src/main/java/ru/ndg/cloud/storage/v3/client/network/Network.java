package ru.ndg.cloud.storage.v3.client.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ndg.cloud.storage.v3.client.handlers.Callback;
import ru.ndg.cloud.storage.v3.client.handlers.ClientMainHandler;
import ru.ndg.cloud.storage.v3.common.services.AuthService;
import ru.ndg.cloud.storage.v3.common.services.FileService;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

public class Network {

    private static final Logger logger = LogManager.getLogger(Network.class);
    private Channel channel;
    private String host;
    private int port;
    private Callback callback;

    public Network(Callback callback) {
        this.callback = callback;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void run(CountDownLatch latch) {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.SO_TIMEOUT, 10)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            Network.this.channel = ch;
                            ch.pipeline().addLast(
                                    new ClientMainHandler(callback)
                            );
                        }
                    });
            ChannelFuture future = bootstrap.connect(host, port).sync();
            latch.countDown();
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            logger.debug(e.getMessage());
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }

    public ChannelFuture close() {
        try {
            return this.channel.close().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isOpen() {
        if (channel == null) return false;
        return this.channel.isOpen();
    }

    public void sendFile(String login, ChannelFutureListener listener) {
        try {
            FileService.sendFile(login, Paths.get("cloud_storage_client","1.txt"), this.channel, listener);
        } catch (IOException e) {
            logger.debug(e.getMessage());
        }
    }

    public void sendAuthCommand(String cmd, ChannelFutureListener listener) {
        AuthService.sendAuthCommand(cmd, this.channel, listener);
    }

    public void sendRegCommand(String cmd, ChannelFutureListener listener) {
        AuthService.sendRegCommand(cmd, this.channel, listener);
    }
}
