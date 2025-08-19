package dev.builder.board.application.controller;

import dev.builder.board.application.view.FileView;
import dev.builder.board.infrastructure.InputStreamBufferizer;
import dev.builder.core.application.ErrorHandler;
import dev.builder.core.infrastructure.di.annotation.Prototype;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.UUID;

@Prototype
public class AttachmentPillController implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(AttachmentPillController.class);

    @FXML private Label filenameLbl;
    @FXML private Label sizeLbl;
    @FXML private Button deleteBtn;
    @FXML private HBox clickableField;

    private File file;
    private String originalName;
    private Runnable onDelete;

    void setData(File file, String originalName, Runnable onDelete) {
        this.file = file;
        this.onDelete = onDelete;
        this.originalName = originalName;
        filenameLbl.setText(originalName);
        setSizeLabel();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        clickableField.setOnMouseClicked(event -> downloadAttachment(filenameLbl.getScene().getWindow()));
        deleteBtn.setOnAction(event -> onDetachAttachment());
    }

    private void setSizeLabel() {
        long sizeInBytes = file.length();

        String readableSize;
        if (sizeInBytes < 1024) {
            readableSize = sizeInBytes + " B";
        } else if (sizeInBytes < 1024 * 1024) {
            readableSize = (sizeInBytes / 1024) + " KB";
        } else {
            readableSize = (sizeInBytes / (1024 * 1024)) + " MB";
        }

        sizeLbl.setText("Tamaño: " + readableSize);
    }

    private void onDetachAttachment(){
        onDelete.run();
    }

    private void downloadAttachment(Window stage){
        try(InputStream stream = new FileInputStream(file)){
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar archivo adjunto");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("PDF", "*.pdf"),
                    new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
            );
            fileChooser.setInitialFileName(originalName);
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                try (FileOutputStream output = new FileOutputStream(file)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = stream.read(buffer)) != -1) {
                        output.write(buffer, 0, bytesRead);
                    }
                }
                log.info("Archivo guardado en: {}", file.getAbsolutePath());
            }
        } catch (IOException e) {
            ErrorHandler.showError("Hubo un error al descargar el archivo adjunto");
        }
    }
}
