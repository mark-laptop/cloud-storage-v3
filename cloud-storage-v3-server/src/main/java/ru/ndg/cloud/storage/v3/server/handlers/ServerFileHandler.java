package ru.ndg.cloud.storage.v3.server.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ndg.cloud.storage.v3.common.services.State;
import ru.ndg.cloud.storage.v3.common.services.TypeCommand;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class ServerFileHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LogManager.getLogger(ServerFileHandler.class);
    private State currentState = State.IDLE;
    private int nextLength;
    private long fileLength;
    private long receivedFileLength;
    private BufferedOutputStream out;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        while (buf.readableBytes() > 0) {
            if (this.currentState == State.IDLE) {
                byte read = buf.readByte();
                if (read == TypeCommand.FILE_COMMAND.getCommandByte()) {
                    this.currentState = State.NAME_LENGTH;
                    this.receivedFileLength = 0L;
                    logger.debug("STATE: Start file receiving");
                } else {
                    logger.debug("ERROR: Invalid first byte - {}", read);
                }
            }


            if (this.currentState == State.NAME_LENGTH) {
                if (buf.readableBytes() >= 4) {
                    logger.debug("STATE: Get filename length");
                    this.nextLength = buf.readInt();
                    this.currentState = State.NAME;
                }
            }

            if (this.currentState == State.NAME) {
                if (buf.readableBytes() >= this.nextLength) {
                    byte[] fileName = new byte[this.nextLength];
                    buf.readBytes(fileName);
                    String fileNameString = new String(fileName, StandardCharsets.UTF_8);
                    logger.debug("STATE: Filename received - {}", new String(fileName, StandardCharsets.UTF_8));
                    this.out = new BufferedOutputStream(new FileOutputStream(fileNameString));
                    this.currentState = State.FILE_LENGTH;
                }
            }

            if (this.currentState == State.FILE_LENGTH) {
                if (buf.readableBytes() >= 8) {
                    this.fileLength = buf.readLong();
                    logger.debug("STATE: File length received - {}", this.fileLength);
                    this.currentState = State.FILE;
                }
            }

            if (this.currentState == State.FILE) {
                while (buf.readableBytes() > 0) {
                    this.out.write(buf.readByte());
                    this.receivedFileLength++;
                    if (this.fileLength == this.receivedFileLength) {
                        this.currentState = State.IDLE;
                        logger.debug("File received");
                        this.out.close();
                        break;
                    }
                }
            }
        }
        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.debug(cause.getMessage());
        ctx.close();
    }
}
