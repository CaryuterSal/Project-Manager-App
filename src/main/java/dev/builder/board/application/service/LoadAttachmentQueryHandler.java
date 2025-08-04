package dev.builder.board.application.service;

import dev.builder.board.application.query.LoadAttachmentQuery;
import dev.builder.board.application.query.LoadAttachmentQuery;
import dev.builder.board.application.view.BoardView;
import dev.builder.board.domain.port.in.FileService;
import dev.builder.core.application.RequestHandler;
import dev.builder.core.application.validation.RequestValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;

import java.io.InputStream;
import java.util.Optional;

@Bean
public class LoadAttachmentQueryHandler implements RequestHandler<LoadAttachmentQuery, Optional<InputStream>> {
    private final RequestValidator<LoadAttachmentQuery> validator;
    private final FileService fileService;

    public LoadAttachmentQueryHandler(FileService fileService, RequestValidator<LoadAttachmentQuery> requestValidator) {
        this.validator = requestValidator;
        this.fileService = fileService;
    }

    @Override
    public Optional<InputStream> handle(LoadAttachmentQuery query) throws ValidationException {
        validator.validate(query);
        return fileService.openAttachment(query);
    }
}
