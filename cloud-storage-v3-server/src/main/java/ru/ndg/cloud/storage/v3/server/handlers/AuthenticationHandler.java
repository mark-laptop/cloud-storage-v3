package ru.ndg.cloud.storage.v3.server.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ndg.cloud.storage.v3.common.model.User;
import ru.ndg.cloud.storage.v3.common.services.TypeCommand;
import ru.ndg.cloud.storage.v3.common.services.UserService;
import ru.ndg.cloud.storage.v3.common.services.UserServiceImpl;

import java.nio.charset.StandardCharsets;

public class AuthenticationHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LogManager.getLogger(AuthenticationHandler.class);
    private UserService userService;
    private boolean isAuthenticated;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.isAuthenticated = false;
        this.userService = new UserServiceImpl();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        while (buf.readableBytes() > 0) {
            if (TypeCommand.AUTH_COMMAND == TypeCommand.getTypeCommandFromByte(buf.getByte(0))) {
                buf.readByte();
                String authenticationString = buf.toString(StandardCharsets.UTF_8).trim();
                buf.release();
                String[] arrayStringAuthentication = authenticationString.split(" ");
                User user = this.userService.getUserByLoginPassword(arrayStringAuthentication[0], arrayStringAuthentication[1]);
                ByteBuf result = ctx.alloc().buffer();
                if (user != null) {
                    result.writeBoolean(true);
                    this.isAuthenticated = true;
                }
                else
                    result.writeBoolean(false);
                ctx.writeAndFlush(result);
                break;
            }
            if (TypeCommand.REGISTRATION_COMMAND == TypeCommand.getTypeCommandFromByte(buf.getByte(0))) {
                buf.readByte();
                String authenticationString = buf.toString(StandardCharsets.UTF_8).trim();
                buf.release();
                String[] arrayStringAuthentication = authenticationString.split(" ");
                User user = userService.createUser(arrayStringAuthentication[0], arrayStringAuthentication[1]);
                this.isAuthenticated = true;
                ByteBuf result = ctx.alloc().buffer();
                result.writeBoolean(true);
                ctx.writeAndFlush(result);
                break;
            }
            if (TypeCommand.FILE_COMMAND == TypeCommand.getTypeCommandFromByte(buf.getByte(0)) && isAuthenticated) {
                ctx.fireChannelRead(msg);
                break;
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.debug(cause.getMessage());
        ctx.close();
    }
}
