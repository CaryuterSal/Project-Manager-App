package dev.builder.auth.application.controller;

import dev.builder.MainApplication;
import dev.builder.auth.application.command.LoginCommand;
import dev.builder.auth.application.service.UnauthorizedException;
import dev.builder.auth.domain.port.out.SessionContext;
import dev.builder.auth.infrastructure.Role;
import dev.builder.board.application.command.CreateTaskCommand;
import dev.builder.board.application.command.MoveTaskCommand;
import dev.builder.board.application.view.StageView;
import dev.builder.board.application.view.TaskView;
import dev.builder.board.domain.model.Color;
//import javafx.stage.Stage;
import dev.builder.core.application.RequestDispatcher;
import dev.builder.core.application.ViewNavigation;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.di.runtime.DependencyContainer;
import dev.builder.usermanagement.application.command.InviteManagerCommand;
import dev.builder.usermanagement.application.query.FindUserQuery;
import dev.builder.usermanagement.application.view.UserView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Bean
public class LoginController implements Initializable {
    @FXML
    private TextField txtCorreo;
    @FXML
    private PasswordField txtContra;
    @FXML
    private Button btnLogin,btnCancel;
    @FXML
    private Label labelError;

    private final SessionContext sessionContext;
    private final RequestDispatcher requestDispatcher;
    private final ViewNavigation viewNavigation;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();


    @Inject
    public LoginController(SessionContext sessionContext, RequestDispatcher requestDispatcher, ViewNavigation viewNavigation) {
        this.sessionContext = sessionContext;
        this.requestDispatcher = requestDispatcher;
        this.viewNavigation = viewNavigation;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        btnLogin.setOnAction(this::validarCorreo);
        btnCancel.setOnAction(this::cancel);
        //btnLogin.setOnAction(this::navigate);
    }

    private void cancel(ActionEvent event){
        txtCorreo.setText("");
        txtContra.setText("");
        txtContra.setVisible(false);
        txtContra.setEditable(false);
        txtContra.setDisable(true);
        txtCorreo.setEditable(true);
        txtCorreo.setStyle("-fx-border-color: grey;");
        labelError.setText("");
        btnLogin.setOnAction(this::validarCorreo);
    }
    private void validarContra(ActionEvent event) {
        String contra = txtContra.getText();
        String correo = txtCorreo.getText();

        btnLogin.setDisable(true);
        txtContra.setStyle("-fx-border-color: grey;");
        labelError.setText("");

        if(contra.isBlank()){
            labelError.setText("La contraseña no debe estár vacía");
            txtContra.setStyle("-fx-border-color: red;");
            btnLogin.setDisable(false);
            return;
        }

        LoginCommand loginCommand = new LoginCommand(correo,contra);

        javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<>(){
            protected Void call() throws Exception{
                requestDispatcher.dispatch(loginCommand);
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            Stage stage = (Stage) labelError.getScene().getWindow();
            if(sessionContext.hasRole(Role.ADMIN)) {
                viewNavigation.navigate("admin-panel-view.fxml", stage);
            }else{
                viewNavigation.navigate("board-main-view.fxml", stage);
            }

        });
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            Stage stage = (Stage) labelError.getScene().getWindow();
            viewNavigation.navigate("admin-panel-view.fxml", stage);
            ex.printStackTrace();
            if(ex instanceof ValidationException ve){
                labelError.setText(ve.getMessage());
            }else if(ex instanceof UnauthorizedException ue){
                labelError.setText(ue.getMessage());
            }else{
                labelError.setText("HUBO UN ERROR");
            }
            txtContra.setStyle("-fx-border-color: red;");
            btnLogin.setDisable(false);
        });
        executor.submit(task);
    }

    private void validarCorreo(ActionEvent event) {
        btnLogin.setDisable(true);
        String correo = txtCorreo.getText();
        if(correo.isBlank()){
            labelError.setText("El correo no debe esetar vacío");
            txtCorreo.setStyle("-fx-border-color: red;");
            btnLogin.setDisable(false);
            return;
        }
        javafx.concurrent.Task<Optional<UserView>> task = new javafx.concurrent.Task<>(){
            protected Optional<UserView> call() throws Exception {
                return requestDispatcher.dispatch(FindUserQuery.generic((correo)));
            }
        };
        task.setOnSucceeded(e -> {
            Optional<UserView> resultado = task.getValue();
            if(resultado.isEmpty()){
                labelError.setText("El correo no existe");
                txtCorreo.setStyle("-fx-border-color: red;");
                btnLogin.setDisable(false);
                return;
            }
            UserView user = resultado.get();
            if(user.verified()){
                mostrarCampo();
                btnLogin.setOnAction(this::validarContra);
                txtCorreo.setStyle("-fx-background-color: #f7f7f7;");
            }else{
                labelError.setText("El correo no ha sido verificado");
                txtCorreo.setStyle("-fx-border-color: red;");
                btnLogin.setDisable(false);
            }
        });
        task.setOnFailed(workerStateEvent -> {
            Throwable e = task.getException();
            if (e instanceof ValidationException) {
                labelError.setText("El correo no es válido");
            } else {
                labelError.setText("Error al buscar el correo");
            }
            txtCorreo.setStyle("-fx-border-color: red;");
            btnLogin.setDisable(false); // Reactiva en cualquier error
        });

        new Thread(task).start();
    }

    private void mostrarCampo(){
        labelError.setText(" ");
        txtContra.setVisible(true);
        txtContra.setEditable(true);
        txtContra.setDisable(false);
        txtCorreo.setEditable(false);
        btnLogin.setDisable(false);
        //txtCorreo.setDisable(true);
    }

    /*public void clickButton() {
        try {
            TaskView response = requestDispatcher.dispatch(new CreateTaskCommand(Stage.StageState.IN_PROGRESS, "tasdf", "fsdffsfd", "Pink", LocalDateTime.now().plusDays(10)));
            requestDispatcher.dispatch(MoveTaskCommand.builder()
                            .task(response.id())
                            .toStage(Stage.StageState.DONE)
                            .placeAtStart()
                    );
        } catch (ValidationException e) {

        }
    }*/


}
