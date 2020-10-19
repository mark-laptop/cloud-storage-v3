package ru.ndg.cloud.storage.v3.common.services;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

public class AuthService {
    public static void sendAuthCommand(String command, Channel channel, ChannelFutureListener finishListener) {
        sendCommand(command, TypeCommand.AUTH_COMMAND.getCommandByte(), channel, finishListener);
    }

    public static void sendRegCommand(String command, Channel channel, ChannelFutureListener finishListener) {
        sendCommand(command, TypeCommand.REGISTRATION_COMMAND.getCommandByte(), channel, finishListener);
    }

    private static void sendCommand(String command, byte commandByte, Channel channel, ChannelFutureListener finishListener) {
        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(command.getBytes().length + 1);
        buf.writeByte(commandByte);
        buf.writeBytes(command.getBytes());
        ChannelFuture transferOperationFuture = channel.writeAndFlush(buf);
        if (finishListener != null) {
            transferOperationFuture.addListener(finishListener);
        }
    }
}
