package dev.builder.board.application.controller;

import dev.builder.auth.application.command.LogoutCommand;
import dev.builder.auth.domain.port.out.SessionContext;
import dev.builder.core.application.ErrorHandler;
import dev.builder.core.application.RequestDispatcher;
import dev.builder.core.application.ViewNavigation;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.runtime.DependencyContainer;
import dev.builder.usermanagement.application.command.DeleteUserCommand;
import dev.builder.usermanagement.application.controller.ManagerCardCell;
import dev.builder.usermanagement.application.controller.RegisterManagerController;
import dev.builder.usermanagement.application.query.FindAllManagersQuery;
import dev.builder.usermanagement.application.query.FindAllStudentsQuery;
import dev.builder.usermanagement.application.view.ManagerView;
import dev.builder.usermanagement.application.view.StudentView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@Bean
public class StudentDashboardController implements Initializable {
    private final RequestDispatcher requestDispatcher;
    private final SessionContext sessionContext;
    private final DependencyContainer dependencyContainer;
    private final ViewNavigation viewNavigation;
    private final RegisterStudentController  registerStudentController;

    @FXML
    private Button clearSearch;
    @FXML private Button logoutBtn;
    @FXML private Button reloadButton;
    @FXML private ProgressIndicator searchProgressIndicator;
    @FXML private Button addManagerBtn;
    @FXML private Label usernameLabel;
    @FXML private Button noManagerAddBtn;
    @FXML private Button searchButton;
    @FXML private TextField searchText;
    @FXML private Label noManagerLabel;
    @FXML private ProgressIndicator loadListIndicator;
    @FXML private ListView<StudentView> studentsList;

    private final ObservableList<StudentView> students = FXCollections.observableList(new ArrayList<>());

