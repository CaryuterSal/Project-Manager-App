package dev.builder.board.application.service;

import dev.builder.board.application.query.LoadCoverImageQuery;
import dev.builder.board.application.query.LoadCoverImageQuery;
import dev.builder.board.domain.port.in.FileService;
import dev.builder.core.application.RequestHandler;
import dev.builder.core.application.validation.RequestValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;

import java.io.InputStream;
import java.util.Optional;

@Bean
public class LoadCoverImageQueryHandler implements RequestHandler<LoadCoverImageQuery, Optional<InputStream>> {
    private final LoadCoverImageQuery.LoadCoverImageQueryValidator validator;
    private final FileService fileService;

    public LoadCoverImageQueryHandler(FileService fileService, LoadCoverImageQuery.LoadCoverImageQueryValidator requestValidator) {
        this.validator = requestValidator;
        this.fileService = fileService;
    }

    @Override
    public Optional<InputStream> handle(LoadCoverImageQuery query) throws ValidationException {
        validator.validate(query);
        return fileService.openCoverImage(query);
    }
}
