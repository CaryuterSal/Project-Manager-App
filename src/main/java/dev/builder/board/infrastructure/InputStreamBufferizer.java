package dev.builder.board.infrastructure;

import dev.builder.core.infrastructure.di.annotation.Bean;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Bean
public class InputStreamBufferizer {

    public byte[] toByteArray(InputStream stream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int nRead;
        while ((nRead = stream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }
}
