package dev.builder.auth.application.controller;

import dev.builder.auth.application.command.LoginCommand;
import dev.builder.auth.domain.port.out.SessionContext;
import dev.builder.core.application.RequestDispatcher;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.di.annotation.PostConstruct;
import dev.builder.core.infrastructure.di.runtime.DependencyContainer;
import dev.builder.core.infrastructure.properties.MessageLocalizer;
import dev.builder.usermanagement.application.command.CompleteRegistrationCommand;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

@Bean
public class LoginController implements Initializable {

    private final MessageLocalizer messageLocalizer;
    private final SessionContext sessionContext;
    private final RequestDispatcher requestDispatcher;
    private final DependencyContainer dependencyContainer;

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;

    @Inject
    public LoginController(DependencyContainer dependencyContainer, MessageLocalizer messageLocalizer, SessionContext sessionContext, RequestDispatcher requestDispatcher) {
        this.dependencyContainer = dependencyContainer;
        this.messageLocalizer = messageLocalizer;
        this.sessionContext = sessionContext;
        this.requestDispatcher = requestDispatcher;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loginButton.setOnAction(this::login);
    }

    private void login(ActionEvent actionEvent) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        try {
            requestDispatcher.dispatch(new LoginCommand(username, password));
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/dev/builder/hello.fxml"));
            fxmlLoader.setControllerFactory(dependencyContainer::getInstance);
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(fxmlLoader.load());
            stage.setScene(scene);
            stage.show();
        } catch (ValidationException e) {
            LoggerFactory.getLogger(LoginController.class).info(e.getMessage());
            System.out.println(e.getViolations());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
