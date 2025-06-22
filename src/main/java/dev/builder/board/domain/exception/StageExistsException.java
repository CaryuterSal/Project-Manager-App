package dev.builder.board.domain.exception;

public class StageExistsException extends RuntimeException {
    public StageExistsException(String message) {
        super(message);
    }
}
