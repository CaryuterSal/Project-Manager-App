package dev.builder.core.application;

import javafx.geometry.Pos;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

public class ErrorHandler {

    public static void showError(String message){
        Notifications.create()
                .text(message)
                .hideAfter(Duration.seconds(3))
                .position(Pos.BOTTOM_CENTER)
                .showError();
    }
}
