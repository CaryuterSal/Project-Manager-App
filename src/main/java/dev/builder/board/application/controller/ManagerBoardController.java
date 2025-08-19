package dev.builder.board.application.controller;

import dev.builder.auth.domain.port.out.SessionContext;
import dev.builder.auth.infrastructure.Role;
import dev.builder.core.application.RequestDispatcher;
import dev.builder.core.application.ViewNavigation;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.runtime.DependencyContainer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;

@Bean
public class ManagerBoardController extends BaseBoardContainerController implements Initializable {


    private static final Logger log = LoggerFactory.getLogger(ManagerBoardController.class);
    @FXML
    private Button btnAccount;
    @FXML
    private Button btnBoard;

    private BoardController boardController;
    private Node boardNodeRoot;
    private StudentDashboardController studentDashboardController;
    private Node studentDashboardNodeRoot;

    private final SessionContext sessionContext;
    private final RequestDispatcher requestDispatcher;
    private final ViewNavigation viewNavigation;
    private final DependencyContainer dependencyContainer;

    public ManagerBoardController(SessionContext sessionContext, RequestDispatcher requestDispatcher, ViewNavigation viewNavigation, DependencyContainer dependencyContainer) {
        this.sessionContext = sessionContext;
        this.requestDispatcher = requestDispatcher;
        this.viewNavigation = viewNavigation;
        this.dependencyContainer = dependencyContainer;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (sessionContext.hasRole(Role.STUDENT)) {
            Stage stage = (Stage) btnAccount.getScene().getWindow();
            viewNavigation.navigate("board-student-view.fxml", stage);
            return;
        }
        btnAccount.setOnAction(this::navigateToStudentDashboard);
        btnBoard.setOnAction(this::navigateToBoardView);
        loadBoardController();
        loadStudentDashboardController();
        navigateToBoardView(null);
    }

    private void loadBoardController(){
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/dev/builder/views/board-view.fxml"));
        loader.setControllerFactory(dependencyContainer::getInstance);
        try {
            boardNodeRoot = loader.load();
            boardController = loader.getController();
            boardController.setOnClickRegisterStudent(() -> navigateToBoardView(null));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void loadStudentDashboardController(){
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/dev/builder/views/manager-account-view.fxml"));
        loader.setControllerFactory(dependencyContainer::getInstance);
        try {
            studentDashboardNodeRoot = loader.load();
            studentDashboardController = loader.getController();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }


    private void navigateToBoardView(ActionEvent actionEvent) {
        btnAccount.getStyleClass().remove("active");
        btnBoard.getStyleClass().add("active");
        btnAccount.setDisable(false);
        btnBoard.setDisable(true);
        switchMainContent(boardNodeRoot);
    }

    private void navigateToStudentDashboard(ActionEvent actionEvent) {
        btnAccount.getStyleClass().add("active");
        btnBoard.getStyleClass().remove("active");
        btnBoard.setDisable(false);
        btnAccount.setDisable(true);
        switchMainContent(studentDashboardNodeRoot);
    }

}
