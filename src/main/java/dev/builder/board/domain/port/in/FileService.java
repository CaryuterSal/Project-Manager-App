package dev.builder.board.domain.port.in;

import dev.builder.board.application.command.AddAttachmentToTaskCommand;
import dev.builder.board.application.command.AttachCoverImageToTaskCommand;
import dev.builder.board.application.command.RemoveAttachmentCommand;
import dev.builder.board.application.command.RemoveCoverImageCommand;
import dev.builder.board.application.query.LoadAttachmentQuery;
import dev.builder.board.application.query.LoadCoverImageQuery;
import dev.builder.board.application.view.FileView;

import java.io.InputStream;
import java.util.Optional;

public interface FileService {
    Optional<InputStream> openAttachment(LoadAttachmentQuery query);
    Optional<InputStream> openCoverImage(LoadCoverImageQuery query);
}
