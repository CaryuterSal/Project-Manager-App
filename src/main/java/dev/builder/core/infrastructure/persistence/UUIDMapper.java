package dev.builder.core.infrastructure.persistence;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class UUIDMapper {

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

    public static @Nullable UUID extractUUID(@NotNull ResultSet resultSet, String columnName) throws SQLException {
        byte[] bytes = resultSet.getBytes(columnName);
        if(bytes.length == 0) return null;
        return byteArrayToUUID(bytes);
    }
}
