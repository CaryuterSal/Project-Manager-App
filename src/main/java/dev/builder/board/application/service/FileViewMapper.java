package dev.builder.board.application.service;

import dev.builder.board.application.view.FileView;
import dev.builder.board.domain.model.StoredFile;
import dev.builder.core.infrastructure.di.annotation.Bean;
import org.jetbrains.annotations.NotNull;

@Bean
public class FileViewMapper {

    public FileView toView(@NotNull StoredFile<?> file){
        return new FileView(file.id().uuid(), file.name().value(), file.mimeType());
    }
}