    public StudentDashboardController(RequestDispatcher requestDispatcher, SessionContext sessionContext, DependencyContainer dependencyContainer, ViewNavigation viewNavigation, RegisterStudentController registerStudentController) {
        this.requestDispatcher = requestDispatcher;
        this.sessionContext = sessionContext;
        this.dependencyContainer = dependencyContainer;
        this.viewNavigation = viewNavigation;
        this.registerStudentController = registerStudentController;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        studentsList.setItems(students);
        studentsList.setCellFactory(lv -> dependencyContainer.getInstance(StudentCardCell.class));
        usernameLabel.setText(sessionContext.getCurrentUser());
        searchButton.setOnAction(event -> search());
        searchText.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER) search();
        });
        noManagerAddBtn.setOnAction(this::openManagerRegistration);
        addManagerBtn.setOnAction(this::openManagerRegistration);
        logoutBtn.setOnAction(this::logout);
        reloadButton.setOnAction(event -> viewStudents());
        clearSearch.setOnAction(ev -> {
            searchText.clear();
            search();
        });

        viewStudents();

    }

    private void viewReload(){

        searchButton.setDisable(false);
        noManagerAddBtn.setDisable(false);
        reloadButton.setDisable(false);
        addManagerBtn.setDisable(false);

        noManagerLabel.setVisible(true);
        noManagerLabel.setManaged(true);
        reloadButton.setVisible(true);
        reloadButton.setManaged(true);
        noManagerLabel.setText("Hubo un error al cargar la información, por favor recarga");

        studentsList.setVisible(false);
        studentsList.setManaged(false);
        noManagerAddBtn.setVisible(false);
        noManagerAddBtn.setManaged(false);
        loadListIndicator.setVisible(false);
        loadListIndicator.setManaged(false);
    }

    private void visualizeTable(boolean visualize){

        noManagerAddBtn.setVisible(!visualize);
        noManagerAddBtn.setManaged(!visualize);
        noManagerLabel.setVisible(!visualize);
        noManagerLabel.setManaged(!visualize);
        reloadButton.setVisible(false);
        reloadButton.setManaged(false);
        loadListIndicator.setVisible(false);
        loadListIndicator.setManaged(false);
        noManagerLabel.setText("Aún no hay maestros aquí");

        studentsList.setVisible(visualize);
        studentsList.setManaged(visualize);
    }

    private void viewLoading(){
        noManagerLabel.setVisible(false);
        noManagerLabel.setManaged(false);
        noManagerAddBtn.setVisible(false);
        noManagerAddBtn.setManaged(false);
        reloadButton.setVisible(false);
        reloadButton.setManaged(false);
        studentsList.setVisible(false);
        studentsList.setManaged(false);

        loadListIndicator.setVisible(true);
        loadListIndicator.setManaged(true);
    }

    private void viewNoManagerFound(){

        searchButton.setDisable(false);
        noManagerAddBtn.setDisable(false);
        reloadButton.setDisable(false);
        addManagerBtn.setDisable(false);

        noManagerLabel.setVisible(true);
        noManagerLabel.setManaged(true);
        noManagerLabel.setText("No se encontraron resultados");

        studentsList.setVisible(false);
        studentsList.setManaged(false);
        noManagerAddBtn.setVisible(false);
        noManagerAddBtn.setManaged(false);
        loadListIndicator.setVisible(false);
        loadListIndicator.setManaged(false);
        reloadButton.setVisible(false);
        reloadButton.setManaged(false);
    }

    private void showDeleteConfirmAlert(){

    }

    private void search(){
        String emailContaining = searchText.getText();
        javafx.concurrent.Task<List<StudentView>> task = new Task<>(){
            @Override
            protected List<StudentView> call() throws Exception {
                Platform.runLater(() -> addManagerBtn.getScene().setCursor(Cursor.WAIT));
                return requestDispatcher.dispatch(FindAllStudentsQuery.builder()
                        .emailLike(emailContaining)
                        .build());
            }
        };
        task.setOnSucceeded(event -> {

            Platform.runLater(() -> addManagerBtn.getScene().setCursor(Cursor.DEFAULT));
            searchButton.setDisable(false);
            noManagerAddBtn.setDisable(false);
            reloadButton.setDisable(false);
            addManagerBtn.setDisable(false);
            students.setAll(task.getValue());
            searchProgressIndicator.setVisible(false);
            if(students.isEmpty()){
                viewNoManagerFound();
            } else {
                visualizeTable(true);
            }
        });

        searchButton.setDisable(true);
        noManagerAddBtn.setDisable(true);
        reloadButton.setDisable(true);
        addManagerBtn.setDisable(true);
        task.setOnFailed(event -> viewReload());
        searchProgressIndicator.setVisible(true);
        new Thread(task).start();
    }

    private void viewStudents(){
        javafx.concurrent.Task<List<StudentView>> task = new Task<>(){
            @Override
            protected List<StudentView> call() throws Exception {
                Platform.runLater(() -> addManagerBtn.getScene().setCursor(Cursor.WAIT));
                return requestDispatcher.dispatch(FindAllStudentsQuery.builder()
                        .createdBy(sessionContext.getCurrentUser())
                        .build());
            }
        };
        task.setOnSucceeded(event -> {
            Platform.runLater(() -> addManagerBtn.getScene().setCursor(Cursor.DEFAULT));
            searchButton.setDisable(false);
            noManagerAddBtn.setDisable(false);
            reloadButton.setDisable(false);
            addManagerBtn.setDisable(false);
            students.setAll(task.getValue());
            visualizeTable(!students.isEmpty());
        });
        task.setOnFailed(event -> viewReload());

        searchButton.setDisable(true);
        noManagerAddBtn.setDisable(true);
        reloadButton.setDisable(true);
        addManagerBtn.setDisable(true);
        if(students.isEmpty()){
            viewLoading();
        }
        new Thread(task).start();
    }

    private void openManagerRegistration(ActionEvent aEv){
        viewNavigation.openModal("add-student-form.fxml", StageStyle.UTILITY);
        RegisterStudentController.RegistrationStatus status = registerStudentController.status();
        switch (status){
            case FAIL -> viewNavigation.openModal("add-student-error-modal.fxml", StageStyle.DECORATED);
            case SUCCESS -> {
                viewNavigation.openModal("add-student-successful-modal.fxml", StageStyle.DECORATED);
                viewStudents();
            }
        }
    }

    private void logout(ActionEvent aEv){
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                requestDispatcher.dispatch(new LogoutCommand());
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            viewNavigation.navigate("hello-view.fxml", (Stage) usernameLabel.getScene().getWindow());
        });

        searchButton.setDisable(true);
        noManagerAddBtn.setDisable(true);
        reloadButton.setDisable(true);
        addManagerBtn.setDisable(true);
        logoutBtn.setDisable(true);
        new Thread(task).start();
    }

    void onDeleteManager(StudentView student){
        boolean delete = viewNavigation.showConfirmationDialog(
                "Eliminar estudiante",
                "¿Estás seguro que quieres eliminar al estudiante de correo %s? Esta acción no se puede deshaces".formatted(student.email()));
        if(delete) {
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    requestDispatcher.dispatch(new DeleteUserCommand(student.email()));
                    return null;
                }
            };
            task.setOnSucceeded(event -> {
                students.remove(student);
            });
            task.setOnFailed(event -> {
                ErrorHandler.showError("Hubo un error al eliminar el maestro");
            });
            new Thread(task).start();
        }
    }
}
