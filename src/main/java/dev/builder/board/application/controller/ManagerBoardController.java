package dev.builder.board.application.controller;

import dev.builder.auth.application.command.LogoutCommand;
import dev.builder.auth.domain.port.out.SessionContext;
import dev.builder.auth.infrastructure.Role;
import dev.builder.board.application.command.MoveTaskCommand;
import dev.builder.board.application.query.GetBoardQuery;
import dev.builder.board.application.view.BoardView;
import dev.builder.board.application.view.StageView;
import dev.builder.board.application.view.TaskView;
import dev.builder.board.domain.model.Stage.StageState;
import dev.builder.core.application.ErrorHandler;
import dev.builder.core.application.RequestDispatcher;
import dev.builder.core.application.ViewNavigation;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.di.runtime.DependencyContainer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Bean
public class ManagerBoardController implements Initializable, BoardController {

    @FXML private MenuItem logoutBtn;
    @FXML private ProgressIndicator searchProgressIndicator;
    @FXML private Button delSearchBtn;
    @FXML private Button searchBtn;
    @FXML private Button btnAccount;
    @FXML private Button btnBoard;
    @FXML private TextField txtSearch;
    @FXML private Button btnInvite;
    @FXML private ListView<TaskView> todoList;
    @FXML private ListView<TaskView> inProgressList;
    @FXML private ListView<TaskView> doneList;
    @FXML private Button btnAddTask;
    @FXML private Label lblError;

    private final SessionContext sessionContext;
    private final RequestDispatcher requestDispatcher;
    private final ViewNavigation viewNavigation;
    private final DependencyContainer dependencyContainer;
    private final InviteStudentFormController inviteStudentFormController;
    private final ObservableList<TaskView> toDoTasks = FXCollections.observableArrayList();
    private final ObservableList<TaskView> inProgressTasks = FXCollections.observableArrayList();
    private final ObservableList<TaskView> doneTasks = FXCollections.observableArrayList();

    @Inject
    public ManagerBoardController(SessionContext sessionContext,
                                  RequestDispatcher requestDispatcher,
                                  ViewNavigation viewNavigation, DependencyContainer dependencyContainer, InviteStudentFormController inviteStudentFormController) {
        this.sessionContext = sessionContext;
        this.requestDispatcher = requestDispatcher;
        this.viewNavigation = viewNavigation;
        this.dependencyContainer = dependencyContainer;
        this.inviteStudentFormController = inviteStudentFormController;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (sessionContext.hasRole(Role.STUDENT)) {
            Stage stage = (Stage) lblError.getScene().getWindow();
            viewNavigation.navigate("board-student-view.fxml", stage);
            return;
        } else if(sessionContext.hasRole(Role.ADMIN)) {
            lblError.setText("No tienes permisos para acceder a este panel.");
        }

        btnAddTask.setOnAction(e -> onAddTaskClicked());

        todoList.setCellFactory(lv -> {
            TaskCardCell cell = dependencyContainer.getInstance(TaskCardCell.class);
            cell.setBoardController(this);
            return cell;
        });
        inProgressList.setCellFactory(lv -> {
            TaskCardCell cell = dependencyContainer.getInstance(TaskCardCell.class);
            cell.setBoardController(this);
            return cell;
        });
        doneList.setCellFactory(lv -> {
            TaskCardCell cell = dependencyContainer.getInstance(TaskCardCell.class);
            cell.setBoardController(this);
            return cell;
        });
        todoList.setItems(toDoTasks);
        inProgressList.setItems(inProgressTasks);
        doneList.setItems(doneTasks);
        btnAccount.setOnAction(this::onAccountClicked);
        txtSearch.setOnKeyPressed(ev -> {
            if(ev.getCode() == KeyCode.ENTER) {
                onSearch(null);
            }
        });
        searchBtn.setOnAction(this::onSearch);
        delSearchBtn.setOnAction(ev -> {
                txtSearch.setText("");
                onSearch(null);
        });
        btnInvite.setOnAction(this::onInviteStudentClicked);
        btnAccount.setOnAction(this::onAccountClicked);
        todoList.setOnDragOver(ev -> onDragOverStage(todoList, ev));
        todoList.setOnDragDropped(ev -> onDragDroppedStage(todoList, ev));
        inProgressList.setOnDragOver(ev -> onDragOverStage(inProgressList, ev));
        inProgressList.setOnDragDropped(ev -> onDragDroppedStage(inProgressList, ev));
        doneList.setOnDragOver(ev -> onDragOverStage(doneList, ev));
        doneList.setOnDragDropped(ev -> onDragDroppedStage(doneList, ev));

        logoutBtn.setOnAction(this::onClickLogout);

        updateBoard();
    }

    private void onDragOverStage(ListView<TaskView> source, DragEvent ev){
        if (ev.getGestureSource() != source && ev.getDragboard().hasString()) {
            ev.acceptTransferModes(TransferMode.MOVE);
        }
        ev.consume();
    }

