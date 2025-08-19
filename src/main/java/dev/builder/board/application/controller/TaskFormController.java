package dev.builder.board.application.controller;

import dev.builder.auth.domain.port.out.SessionContext;
import dev.builder.auth.infrastructure.Role;
import dev.builder.board.application.command.*;
import dev.builder.board.application.query.GetBoardQuery;
import dev.builder.board.application.query.LoadAttachmentQuery;
import dev.builder.board.application.query.LoadCoverImageQuery;
import dev.builder.board.application.view.*;
import dev.builder.board.domain.model.Color;
import dev.builder.core.application.ErrorHandler;
import dev.builder.core.application.RequestDispatcher;
import dev.builder.core.application.validation.FieldViolationException;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.di.runtime.DependencyContainer;
import dev.builder.core.infrastructure.properties.MessageLocalizer;
import dev.builder.usermanagement.application.view.StudentView;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Bean
public class TaskFormController implements Initializable {


    private static final Logger log = LoggerFactory.getLogger(TaskFormController.class);
    private final RequestDispatcher requestDispatcher;
    public VBox descriptionVisualizerContainer;
    public WebView descriptionVisualizer;
    private  BoardController boardController;
    private final MessageLocalizer messageLocalizer;
    private final DependencyContainer dependencyContainer;
    private final SessionContext sessionContext;

    public Button addCoverImageBtn;
    public MenuButton colorSelector;
    public TextField txtTitle;
    public MenuButton btnMembers;
    public FlowPane collaboratorsPillContainer;
    public DatePicker dpEnd;
    public Button addAttachmentBtn;
    public HTMLEditor descriptionEditor;
    public Button btnCancel;
    public Button btnSave;
    public FlowPane attachmentPillContainer;
    public Label startedAt;
    public Label dueDateStatus;
    public Label finishedAt;
    public HBox dateEditData;
    public ProgressIndicator loadIndicator;
    public StackPane coverImageContainer;
    public Circle colorCircle;

    private TaskView task;
    private ObjectProperty<Color> selectedColor = new SimpleObjectProperty<>();
    private File selectedCover;
    private final List<IdentifiableFile> selectedAttachments = new ArrayList<>();
    private final Map<UUID, Node> selectedAttachmentsPills = new HashMap<>();
    private final Map<String, Node> selectedAssigneesPills = new HashMap<>();

    record IdentifiableFile(UUID id, File file){
    }

    @Inject
    public TaskFormController(RequestDispatcher requestDispatcher, MessageLocalizer messageLocalizer, DependencyContainer dependencyContainer, SessionContext sessionContext) {
        this.requestDispatcher = requestDispatcher;
        this.messageLocalizer = messageLocalizer;
        this.dependencyContainer = dependencyContainer;
        this.sessionContext = sessionContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadIndicator.setVisible(false);
        btnCancel.setOnAction(e -> close());
        btnSave.setOnAction(e -> onAddTask());
        btnSave.disableProperty().bind(
                txtTitle.textProperty().isEmpty()
                        .or(dpEnd.valueProperty().isNull())
                        .or(selectedColor.isNull())
        );
        loadMembers();
        loadColors();
        addCoverImageBtn.setOnAction(ev -> changeCoverImage());
        addAttachmentBtn.setOnAction(this::onAddAttachment);
        txtTitle.setOnKeyTyped(ev -> txtTitle.setStyle(""));
        descriptionEditor.setOnKeyTyped(ev -> descriptionEditor.setStyle(""));
        dpEnd.setOnAction(ev -> dpEnd.setStyle(""));
        if(sessionContext.hasRole(Role.STUDENT)){
            descriptionEditor.setVisible(false);
            descriptionEditor.setManaged(false);
            txtTitle.setEditable(false);
            dpEnd.setEditable(false);
            colorSelector.setDisable(true);
            addAttachmentBtn.setVisible(false);
            addAttachmentBtn.setManaged(false);
            addCoverImageBtn.setVisible(false);
            addCoverImageBtn.setManaged(false);
            btnMembers.setVisible(false);
            btnMembers.setManaged(false);
            btnSave.setVisible(false);
            btnSave.setManaged(false);
        } else {
            descriptionEditor.setVisible(true);
            descriptionEditor.setManaged(true);
            descriptionVisualizerContainer.setVisible(false);
            descriptionVisualizerContainer.setManaged(false);
        }
    }


