package dev.builder.board.application.controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;


public class BaseBoardContainerController {

    @FXML
    private GridPane baseGrid;

    protected Node getOldPaneContent(){
        Node oldContent = null;
        for (Node child : baseGrid.getChildren()) {
            Integer col = GridPane.getColumnIndex(child);
            if (col != null && col == 1) {
                oldContent = child;
                break;
            }
        }
        return oldContent;
    }

    protected void switchMainContent(Node newContent){
        Node oldContent = getOldPaneContent();
        if(oldContent != null){
            baseGrid.getChildren().remove(oldContent);
        }
        baseGrid.add(newContent, 1, 0);
    }




}
