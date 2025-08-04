package dev.builder.board.domain.port.out;

import java.io.InputStream;

public interface MimeTypeGenerator {
    String generateMimeType(InputStream inputStream);
}
