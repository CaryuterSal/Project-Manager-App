module dev.builder {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires eu.hansolo.tilesfx;
    requires java.sql;
    requires jakarta.validation;
    requires org.jetbrains.annotations;

    opens dev.builder to javafx.fxml;
    opens dev.builder.auth.application.controller to javafx.fxml;
    opens dev.builder.usermanagement.application.controller to javafx.fxml;
    opens dev.builder.board.application.controller to javafx.fxml;
}