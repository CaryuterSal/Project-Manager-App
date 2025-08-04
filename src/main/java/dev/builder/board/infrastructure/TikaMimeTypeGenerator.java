package dev.builder.board.infrastructure;

import dev.builder.board.domain.port.out.MimeTypeGenerator;
import dev.builder.core.infrastructure.di.annotation.Bean;
import org.apache.tika.Tika;

import java.io.IOException;
import java.io.InputStream;

@Bean
public class TikaMimeTypeGenerator implements MimeTypeGenerator {

    @Override
    public String generateMimeType(InputStream inputStream) {
        try {
            Tika tika = new Tika();
            return tika.detect(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
