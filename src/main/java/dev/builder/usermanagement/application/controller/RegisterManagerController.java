package dev.builder.usermanagement.application.controller;

import dev.builder.core.application.RequestDispatcher;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.properties.MessageLocalizer;
import dev.builder.usermanagement.application.command.InviteManagerCommand;
import dev.builder.usermanagement.application.command.InviteUserCommand;
import dev.builder.usermanagement.application.query.FindUserQuery;
import dev.builder.usermanagement.application.view.UserView;
import dev.builder.usermanagement.domain.exception.UserExistsException;
import dev.builder.usermanagement.domain.model.User;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

@Bean
public class RegisterManagerController implements Initializable {
    private final RequestDispatcher requestDispatcher;
    private final MessageLocalizer messageLocalizer;
    private RegistrationStatus status;

    RegistrationStatus status() {
        return status;
    }

    enum RegistrationStatus {
        SUCCESS,
        CANCEL,
        FAIL
    }

    @FXML private TextField emailInput;
    @FXML private HBox errorPrompt;
    @FXML private Label errorField;
    @FXML private ProgressBar progressBar;
    @FXML private Button cancelBtn;
    @FXML private Button registerBtn;

    @Inject
    public RegisterManagerController(RequestDispatcher requestDispatcher, MessageLocalizer messageLocalizer) {
        this.requestDispatcher = requestDispatcher;
        this.messageLocalizer = messageLocalizer;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        registerBtn.setOnAction(this::registerManager);
        cancelBtn.setOnAction(this::cancel);
        emailInput.setOnKeyPressed(ev -> cleanErrors());
    }

    private void registerManager(ActionEvent aEv){
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(() -> cancelBtn.getScene().setCursor(Cursor.WAIT));
                cancelBtn.setDisable(false);
                registerBtn.setDisable(false);
                progressBar.setVisible(false);
                Optional<UserView> existingUser = requestDispatcher.dispatch(FindUserQuery.generic(emailInput.getText()));
                if(existingUser.isPresent()){
                    throw new UserExistsException(messageLocalizer);
                }
                requestDispatcher.dispatch(new InviteManagerCommand(emailInput.getText()));
                return null;
            }
        };
        task.setOnSucceeded(ev -> {

            Platform.runLater(() -> cancelBtn.getScene().setCursor(Cursor.DEFAULT));
            status = RegistrationStatus.SUCCESS;
            close();
        });

        task.setOnFailed(ev -> {

            Platform.runLater(() -> cancelBtn.getScene().setCursor(Cursor.DEFAULT));
            progressBar.setVisible(false);
            cancelBtn.setDisable(false);
            registerBtn.setDisable(false);
            progressBar.setVisible(false);
            emailInput.setStyle("-fx-border-color: red;");
            if(task.getException() instanceof ValidationException ex){
                errorPrompt.setVisible(true);
                ex.getMessageFor(InviteUserCommand.Fields.EMAIL.getValue())
                        .ifPresent(errorField::setText);
            } else if(task.getException() instanceof UserExistsException ex){
                errorPrompt.setVisible(true);
                errorField.setText(ex.getMessage());
            }else {
                task.getException().printStackTrace();
                status = RegistrationStatus.FAIL;
                close();
            }
        });
        errorPrompt.setVisible(false);
        errorField.setText("");
        cancelBtn.setDisable(true);
        registerBtn.setDisable(true);
        progressBar.setVisible(true);
        new Thread(task).start();

    }


    private void cleanErrors(){
        errorField.setText("");
        errorPrompt.setVisible(false);
        emailInput.setStyle("-fx-border-color: gray;");
    }

    private void cancel(ActionEvent aEv){
        status = RegistrationStatus.CANCEL;
        close();
    }

    private void close(){
        ((Stage) cancelBtn.getScene().getWindow()).close();
    }
}