    private void onDragDroppedStage(ListView<TaskView> source, DragEvent ev){
        Dragboard db = ev.getDragboard();
        boolean success = false;
        if (db.hasString()) {
            UUID taskId = UUID.fromString(db.getString());

            int dropIndex = getDropIndex(source, ev.getY());

            UUID prevTaskId = null;
            UUID nextTaskId = null;
            if (dropIndex > 0) {
                prevTaskId = source.getItems().get(dropIndex - 1).id();
            }
            if (dropIndex < source.getItems().size()) {
                nextTaskId = source.getItems().get(dropIndex).id();
            }

            StageState newStage = getStageFromListView(source); // mapeo a TODO, IN_PROGRESS, etc.

            MoveTaskCommand command;
            if (prevTaskId != null) {
                command = MoveTaskCommand.builder()
                        .task(taskId)
                        .toStage(newStage)
                        .placeAfter(prevTaskId)
                        .build();
            } else if (nextTaskId != null) {
                command = MoveTaskCommand.builder()
                        .task(taskId)
                        .toStage(newStage)
                        .placeBefore(nextTaskId)
                        .build();
            } else {
                command = MoveTaskCommand.builder()
                        .task(taskId)
                        .toStage(newStage)
                        .placeAtStart();
            }

            Task<BoardView> task = new Task<>() {
                @Override
                protected BoardView call() throws Exception {
                    toggleWaitForAction(true);
                    return requestDispatcher.dispatch(command);
                }
            };

            task.setOnSucceeded(e -> {
                toggleWaitForAction(false);
                updateWithData(task.getValue());
            });
            new Thread(task).start();

            success = true;
        }
        ev.setDropCompleted(success);
        ev.consume();
    }

    private StageState getStageFromListView(ListView<TaskView> source) {
        if(source == todoList){
            return StageState.TO_DO;
        } else if(source == inProgressList){
            return StageState.IN_PROGRESS;
        }else {
            return StageState.DONE;
        }
    }


    private void toggleWaitForAction(boolean wait){
        searchProgressIndicator.setVisible(wait);
        searchProgressIndicator.setManaged(wait);
        btnInvite.setDisable(wait);
        btnAddTask.setDisable(wait);
        delSearchBtn.setDisable(wait);
        searchBtn.setDisable(wait);
        todoList.setDisable(wait);
        inProgressList.setDisable(wait);
        doneList.setDisable(wait);
    }

    private void updateBoard(){
        Task<Optional<BoardView>> task = new Task<>() {
            @Override
            protected Optional<BoardView> call() throws Exception {
                toggleWaitForAction(true);
                return requestDispatcher.dispatch(GetBoardQuery.own());
            }
        };

        task.setOnSucceeded(event -> {
            if(task.getValue().isPresent()) {
                toggleWaitForAction(false);
                updateWithData(task.getValue().get());
            } else {
                ErrorHandler.showError("Esta cuenta no existe");
                onClickLogout(null);
            }
        });
        task.setOnFailed(event -> {
            ErrorHandler.showError("Esta cuenta no existe");
            onClickLogout(null);
        });
        new Thread(task).start();
    }

    private void updateWithData(BoardView boardView){
        List<StageView> stages = boardView.stages();
        Map<StageState, List<TaskView>> tasksByStage = boardView.stages().stream()
                .collect(Collectors.toMap(StageView::state, StageView::tasks));
        toDoTasks.setAll(tasksByStage.get(StageState.TO_DO));
        inProgressTasks.setAll(tasksByStage.get(StageState.IN_PROGRESS));
        doneTasks.setAll(tasksByStage.get(StageState.DONE));
    }

    private void onClickLogout(ActionEvent e){
        Task<Void> task = new Task<>(){
            @Override
            protected Void call() throws Exception {
                requestDispatcher.dispatch(new LogoutCommand());
                return null;
            }
        };
        task.setOnSucceeded(ev -> {
            viewNavigation.navigate("hello-view.fxml", (Stage) lblError.getScene().getWindow());
        });
        new Thread(task).start();
    }

    private void onSearch(ActionEvent actionEvent) {
        String searchInput = txtSearch.getText();
        updateBoard();
        filterTasksBy(toDoTasks, searchInput);
        filterTasksBy(inProgressTasks, searchInput);
        filterTasksBy(doneTasks, searchInput);
    }

    private void filterTasksBy(@NotNull ObservableList<TaskView> container, String input){
        container.setAll(container.stream()
                .filter(task -> task.title().contains(input) || task.description().contains(input))
                .toList());
    }

    private void onAddTaskClicked() {
        viewNavigation.openModal("create-task-view.fxml", StageStyle.UTILITY);
    }

    public void onTaskCreated(StageView stage) {
        toDoTasks.setAll(stage.tasks());
    }

    @Override
    public void onTaskDeleted(UUID taskId) {
        toDoTasks.removeIf(t -> t.id().equals(taskId));
        inProgressTasks.removeIf(t -> t.id().equals(taskId));
        doneTasks.removeIf(t -> t.id().equals(taskId));
    }

    private void onAccountClicked(ActionEvent actionEvent) {
        viewNavigation.navigate("manager-account-view.fxml",(Stage) btnAccount.getScene().getWindow());
    }

    private void onInviteStudentClicked(ActionEvent actionEvent){
        viewNavigation.openModal("invite-student-form-view.fxml",StageStyle.UTILITY);
        handleInvitationStatus(inviteStudentFormController.status());
    }

    private void handleInvitationStatus(InviteStudentFormController.@NotNull InvitationStatus status){
        switch (status){
            case FAIL: ErrorHandler.showError("Hubo un error, intenta de nuevo");
                break;
            case REGISTER: onAccountClicked(null);
                break;
            default:
        }
    }

    private int getDropIndex(ListView<TaskView> listView, double sceneY) {
        for (var node : listView.lookupAll(".list-cell")) {
            if (node instanceof ListCell<?> cell && !cell.isEmpty()) {
                Bounds bounds = cell.localToScene(cell.getBoundsInLocal());
                if (sceneY < bounds.getMinY() + bounds.getHeight() / 2) {
                    return cell.getIndex();
                }
            }
        }
        return listView.getItems().size();
    }


}
