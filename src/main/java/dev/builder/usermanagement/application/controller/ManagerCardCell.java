package dev.builder.usermanagement.application.controller;

import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.di.annotation.Prototype;
import dev.builder.core.infrastructure.di.runtime.DependencyContainer;
import dev.builder.usermanagement.application.view.ManagerView;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;

import java.io.IOException;

@Prototype
public class ManagerCardCell extends ListCell<ManagerView> {

    private final DependencyContainer container;
    private final AdminDashboardController adminDashboardController;

    @Inject
    public ManagerCardCell(DependencyContainer container, AdminDashboardController adminDashboardController) {
        this.container = container;
        this.adminDashboardController = adminDashboardController;
    }

    @Override
    protected void updateItem(ManagerView item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/dev/builder/views/templates/admin-view-manager-card.fxml"));
                loader.setControllerFactory(container::getInstance);
                HBox root = loader.load();
                ManagerCardController controller = loader.getController();
                controller.setData(item);
                setGraphic(root);
                setOnKeyPressed(ev -> {
                    if(ev.getCode() == KeyCode.DELETE || ev.getCode() == KeyCode.BACK_SPACE){
                        controller.disableButton();
                        adminDashboardController.onDeleteManager(getItem());
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
