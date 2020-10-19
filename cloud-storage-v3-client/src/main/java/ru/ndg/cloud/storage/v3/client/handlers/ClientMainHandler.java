package ru.ndg.cloud.storage.v3.client.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientMainHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LogManager.getLogger(ClientMainHandler.class);
    private Callback callback;

    public ClientMainHandler(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.debug(cause.getMessage());
        ctx.close();
    }
}
