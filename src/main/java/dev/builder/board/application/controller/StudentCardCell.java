package dev.builder.board.application.controller;

import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.di.annotation.Prototype;
import dev.builder.core.infrastructure.di.runtime.DependencyContainer;
import dev.builder.usermanagement.application.controller.AdminDashboardController;
import dev.builder.usermanagement.application.controller.ManagerCardController;
import dev.builder.usermanagement.application.view.ManagerView;
import dev.builder.usermanagement.application.view.StudentView;
import dev.builder.usermanagement.domain.model.Student;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;

import java.io.IOException;


@Prototype
public class StudentCardCell extends ListCell<StudentView> {
    private final DependencyContainer container;
    private final StudentDashboardController studentDashboardController;

    @Inject
    public StudentCardCell(DependencyContainer container, StudentDashboardController studentDashboardController) {
        this.container = container;
        this.studentDashboardController = studentDashboardController;
    }

    @Override
    protected void updateItem(StudentView item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/dev/builder/views/templates/manager-view-student-card.fxml"));
                loader.setControllerFactory(container::getInstance);
                HBox root = loader.load();
                StudentCardController controller = loader.getController();
                controller.setData(item);
                setGraphic(root);
                setOnKeyPressed(ev -> {
                    if(ev.getCode() == KeyCode.DELETE || ev.getCode() == KeyCode.BACK_SPACE){
                        controller.disableButton();
                        studentDashboardController.onDeleteManager(getItem());
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
