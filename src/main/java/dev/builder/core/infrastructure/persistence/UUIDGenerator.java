package dev.builder.core.infrastructure.persistence;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

public class UUIDGenerator {

    @Contract(" -> new")
    public static @NotNull UUID generateUUID(){
        return  UUID.randomUUID();
    }

    public static byte @NotNull [] UUIDtoByteArray(@NotNull UUID uuid){
        byte[] bytes = new byte[16];
        ByteBuffer bb = ByteBuffer.wrap(bytes)
                .order(ByteOrder.BIG_ENDIAN)
                .putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits());
        return bytes;
    }

    public static @NotNull UUID byteArrayToUUID(byte @NotNull [] bytes){
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        return new UUID(bb.getLong(),bb.getLong());
    }
}
