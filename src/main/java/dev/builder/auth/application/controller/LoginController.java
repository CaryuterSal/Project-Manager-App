package dev.builder.auth.application.controller;

import dev.builder.MainApplication;
import dev.builder.auth.application.command.LoginCommand;
import dev.builder.auth.application.command.RestoreSessionCommand;
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
import dev.builder.usermanagement.application.command.CompleteRegistrationCommand;
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
    private PasswordField txtContra,txtContra2;
    @FXML
    private Button btnLogin,btnCancel;
    @FXML
    private Label labelError,labelTitle;

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
        try{
            boolean isRecovered = requestDispatcher.dispatch(new RestoreSessionCommand());
            if(isRecovered){
                loginAndRedirect();
            }
        }catch(Exception e){
            System.out.println("error"+e.getMessage());
        }

        btnLogin.setOnAction(this::validarCorreo);
        btnCancel.setOnAction(this::cancel);
        //btnLogin.setOnAction(this::navigate);
    }

    private void cancel(ActionEvent event){
        btnLogin.setText("Iniciar sesión");
        txtCorreo.setText("");
        txtContra.setText("");
        txtContra2.setText("");
        txtContra.setVisible(false);
        txtContra.setEditable(false);
        txtContra2.setVisible(false);
        txtContra2.setManaged(false);
        txtContra.setDisable(true);
        txtCorreo.setEditable(true);
        txtCorreo.setStyle("-fx-border-color: grey;");
        labelError.setText("");
        btnLogin.setOnAction(this::validarCorreo);
        labelTitle.setText("Bienvenido");
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
            loginAndRedirect();
        });
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            //Stage stage = (Stage) labelError.getScene().getWindow();
            //viewNavigation.navigate("admin-panel-view.fxml", stage);
            ex.printStackTrace();
            labelError.setText(ex.getMessage());
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
                btnLogin.setText("Registrarse");
                btnLogin.setOnAction(this::createPassword);
                labelTitle.setText("Crea una contraseña");
                txtCorreo.setStyle("-fx-background-color: #f7f7f7;");
                cleanErrors();
                mostrarCampo();
                txtContra.setVisible(true);
                txtContra2.setVisible(true);
                txtContra2.setManaged(true);
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
            btnLogin.setDisable(false);
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
    private void cleanErrors(){
        labelError.setText(" ");
        txtContra.setStyle("-fx-border-color: gray;");
        txtCorreo.setStyle("-fx-border-color: gray;");
        txtContra2.setStyle("-fx-border-color: gray;");
    }
    private void createPassword(ActionEvent event){
        cleanErrors();
        String correo = txtCorreo.getText();
        String pass1 = txtContra.getText();
        String pass2 = txtContra2.getText();

        if(pass1.isBlank() || pass2.isBlank()){
            labelError.setText("Todos los campos son obligatorios");
            txtContra.setStyle("-fx-border-color: red;");
            txtContra2.setStyle("-fx-border-color: red;");
            return;
        }
        if(!pass1.equals(pass2)){
            labelError.setText("Las contraseñas no son iguales");
            txtContra.setStyle("-fx-border-color: red;");
            txtContra2.setStyle("-fx-border-color: red;");
            return;
        }
        javafx.concurrent.Task<UserView> task = new javafx.concurrent.Task<>(){
            protected UserView call() throws Exception {
                return requestDispatcher.dispatch(new CompleteRegistrationCommand(correo,pass1));
            }
        };
        task.setOnSucceeded(e -> {
            loginAndRedirect();
        });
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            if(ex instanceof ValidationException ex2){
                ex2.getMessageFor("password");
                labelError.setText(ex2.getMessageFor("password").get());
            }
        });

        new Thread(task).start();


    }
    private void loginAndRedirect(){
        Stage stage = (Stage) labelError.getScene().getWindow();
        if(sessionContext.hasRole(Role.ADMIN)) {
            viewNavigation.navigate("admin-panel-view.fxml", stage);
        }else{
            viewNavigation.navigate("board-main-view.fxml", stage);
        }
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
