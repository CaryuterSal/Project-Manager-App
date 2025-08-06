package dev.builder.auth.application.controller;

import dev.builder.auth.domain.port.out.SessionContext;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

@Bean
public class OtherController implements Initializable {

    private final SessionContext sessionContext;

    @FXML
    private Label currentUserLabel;

    @Inject
    public OtherController(SessionContext sessionContext) {
        this.sessionContext = sessionContext;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        currentUserLabel.setText("Hola: %s".formatted(sessionContext.getCurrentUser()));
    }
}
