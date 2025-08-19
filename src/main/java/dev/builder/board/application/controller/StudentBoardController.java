package dev.builder.board.application.controller;

import dev.builder.auth.domain.port.out.SessionContext;
import dev.builder.auth.infrastructure.Role;
import dev.builder.board.application.query.GetBoardQuery;
import dev.builder.board.application.query.GetBoardsByCollaboratorCommand;
import dev.builder.board.application.view.BoardView;
import dev.builder.board.application.view.StageView;
import dev.builder.core.application.RequestDispatcher;
import dev.builder.core.application.ViewNavigation;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.di.runtime.DependencyContainer;
import dev.builder.usermanagement.application.query.FindAllManagersQuery;
import dev.builder.usermanagement.application.view.StudentView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;

@Bean
public class StudentBoardController extends BaseBoardContainerController implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(StudentBoardController.class);

    @FXML private  ListView<String> boardsList;

    private final Map<String, Node> boardsByManager = new HashMap<>();
    private final ObservableList<String> boardOwners =  FXCollections.observableArrayList();
    private final SessionContext sessionContext;
    private final RequestDispatcher requestDispatcher;
    private final ViewNavigation viewNavigation;
    private final DependencyContainer dependencyContainer;

    public StudentBoardController(SessionContext sessionContext, RequestDispatcher requestDispatcher, ViewNavigation viewNavigation, DependencyContainer dependencyContainer) {
        this.sessionContext = sessionContext;
        this.requestDispatcher = requestDispatcher;
        this.viewNavigation = viewNavigation;
        this.dependencyContainer = dependencyContainer;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (sessionContext.hasRole(Role.MANAGER)) {
            Stage stage = (Stage) boardsList.getScene().getWindow();
            viewNavigation.navigate("board-manager-view.fxml", stage);
            return;
        }
        loadCollaboratingInBoards();
        boardsList.setCellFactory(lv ->new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setGraphic(loadBoardNav(item));
                    if (isSelected()) {
                        setDisable(true);
                        getStyleClass().add("active");
                    } else {
                        getStyleClass().remove("active");
                        setDisable(false);
                    }
                }
            }
        });
        boardsList.setItems(boardOwners);
    }

    private void loadCollaboratingInBoards(){
        Task<List<BoardView>> task = new  Task<>() {
            @Override
            protected List<BoardView> call() throws Exception {
                return requestDispatcher.dispatch(GetBoardsByCollaboratorCommand.me());
            }
        };
        task.setOnSucceeded(event -> {
            boardOwners.setAll(task.getValue().stream()
                    .map(BoardView::owner)
                    .toList());

            for(String owner : boardOwners) {
                loadBoardView(owner);
            }
            navigateToBoardView(boardOwners.getFirst());
        });
        new Thread(task).start();
    }

    private Node loadBoardNav(String email){
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/dev/builder/views/templates/student_board_nav_button.fxml"));
        try {
            Button root = loader.load();
            root.setText(email);
            root.setOnAction(event -> navigateToBoardView(email));
            return root;
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    private void loadBoardView(String email){
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/dev/builder/views/board-view.fxml"));
        loader.setControllerFactory(dependencyContainer::getInstance);
        try {
            Parent root = loader.load();
            BoardController controller = loader.getController();
            controller.setOwnerEmail(email);
            boardsByManager.put(email, root);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void navigateToBoardView(String ownerEmail) {
        switchMainContent(boardsByManager.get(ownerEmail));
    }

}
