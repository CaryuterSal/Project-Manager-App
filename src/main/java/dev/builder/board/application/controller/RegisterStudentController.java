package dev.builder.board.application.controller;

import dev.builder.core.application.RequestDispatcher;
import dev.builder.core.application.validation.FieldViolationException;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.properties.MessageLocalizer;
import dev.builder.usermanagement.application.command.InviteManagerCommand;
import dev.builder.usermanagement.application.command.InviteStudentCommand;
import dev.builder.usermanagement.application.command.InviteUserCommand;
import dev.builder.usermanagement.application.controller.RegisterManagerController;
import dev.builder.usermanagement.application.query.FindUserQuery;
import dev.builder.usermanagement.application.view.UserView;
import dev.builder.usermanagement.domain.exception.UserExistsException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

@Bean
public class RegisterStudentController implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(RegisterStudentController.class);
    private final RequestDispatcher requestDispatcher;
    private final MessageLocalizer messageLocalizer;
    public TextField nameTxt;
    public Label nameErrLbl;
    public TextField lastNameTxt;
    public Label lastNameErrLbl;
    public Label quarterErrLbl;
    public Spinner<Integer> quarterSpinner;
    public TextField groupTxt;
    public Label groupErrLbl;
    public TextField emailTxt;
    public Label emailErrLbl;
    public Button cancelBtn;
    public Button registerBtn;
    public ProgressBar loadIndicator;
    private RegisterStudentController.RegistrationStatus status;

    RegisterStudentController.RegistrationStatus status() {
        return status;
    }

    enum RegistrationStatus {
        SUCCESS,
        CANCEL,
        FAIL
    }
    @Inject
    public RegisterStudentController(RequestDispatcher requestDispatcher, MessageLocalizer messageLocalizer) {
        this.requestDispatcher = requestDispatcher;
        this.messageLocalizer = messageLocalizer;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        registerBtn.setOnAction(this::registerManager);
        cancelBtn.setOnAction(this::cancel);
        quarterSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE));
        quarterSpinner.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                quarterSpinner.getEditor().setText(newValue.replaceAll("\\D", ""));
            }
            if (!quarterSpinner.getEditor().getText().isEmpty()) {
                int value = Integer.parseInt(quarterSpinner.getEditor().getText());
                if (value < 1) {
                    quarterSpinner.getEditor().setText("1");
                }
            }
        });
        emailTxt.setOnKeyPressed(event -> {
            emailErrLbl.setText("");
            emailErrLbl.setVisible(false);
            emailTxt.setStyle("");
        });
        groupTxt.setOnKeyPressed(event -> {
            groupErrLbl.setText("");
            groupErrLbl.setVisible(false);
            groupTxt.setStyle("");
        });
        lastNameTxt.setOnKeyPressed(event -> {
            lastNameErrLbl.setText("");
            lastNameErrLbl.setVisible(false);
            lastNameTxt.setStyle("");
        });
        nameTxt.setOnKeyPressed(event -> {
            nameErrLbl.setText("");
            nameErrLbl.setVisible(false);
            nameTxt.setStyle("");
        });
        quarterSpinner.setOnKeyPressed(event -> {
            quarterErrLbl.setText("");
            quarterErrLbl.setVisible(false);
            quarterSpinner.setStyle("");
        });

        groupTxt.setTextFormatter(new TextFormatter<String>(change -> {
            String newText = change.getControlNewText();
            if (newText.length() > 1) {
                return null;
            }
            if (!newText.matches("[a-zA-Z]?")) {
                return null;
            }
            return change;
        }));
        loadIndicator.setVisible(false);
        bindRegisterDisabled();
    }

    private void bindRegisterDisabled(){
        registerBtn.disableProperty().bind(
                nameTxt.textProperty().isEmpty()
                        .or(lastNameTxt.textProperty().isEmpty())
                        .or(emailTxt.textProperty().isEmpty())
                        .or(groupTxt.textProperty().isEmpty())
                        .or(quarterSpinner.valueProperty().isNull())
        );
    }

    private void waitToSubmit(boolean wait){
        if(wait){
            registerBtn.disableProperty().unbind();
            registerBtn.setDisable(true);
        } else {
            bindRegisterDisabled();
        }
        loadIndicator.setVisible(wait);
    }

    private void registerManager(ActionEvent aEv){
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                waitToSubmit(false);
                Optional<UserView> existingUser = requestDispatcher.dispatch(FindUserQuery.generic(emailTxt.getText()));
                if(existingUser.isPresent()){
                    throw new UserExistsException(messageLocalizer);
                }
                requestDispatcher.dispatch(new InviteStudentCommand(
                        emailTxt.getText(),
                        nameTxt.getText(),
                        lastNameTxt.getText(),
                        quarterSpinner.getValue(),
                        groupTxt.getText().charAt(0)));
                return null;
            }
        };
        task.setOnSucceeded(ev -> {
            status = RegisterStudentController.RegistrationStatus.SUCCESS;
            close();
        });

        task.setOnFailed(ev -> {
            waitToSubmit(false);
            if(task.getException() instanceof ValidationException ex){

                for(FieldViolationException fieldViolationException : ex.getViolations()){
                    if(fieldViolationException.getField().equals("firstName")){
                        nameErrLbl.setVisible(true);
                        nameErrLbl.setText(ex.getMessageFor("firstName").get());
                        nameTxt.setStyle("-fx-border-color: red;");
                    }
                    if(fieldViolationException.getField().equals("lastName")){
                        lastNameErrLbl.setVisible(true);
                        lastNameErrLbl.setText(ex.getMessageFor("lastName").get());
                        lastNameTxt.setStyle("-fx-border-color: red;");
                    }
                    if(fieldViolationException.getField().equals("academicGroup")){
                        groupErrLbl.setVisible(true);
                        groupErrLbl.setText(ex.getMessageFor("academicGroup").get());
                        groupTxt.setStyle("-fx-border-color: red;");
                    }
                    if(fieldViolationException.getField().equals("email")){
                        emailErrLbl.setVisible(true);
                        emailErrLbl.setText(ex.getMessageFor("email").get());
                        emailTxt.setStyle("-fx-border-color: red;");
                    }
                    if(fieldViolationException.getField().equals("academicQuarter")){
                        quarterErrLbl.setVisible(true);
                        quarterErrLbl.setText(ex.getMessageFor("academicQuarter").get());
                        quarterSpinner.setStyle("-fx-border-color: red;");
                    }
                }

            } else if(task.getException() instanceof UserExistsException ex){
                emailErrLbl.setVisible(true);
                emailErrLbl.setText(ex.getMessage());
                emailTxt.setStyle("-fx-border-color: red;");
            }else {
                log.warn(task.getException().getMessage(), task.getException());
                status = RegisterStudentController.RegistrationStatus.FAIL;
                close();
            }
        });
        new Thread(task).start();

    }

    private void cancel(ActionEvent aEv){
        status = RegisterStudentController.RegistrationStatus.CANCEL;
        close();
    }

    private void close(){
        ((Stage) cancelBtn.getScene().getWindow()).close();
    }
}
