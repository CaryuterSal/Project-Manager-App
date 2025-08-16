package dev.builder.board.application.controller;

import dev.builder.board.application.view.StageView;

import java.util.UUID;

public interface BoardController {
    void onTaskCreated(StageView stage);
    void onTaskDeleted(UUID taskId);
}
