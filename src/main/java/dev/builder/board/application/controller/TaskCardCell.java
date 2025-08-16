package dev.builder.board.application.controller;

import dev.builder.board.application.view.TaskView;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.di.annotation.Prototype;
import dev.builder.core.infrastructure.di.runtime.DependencyContainer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ListCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Prototype
public class TaskCardCell extends ListCell<TaskView> {

    private static final Logger log = LoggerFactory.getLogger(TaskCardCell.class);
    private final DependencyContainer dependencyContainer;
    private BoardController boardController;

    @Inject
    public TaskCardCell(DependencyContainer dependencyContainer) {
        this.dependencyContainer = dependencyContainer;
    }


    @Override
    protected void updateItem(TaskView item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dev/builder/views/templates/task.fxml"));
            loader.setControllerFactory(dependencyContainer::getInstance);
            try{
                Parent root = loader.load();
                StageTaskCardController controller = loader.getController();
                controller.setData(getItem());
                controller.setBoardController(boardController);
                setGraphic(root);
            }catch(IOException e){
                log.warn(e.getMessage(),e);
            }
        }
    }

    public void setBoardController(BoardController boardController) {
        this.boardController = boardController;
    }
}