    public void setTask(TaskView task) {
        this.task = task;
        if (task != null) {
            txtTitle.setText(task.title());
            if(sessionContext.hasRole(Role.MANAGER)) {
                descriptionEditor.setHtmlText(task.description());
            } else {
                WebEngine engine =  descriptionVisualizer.getEngine();
                engine.loadContent(task.description(), "text/html");
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
            task.startedAt().ifPresent(s -> startedAt.setText(s.toLocalDate().format(formatter)));
            task.finishedAt().ifPresent(s -> finishedAt.setText(s.toLocalDate().format(formatter)));
            dpEnd.setValue(task.deadline().toLocalDate());
            colorCircle.setFill(javafx.scene.paint.Color.web(task.color().hexCode().value()));
            selectedColor.set(task.color());
            task.coverImage().ifPresent(coverImage -> openCoverImage(coverImage.id()));
            loadAttachments();
            loadAssignees();
            if(task.deadline().isBefore(LocalDateTime.now())) {
                dueDateStatus.setText("Con Retraso");
                dueDateStatus.setStyle("-fx-text-fill: #9b1b1b;");
            } else {
                dueDateStatus.setText("En Tiempo");
                dueDateStatus.setStyle("-fx-text-fill: #1f851f;");
            }
        }
        btnSave.setOnAction(e -> onEditTask());
    }
    
    private void loadAttachments(){
        try(ExecutorService executorService = Executors.newFixedThreadPool(4)) {
            for (FileView attachment : task.attachments()) {
                Task<Optional<InputStream>> task = new Task<Optional<InputStream>>() {
                    @Override
                    protected Optional<InputStream> call() throws Exception {
                        return requestDispatcher.dispatch(new LoadAttachmentQuery(attachment.id()));
                    }
                };
                task.setOnSucceeded(e -> {
                    if(task.getValue().isPresent()) {
                        try {
                            addAttachmentPill(inputStreamToTempFile(task.getValue().get(), attachment.name()), attachment.name());
                        } catch (IOException ex) {
                            log.warn(ex.getMessage(), ex);
                        }
                    }
                });
                executorService.submit(task);
            }
        }
    }
    
    private void loadAssignees(){
        for(StudentView assignee : task.assignees()) {
            addCollaboratorPill(assignee.email());
        }
    }

    private void waitAndLoad(boolean wait){
        if(wait){
            btnSave.disableProperty().unbind();
            btnSave.setDisable(true);
        } else {
            btnSave.disableProperty().bind(
                    txtTitle.textProperty().isEmpty()
                            .or(dpEnd.valueProperty().isNull())
                            .or(selectedColor.isNull())
            );
        }
        btnMembers.setDisable(wait);
        addAttachmentBtn.setDisable(wait);
        addCoverImageBtn.setDisable(wait);
        loadIndicator.setVisible(wait);
        loadIndicator.setManaged(wait);
    }

    private void loadMembers(){
        Task<BoardView> task = new Task<>() {
            @Override
            protected BoardView call() throws Exception {
                return requestDispatcher.dispatch(GetBoardQuery.own()).orElseThrow();
            }
        };
        task.setOnSucceeded(ev -> {
            for(CollaboratorView collaborator: task.getValue().collaborators()){
                MenuItem item = new MenuItem(collaborator.student().email());
                item.setOnAction(e -> onAddCollaborator(collaborator.student().email()));
                btnMembers.getItems().add(item);
            }
        });
        task.setOnFailed(ev -> {
            ErrorHandler.showError("Hubo un error al cargar la información");
        });
        new Thread(task).start();
    }

    private void loadColors(){
        for(Color color : Color.values()) {
            MenuItem menuItem = new MenuItem(messageLocalizer.getMessage("color.%s".formatted(color.toString().toLowerCase())));
            menuItem.setOnAction(ev -> {
                selectedColor.set(color);
                colorCircle.setFill(javafx.scene.paint.Color.web(color.hexCode().value()));
            });
            colorSelector.getItems().add(menuItem);
        }
    }

    private void onSaveAllAttachments(CountDownLatch latch, UUID taskId){
        try (ExecutorService executorService = Executors.newFixedThreadPool(4)) {

            int total = selectedAttachments.size();
            CountDownLatch internalLatch = new CountDownLatch(total);
            for(File attachment : selectedAttachments.stream().map(IdentifiableFile::file).toList()) {
               Task<FileView> addTask = onSaveAttachment(attachment, taskId);
               addTask.setOnSucceeded(event -> internalLatch.countDown());
               addTask.setOnFailed(event -> internalLatch.countDown());
               executorService.submit(addTask);
            }
            new Thread(() -> {
                try {
                    internalLatch.await();
                    latch.countDown();
                } catch (InterruptedException ex) {
                    log.info(ex.getMessage(),ex);
                }
            }).start();
        }
    }

    private void onAddAttachment(ActionEvent  actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Agregar archivo adjunto");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF", "*.pdf"),
                new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
        );
        File file = fileChooser.showOpenDialog(btnSave.getScene().getWindow());
        addAttachmentPill(file, file.getName());
    }

    private Task<FileView> onSaveAttachment(File attachment, UUID taskId){
        Task<FileView> addTask = new Task<>() {
            @Override
            protected FileView call() throws Exception {
                try(InputStream stream = new FileInputStream(attachment)){
                    return requestDispatcher.dispatch(new AddAttachmentToTaskCommand(taskId,attachment.getName(),stream));
                }
            }
        };
        return addTask;
    }

    private File inputStreamToTempFile(InputStream inputStream, String originalName) throws IOException {
        String suffix = "";
        int dot = originalName.lastIndexOf('.');
        if (dot > 0) {
            suffix = originalName.substring(dot);
        }

        File tempFile = File.createTempFile(UUID.randomUUID().toString(), suffix);
        tempFile.deleteOnExit();

        try (OutputStream out = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }

        return tempFile;
    }

    private void addAttachmentPill(File attachment, String originalFilename){
        try {

            UUID tempId = UUID.randomUUID();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dev/builder/views/templates/attachment-pill.fxml"));
            loader.setControllerFactory(dependencyContainer::getInstance);
            Parent root = loader.load();

            AttachmentPillController controller = loader.getController();
            controller.setData(attachment, originalFilename, () -> onDeleteAttachment(tempId));
            attachmentPillContainer.getChildren().add(root);
            selectedAttachmentsPills.put(tempId, root);
            selectedAttachments.add(new IdentifiableFile(tempId, attachment));
        } catch (IOException e){
            log.warn(e.getMessage(), e);
        }
    }

    private void onDeleteAttachment(UUID attachmentId){
        attachmentPillContainer.getChildren().remove(selectedAttachmentsPills.get(attachmentId));
    }

    private Task<Void> onPersistDeleteAttachment(UUID attachmentId){
        Task<Void> deleteTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                requestDispatcher.dispatch(new RemoveAttachmentCommand(task.id(),attachmentId));
                return null;
            }
        };
        return deleteTask;
    }

    private void onReplaceAttachments(CountDownLatch latch){
        try(ExecutorService executorService = Executors.newFixedThreadPool(4)) {
            Set<UUID> existingAttachments = task.attachments().stream().map(FileView::id).collect(Collectors.toSet());
            Set<IdentifiableFile> missingAttachments = new HashSet<>(selectedAttachments);
            missingAttachments.removeIf(e -> !existingAttachments.contains(e.id));

            Set<UUID> removedAttachments = new HashSet<>(existingAttachments);
            removedAttachments.removeAll(selectedAttachments.stream().map(IdentifiableFile::id).collect(Collectors.toSet()));

            int total = missingAttachments.size() + removedAttachments.size();
            CountDownLatch internalLatch = new CountDownLatch(total);

            for (IdentifiableFile attachment : missingAttachments) {
                Task<FileView> saveTask = onSaveAttachment(attachment.file, task.id());
                saveTask.setOnSucceeded(event -> internalLatch.countDown());
                saveTask.setOnFailed(event -> internalLatch.countDown());
                executorService.submit(saveTask);
            }

            for(UUID attachment : removedAttachments) {
                Task<Void> deleteTask = onPersistDeleteAttachment(attachment);
                deleteTask.setOnSucceeded(event -> internalLatch.countDown());
                deleteTask.setOnFailed(event -> internalLatch.countDown());
                executorService.submit(deleteTask);
            }

            new Thread(() -> {
                try {
                    internalLatch.await();
                    latch.countDown();
                } catch (InterruptedException ex) {
                    log.info(ex.getMessage(), ex);
                }
            }).start();
        }
    }

    private void onSaveCollaborators(CountDownLatch latch, UUID taskId){
        try (ExecutorService executorService = Executors.newFixedThreadPool(4)) {
            int total = selectedAssigneesPills.size();
            CountDownLatch internalLatch = new CountDownLatch(total);
            for(String assignees : selectedAssigneesPills.keySet()) {
                Task<Void> saveTask = onSaveCollaborator(assignees, taskId);
                saveTask.setOnSucceeded(event -> internalLatch.countDown());
                saveTask.setOnFailed(event -> internalLatch.countDown());
                executorService.submit(saveTask);
            }

            new Thread(() -> {
                try {
                    internalLatch.await();
                    latch.countDown();
                } catch (InterruptedException ex) {
                    log.info(ex.getMessage(), ex);
                }
            }).start();
        }
    }

    private void onAddCollaborator(String email){
        btnMembers.getItems().removeIf(i -> i.getText().equals(email));
        addCollaboratorPill(email);
    }

    private void addCollaboratorPill(String email){
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dev/builder/views/templates/assignee-pill.fxml"));
            loader.setControllerFactory(dependencyContainer::getInstance);
            Parent root = loader.load();

            AssigneePillController controller = loader.getController();
            controller.setData(email, () -> onDeleteCollaborator(email));
            collaboratorsPillContainer.getChildren().add(root);
            selectedAssigneesPills.put(email, root);
        } catch (IOException e){
            log.warn(e.getMessage(), e);
        }
    }

    private void onDeleteCollaborator(String email){
        collaboratorsPillContainer.getChildren().remove(selectedAssigneesPills.get(email));
    }

    private Task<Void> onSaveCollaborator(String email, UUID taskId){
        Task<Void> assignTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                requestDispatcher.dispatch(new AssignStudentToTaskCommand(taskId,email));
                return null;
            }
        };
        return assignTask;
    }

    private Task<Void> onPersistDeleteCollaborator(String email, ExecutorService service){
        Task<Void> deleteTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                requestDispatcher.dispatch(new RevokeAssignmentFromTaskCommand(task.id(),email));
                return null;
            }
        };
        return deleteTask;
    }

    private void onReplaceCollaborators(CountDownLatch latch){
        try(ExecutorService executorService = Executors.newFixedThreadPool(4)) {
            Set<String> existingAssignees = task.assignees().stream().map(StudentView::email).collect(Collectors.toSet());
            Set<String> missingAssignees = new HashSet<>(selectedAssigneesPills.keySet());
            missingAssignees.removeAll(existingAssignees);

            Set<String> removedAssignees = new HashSet<>(existingAssignees);
            removedAssignees.removeAll(selectedAssigneesPills.keySet());
            int total =  missingAssignees.size() + removedAssignees.size();
            CountDownLatch internalLatch = new CountDownLatch(total);

            for(String email : removedAssignees) {
                Task<Void> deleteTask = onPersistDeleteCollaborator(email, executorService);
                deleteTask.setOnFailed(event -> internalLatch.countDown());
                deleteTask.setOnSucceeded(event -> internalLatch.countDown());
                executorService.submit(deleteTask);
            }

            for (String email : missingAssignees) {
                Task<Void> saveTask = onSaveCollaborator(email, task.id());

                saveTask.setOnFailed(event -> internalLatch.countDown());
                saveTask.setOnSucceeded(event -> internalLatch.countDown());
                executorService.submit(saveTask);
            }

            new Thread(() -> {
                try {
                    internalLatch.await();
                    latch.countDown();
                } catch (InterruptedException ex) {
                    log.info(ex.getMessage(), ex);
                }
            }).start();
        }

    }

    private void changeCoverImage(Image image){
        BackgroundImage backgroundImage =  new BackgroundImage(
                image,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(
                        BackgroundSize.AUTO, BackgroundSize.AUTO,
                        true, true, false, true
                ));
        coverImageContainer.setBackground(new Background(backgroundImage));
    }

    private void changeCoverImage(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Agregar imagen de carátula");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("png", "*.png"),
                new FileChooser.ExtensionFilter("jpg", "*.jpg"),
                new FileChooser.ExtensionFilter("jpeg", "*.jpeg")
        );
        selectedCover = fileChooser.showOpenDialog(btnSave.getScene().getWindow());
        Image image = new Image(selectedCover.toURI().toString());
        changeCoverImage(image);
    }

    private void openCoverImage(UUID imageId){
        Task<Optional<InputStream>> task = new Task<>() {
            @Override
            protected Optional<InputStream> call() throws Exception {
                return  requestDispatcher.dispatch(new LoadCoverImageQuery(imageId));
            }
        };

        task.setOnSucceeded(event -> {
            if(task.getValue().isPresent()){
                try(InputStream stream = task.getValue().get()){
                    changeCoverImage(new Image(stream));
                } catch (IOException e) {
                    log.warn(e.getMessage(), e);
                    ErrorHandler.showError("No se pudo cargar la imagen de carátula");
                }
            }
        });

        new Thread(task).start();
    }

    private void onPersistCoverImage(CountDownLatch latch, UUID taskId){

        Task<Void> attachTask = new  Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try(InputStream stream = new FileInputStream(selectedCover)){
                    requestDispatcher.dispatch(new AttachCoverImageToTaskCommand(taskId, selectedCover.getName(), stream));
                }
                return null;
            }
        };
        attachTask.setOnFailed(event -> latch.countDown());
        attachTask.setOnSucceeded(event -> latch.countDown());
        new Thread(attachTask).start();
    }

    private void onReplaceCoverImage(CountDownLatch latch){
        if(task.coverImage().isPresent()) {
            Task<Void> deleteCoverTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    requestDispatcher.dispatch(new RemoveCoverImageCommand(task.id()));
                    return null;
                }
            };
            deleteCoverTask.setOnSucceeded(e -> onPersistCoverImage(latch, task.id()));
            new Thread(deleteCoverTask).start();
        } else {
            onPersistCoverImage(latch, task.id());
        }
    }

    private void onAddTask() {

        waitAndLoad(true);
        Task<StageView> creationTask = new Task<>() {
            @Override
            protected StageView call() throws Exception {
                String title = txtTitle.getText();
                LocalDateTime due = dpEnd == null ? null : dpEnd.getValue().atStartOfDay();
                String desc = descriptionEditor.getHtmlText();

                CreateTaskCommand cmd = new CreateTaskCommand(
                        dev.builder.board.domain.model.Stage.StageState.TO_DO,
                        title,
                        desc,
                        selectedColor.get(),
                        due
                );
                return requestDispatcher. dispatch(cmd);
            }
        };

        creationTask.setOnSucceeded(e -> {
            StageView updatedStage = creationTask.getValue();
            TaskView createdTask = updatedStage.tasks().getLast();
            CountDownLatch latch = new CountDownLatch(3);
            onSaveAllAttachments(latch, createdTask.id());
            onSaveCollaborators(latch, createdTask.id());
            onPersistCoverImage(latch, createdTask.id());
            new Thread(() -> {
                try{
                    latch.await();
                    Platform.runLater(() -> {
                        boardController.onTaskCreated(updatedStage);
                        waitAndLoad(false);
                        close();
                    });
                } catch (InterruptedException ex) {
                    log.info(ex.getMessage(), ex);
                }
            }).start();
        });
        creationTask.setOnFailed(e -> {
            waitAndLoad(false);
            Throwable thrown = creationTask.getException();
            if(thrown instanceof ValidationException validationException) {
                for(FieldViolationException fieldViolationException : validationException.getViolations()) {
                    if(fieldViolationException.getField().equals("title")){
                        txtTitle.setStyle("-fx-text-fill: red; -fx-border-color: red; -fx-border-width: 1;");
                    }
                    if(fieldViolationException.getField().equals("description")){
                        descriptionEditor.setStyle("-fx-text-fill: red; -fx-border-color: red; -fx-border-width: 1;");
                    }
                    if(fieldViolationException.getField().equals("deadline")){
                        dpEnd.setStyle("-fx-text-fill: red; -fx-border-color: red; -fx-border-width: 1;");
                    }
                }
            }
        });
        new Thread(creationTask).start();
    }

    private void onEditTask(){
        Task<TaskView> editTask = new Task<>() {
            @Override
            protected TaskView call() throws Exception {
                String title = txtTitle.getText();
                LocalDateTime due = dpEnd.getValue() == null ? null : dpEnd.getValue().atStartOfDay();
                String desc = descriptionEditor.getHtmlText();

                EditTaskCommand cmd = new EditTaskCommand(
                        task.id(),
                        title,
                        desc,
                        selectedColor.get(),
                        due
                );

                waitAndLoad(true);
                return requestDispatcher.dispatch(cmd);
            }
        };

        editTask.setOnSucceeded(e -> {
            CountDownLatch latch = new CountDownLatch(3);
            onReplaceAttachments(latch);
            onReplaceCollaborators(latch);
            onReplaceCoverImage(latch);
            new Thread(() -> {
                try{
                    latch.await();
                    Platform.runLater(() -> {
                        waitAndLoad(false);
                        close();
                    });
                } catch (InterruptedException ex) {
                    log.info(ex.getMessage(), ex);
                }
            }).start();
        });
        editTask.setOnFailed(e -> {
            Throwable thrown = editTask.getException();
            if(thrown instanceof ValidationException validationException) {
                for(FieldViolationException fieldViolationException : validationException.getViolations()) {
                    if(fieldViolationException.getField().equals("title")){
                        txtTitle.setStyle("-fx-text-fill: red; -fx-border-color: red; -fx-border-width: 1;");
                    }
                    if(fieldViolationException.getField().equals("description")){
                        descriptionEditor.setStyle("-fx-text-fill: red; -fx-border-color: red; -fx-border-width: 1;");
                    }
                    if(fieldViolationException.getField().equals("deadline")){
                        dpEnd.setStyle("-fx-text-fill: red; -fx-border-color: red; -fx-border-width: 1;");
                    }
                }
            }
        });
        new Thread(editTask).start();
    }

    private void close(){
        ((Stage)txtTitle.getScene().getWindow()).close();
    }

    void setBoardController(BoardController boardController) {
        this.boardController = boardController;
    }
}
