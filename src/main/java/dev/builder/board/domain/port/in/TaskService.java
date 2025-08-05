package dev.builder.board.domain.port.in;

import dev.builder.board.application.command.*;
import dev.builder.board.application.query.GetTaskQuery;
import dev.builder.board.application.view.FileView;
import dev.builder.board.application.view.TaskView;

import java.util.Optional;

public interface TaskService {
    TaskView editTask(EditTaskCommand command);
    TaskView assignStudent(AssignStudentToTaskCommand command);
    void removeAssignedStudent(RevokeAssignmentFromTaskCommand command);
    FileView attachCoverImage(AttachCoverImageToTaskCommand command);
    FileView addAttachment(AddAttachmentToTaskCommand command);
    void removeAttachment(RemoveAttachmentCommand command);
    void deleteCoverImage(RemoveCoverImageCommand command);
    Optional<TaskView> getTask(GetTaskQuery query);
}
