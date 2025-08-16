package dev.builder.board.application.controller;

import dev.builder.auth.domain.port.out.SessionContext;
import dev.builder.board.application.view.StageView;
import dev.builder.core.application.RequestDispatcher;
import dev.builder.core.application.ViewNavigation;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.UUID;

@Bean
public class StudentBoardController implements Initializable, BoardController {

    private final SessionContext sessionContext;
    private final RequestDispatcher requestDispatcher;
    private final ViewNavigation viewNavigation;

    @Inject
    public StudentBoardController(SessionContext sessionContext, RequestDispatcher requestDispatcher, ViewNavigation viewNavigation) {
        this.sessionContext = sessionContext;
        this.requestDispatcher = requestDispatcher;
        this.viewNavigation = viewNavigation;
    }

    @Override
    public void onTaskCreated(StageView stage) {

    }

    @Override
    public void onTaskDeleted(UUID taskId) {

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
