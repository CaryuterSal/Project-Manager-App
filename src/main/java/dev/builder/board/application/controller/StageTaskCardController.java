package dev.builder.board.application.controller;

import dev.builder.board.application.view.TaskView;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.di.annotation.Prototype;
import dev.builder.core.infrastructure.properties.MessageLocalizer;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

@Prototype
public class StageTaskCardController implements Initializable {

    private TaskView data;
    private
    public Label titleLbl;
    public HBox dueDateInfo;
    public HBox attachmentsInfo;
    public Label attachmentsLbl;
    public HBox assigneesInfo;
    public Label dueDateLbl;
    public Label assigneeLbl;
    public Label assigneesExtras;

    private final MessageLocalizer messageLocalizer;

    @Inject
    public StageTaskCardController(MessageLocalizer messageLocalizer) {
        this.messageLocalizer = messageLocalizer;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        titleLbl.setText(data.title());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        dueDateLbl.setText(formatter.format(data.deadline()));
        if(data.attachments().isEmpty()){
            attachmentsInfo.setVisible(false);
            attachmentsInfo.setManaged(false);
        } else {
            attachmentsInfo.setVisible(true);
            attachmentsInfo.setManaged(true);
            attachmentsLbl.setText(messageLocalizer.getMessage("task.attachment.label", data.attachments().size()));
        }

        if(data.assignees().isEmpty()){
            assigneesInfo.setVisible(false);
            assigneesInfo.setManaged(false);
        } else {
            assigneesInfo.setVisible(true);
            assigneesInfo.setManaged(true);
            assigneeLbl.setText(data.assignees().stream().findFirst().get().email());
            if(data.assignees().size() > 1){
                assigneesExtras.setVisible(true);
                assigneesExtras.setManaged(true);
                assigneesExtras.setText("+%d".formatted(data.assignees().size() - 1));
            } else {
                assigneesExtras.setVisible(false);
                assigneesExtras.setManaged(false);
            }
        }
    }

    public void setData(TaskView taskView) {
        data = taskView;
    }
}
