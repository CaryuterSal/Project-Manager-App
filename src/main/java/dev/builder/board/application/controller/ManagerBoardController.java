package dev.builder.board.application.controller;

import dev.builder.auth.domain.model.Role;
import dev.builder.auth.domain.model.User;
import dev.builder.auth.domain.port.out.SessionContext;
import dev.builder.board.application.command.GetManagerBoardCommand;
import dev.builder.board.application.view.BoardView;
import dev.builder.board.application.view.StageView;
import dev.builder.board.application.view.TaskView;
import dev.builder.core.application.RequestDispatcher;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.application.ViewNavigation;
import dev.builder.core.application.validation.ValidationException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

@Bean
public class ManagerBoardController implements Initializable {

    @FXML
    private TextField txtSearch;

    @FXML
    private Button btnSearch, btnBoard, btnAccount, btnInvite;

    @FXML
    private Label lblError;

    @FXML
    private VBox boardContainer;

    private final SessionContext sessionContext;
    private final RequestDispatcher requestDispatcher;
    private final ViewNavigation viewNavigation;

    @Inject
    public ManagerBoardController(SessionContext sessionContext, RequestDispatcher requestDispatcher, ViewNavigation viewNavigation) {
        this.sessionContext = sessionContext;
        this.requestDispatcher = requestDispatcher;
        this.viewNavigation = viewNavigation;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User currentUser = sessionContext.activeUser();

        if (currentUser == null || !currentUser.hasRole(Role.MANAGER)) {
            lblError.setText("Acceso denegado: solo usuarios MANAGER.");
            return;
        }

        btnSearch.setOnAction(this::buscar);
        btnBoard.setOnAction(this::cargarTablero);
        btnInvite.setOnAction(event -> viewNavigation.navigate("invite-student-form-view.fxml"));

        cargarTablero(null);
    }

    private void cargarTablero(ActionEvent event) {
        lblError.setText("");

        Task<BoardView> task = new Task<>() {
            @Override
            protected BoardView call() throws Exception {
                return requestDispatcher.dispatch(new GetManagerBoardCommand());
            }
        };

        task.setOnSucceeded(e -> {
            BoardView board = task.getValue();
            mostrarTablero(board);
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            if (ex instanceof ValidationException ve) {
                lblError.setText(ve.getMessage());
            } else {
                lblError.setText("Error al cargar el tablero.");
            }
            ex.printStackTrace();
        });

        new Thread(task).start();
    }

    private void mostrarTablero(BoardView board) {
        boardContainer.getChildren().clear();

        if (board.stages().isEmpty()) {
            boardContainer.getChildren().add(new Label("No hay etapas disponibles."));
            return;
        }

        for (StageView stage : board.stages()) {
            VBox etapaBox = new VBox();
            etapaBox.setStyle("-fx-border-color: black; -fx-padding: 10; -fx-spacing: 5;");
            etapaBox.getChildren().add(new Label("Etapa: " + stage.name()));

            for (TaskView task : stage.tasks()) {
                VBox taskBox = new VBox();
                taskBox.setStyle("-fx-border-color: #ccc; -fx-background-color: #fff; -fx-padding: 5;");
                taskBox.setSpacing(2);

                Text titulo = new Text("Título: " + task.title());
                Text fecha = new Text("Vence: " + (task.dueDate() != null ? task.dueDate().toString() : "Sin fecha"));

                taskBox.getChildren().addAll(titulo, fecha);
                etapaBox.getChildren().add(taskBox);
            }

            boardContainer.getChildren().add(etapaBox);
        }
    }

    private void buscar(ActionEvent event) {
        String texto = txtSearch.getText().trim();
        if (texto.isBlank()) {
            lblError.setText("Ingrese un término para buscar.");
            return;
        }

        lblError.setText("Función de búsqueda no implementada aún.");
    }
}
