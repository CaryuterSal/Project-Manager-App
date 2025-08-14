package dev.builder.board.application.controller;

import dev.builder.board.application.controller.StudentDashboardController;
import dev.builder.core.application.ErrorHandler;
import dev.builder.core.application.RequestDispatcher;
import dev.builder.core.application.ViewNavigation;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.di.annotation.Prototype;
import dev.builder.core.infrastructure.di.runtime.DependencyContainer;
import dev.builder.usermanagement.application.command.DeleteUserCommand;
import dev.builder.usermanagement.application.view.ManagerView;
import dev.builder.usermanagement.application.view.StudentView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

@Prototype
public class StudentCardController implements Initializable {
    @FXML private Label email;
    @FXML private Button disableButton;

    private StudentView manager;
    private final StudentDashboardController parent;

    @Inject
    public StudentCardController(StudentDashboardController parent) {
        this.parent = parent;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        disableButton.setOnAction(ev -> parent.onDeleteManager(manager));
    }

    public void setData(StudentView manager) {
        this.manager = manager;
        this.email.setText(manager.email());
    }

    public void disableButton() {
        disableButton.setDisable(true);
    }
}
