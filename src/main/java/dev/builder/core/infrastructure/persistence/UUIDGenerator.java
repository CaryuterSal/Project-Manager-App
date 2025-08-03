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
}
