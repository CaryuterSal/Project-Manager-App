package dev.builder.auth.application.controller;

import dev.builder.MainApplication;
import dev.builder.auth.domain.port.out.SessionContext;
import dev.builder.board.application.command.CreateTaskCommand;
import dev.builder.board.application.command.MoveTaskCommand;
import dev.builder.board.application.view.StageView;
import dev.builder.board.application.view.TaskView;
import dev.builder.board.domain.model.Color;
import dev.builder.board.domain.model.Stage;
//import javafx.stage.Stage;
import dev.builder.core.application.RequestDispatcher;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
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

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.ResourceBundle;

@Bean
public class LoginController implements Initializable {
    @FXML
    private TextField txtCorreo;
    @FXML
    private PasswordField txtContra;
    @FXML
    private Button btnLogin;
    @FXML
    private Label labelError;

    private final SessionContext sessionContext;
    private final RequestDispatcher requestDispatcher;

    @Inject
    public LoginController(SessionContext sessionContext, RequestDispatcher requestDispatcher) {
        this.sessionContext = sessionContext;
        this.requestDispatcher = requestDispatcher;
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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        btnLogin.setOnAction(this::validarCorreo);
        //btnLogin.setOnAction(this::navigate);
    }

    private void validarCorreo(ActionEvent event) {
        String correo = txtCorreo.getText();
        System.out.println("hola");
        if(correo.isBlank()){
            labelError.setText("el correo no debe estár vacío");
            return;
        }
        try{
            Optional<UserView> resultado = requestDispatcher.dispatch(FindUserQuery.generic(correo));
            if(resultado.isEmpty()){
                labelError.setText("el correo no existe");
                return;
            }
            UserView user = resultado.get();
            if(user.verified()){
                mostrarCampo();
            }else{

            }
        }catch(ValidationException e){
            System.out.println("Correo invalido "+ e.getMessage());
            labelError.setText("el correo no es valido");
        }catch (Exception e){
            System.out.println("Error al buscar"+ e.getMessage());
        }
    }
    private void mostrarCampo(){
        txtContra.setVisible(true);
        txtContra.setEditable(true);
        txtContra.setDisable(false);
        txtCorreo.setEditable(false);
    }
    /*private void navigate(ActionEvent actionEvent) {
        try {
            // Configura el FXMLLoader manualmente
            FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("/dev/builder/views/admin-panel-view.fxml"));

            // Si tu vista también tiene un controlador con dependencias, puedes hacer lo mismo que aquí.
            // loader.setControllerFactory(param -> new OtroController(...));

            Scene nuevaEscena = new Scene(loader.load(), 540, 420);

            // Obtiene el stage desde el botón
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(nuevaEscena);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            labelError.setText("No se pudo cargar la vista.");
        }
    }*/


}
