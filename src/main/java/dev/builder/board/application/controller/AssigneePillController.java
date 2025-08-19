package dev.builder.board.application.controller;

import dev.builder.board.application.view.CollaboratorView;
import dev.builder.core.infrastructure.di.annotation.Prototype;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

@Prototype
public class AssigneePillController implements Initializable {

    @FXML private Button deleteBtn;
    @FXML private Label emailLbl;
    private Runnable onDelete;
    private String email;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        deleteBtn.setOnAction(event -> onDelete());
    }

    void setData(String email,  Runnable onDelete) {
        this.email = email;
        this.onDelete = onDelete;
        emailLbl.setText(email);
    }

    private void onDelete(){
        onDelete.run();
    }
}
