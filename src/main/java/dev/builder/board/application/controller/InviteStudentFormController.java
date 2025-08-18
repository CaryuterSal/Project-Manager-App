package dev.builder.board.application.controller;

import dev.builder.board.application.command.AddCollaboratorCommand;
import dev.builder.board.application.query.GetBoardQuery;
import dev.builder.board.application.view.CollaboratorView;
import dev.builder.core.application.RequestDispatcher;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.properties.MessageLocalizer;
import dev.builder.usermanagement.application.query.FindAllStudentsQuery;
import dev.builder.usermanagement.application.query.FindStudentQuery;
import dev.builder.usermanagement.application.view.StudentView;
import dev.builder.usermanagement.application.view.UserView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Bean
public class InviteStudentFormController implements Initializable {

    private final RequestDispatcher requestDispatcher;
    private final MessageLocalizer messageLocalizer;

    private final ObservableList<StudentView> studentSelectorData = FXCollections.observableArrayList();

    public Button registerBtn;
    public Button cancelBtn;
    public ComboBox<StudentView> studentSelector;
    public Button inviteBtn;
    public Label errorLbl;

    private InvitationStatus status;

    enum InvitationStatus {
        SUCCESS,
        CANCEL,
        FAIL,
        REGISTER
    }

    InvitationStatus status() {
        return status;
    }

    @Inject
    public InviteStudentFormController(RequestDispatcher requestDispatcher, MessageLocalizer messageLocalizer) {
        this.requestDispatcher = requestDispatcher;
        this.messageLocalizer = messageLocalizer;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        studentSelector.setItems(studentSelectorData);
        studentSelector.setConverter(new StringConverter<>() {
            @Override
            public String toString(StudentView studentView) {
                return studentView == null ? "" : studentView.email();
            }

            @Override
            public StudentView fromString(String email) {
               return null;
            }
        });
        inviteBtn.setOnAction(this::onInvite);
        cancelBtn.setOnAction(this::onCancel);
        cancelBtn.getScene().getWindow().setOnCloseRequest((event) -> {
            event.consume();
            close();
        });
        fillInvitationOptions();
    }

    private void cleanError(){
        errorLbl.setText("");
        errorLbl.setVisible(false);
        errorLbl.setManaged(false);
    }

    private void showError(String message){
        errorLbl.setText(message);
        errorLbl.setVisible(true);
        errorLbl.setManaged(true);
    }

    public void fillInvitationOptions(){
        Task<Set<StudentView>> task = new Task<>() {
            @Override
            protected Set<StudentView> call() throws Exception {
                toggleWait(true);
                Set<StudentView> allStudents = requestDispatcher.dispatch(FindAllStudentsQuery.builder().build())
                        .stream().filter(UserView::verified)
                        .collect(Collectors.toSet());
                Set<StudentView> collaboratingStudents = requestDispatcher.dispatch(GetBoardQuery.own()).get().collaborators()
                        .stream().map(CollaboratorView::student).collect(Collectors.toSet());
                Set<StudentView> nonCollaboratingStudents = new HashSet<>(allStudents);
                nonCollaboratingStudents.removeIf(collaboratingStudents::contains);
                return nonCollaboratingStudents;
            }
        };
        task.setOnSucceeded(event -> {
            toggleWait(false);
            studentSelectorData.setAll(task.getValue());
        });
        new Thread(task).start();
    }

    private void toggleWait(boolean wait){
        cancelBtn.setDisable(wait);
        inviteBtn.setDisable(wait);
        studentSelector.setDisable(wait);
    }
    private void onCancel(ActionEvent e){
        status = InvitationStatus.CANCEL;
        close();
    }

    private void onRegister(ActionEvent e){
        status = InvitationStatus.REGISTER;
        close();
    }

    private void close(){
        ((Stage) cancelBtn.getScene().getWindow()).close();
    }

    private void onInvite(ActionEvent e){
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                toggleWait(true);
                String selection = studentSelector.getValue().email();
                requestDispatcher.dispatch(new AddCollaboratorCommand(selection));
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            toggleWait(false);
            status = InvitationStatus.SUCCESS;
            close();
        });
        task.setOnFailed(event -> {
            status = InvitationStatus.FAIL;
            close();
        });
        new Thread(task).start();
    }

}
