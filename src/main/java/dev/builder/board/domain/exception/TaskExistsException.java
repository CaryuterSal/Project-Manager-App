package dev.builder.board.domain.exception;

public class TaskExistsException extends RuntimeException {
    public TaskExistsException(String message) {
        super(message);
    }
}
