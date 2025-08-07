package dev.builder.usermanagement.application.controller;

import dev.builder.core.application.ErrorHandler;
import dev.builder.core.application.RequestDispatcher;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.di.annotation.Prototype;
import dev.builder.usermanagement.application.command.DeleteUserCommand;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

@Prototype
public class ManagerCardController implements Initializable {

    private final AdminDashboardController adminDashboardController;
    private final RequestDispatcher requestDispatcher;

    @Inject
    public ManagerCardController(AdminDashboardController adminDashboardController, RequestDispatcher requestDispatcher) {
        this.adminDashboardController = adminDashboardController;
        this.requestDispatcher = requestDispatcher;
    }

    @FXML private Label email;
    @FXML private Button disableButton;

    void setEmail(String email) {
        this.email.setText(email);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        disableButton.setOnAction(this::onClickDisableManager);
    }

    private void onClickDisableManager(ActionEvent event) {
        try {
            requestDispatcher.dispatch(new DeleteUserCommand(email.getText()));
            adminDashboardController.onDeleteManager(email.getText());
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            ErrorHandler.showError("Hubo un error al eliminar el maestro");
        }
    }
}
