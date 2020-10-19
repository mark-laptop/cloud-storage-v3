package ru.ndg.cloud.storage.v3.common.services;

public enum TypeCommand {

    AUTH_COMMAND((byte) 10), REGISTRATION_COMMAND((byte) 11), FILE_COMMAND((byte) 12), EMPTY((byte) -1);

    private byte commandByte;

    TypeCommand(byte commandByte) {
        this.commandByte = commandByte;
    }

    public byte getCommandByte() {
        return commandByte;
    }

    public static TypeCommand getTypeCommandFromByte(byte readableByte) {
        if (readableByte == AUTH_COMMAND.commandByte) {
            return AUTH_COMMAND;
        }
        if (readableByte == REGISTRATION_COMMAND.commandByte) {
            return REGISTRATION_COMMAND;
        }
        if (readableByte == FILE_COMMAND.commandByte) {
            return FILE_COMMAND;
        }
        return EMPTY;
    }
}
