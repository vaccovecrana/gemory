package io.vacco.gemory;

import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import static io.vacco.gemory.GmContext.*;

public class GmStyleEditor extends StackPane {

  public GmStyleEditor(Pane pane) {

    pane.getStyleClass().add("root");

    getChildren().add(root(r -> r.borderPane((bp, ctx) -> {
      bp.setTop(root(lr -> lr.hBox((hb, hbc) -> {
        TextField cssUrl = new TextField();
        cssUrl.setPromptText("Input CSS URL");
        hbc.node(cssUrl);
        hbc.button(bt -> {
          bt.setText("Apply");
          bt.setOnAction(e -> {
            pane.getStylesheets().clear();
            pane.getStylesheets().add(cssUrl.getText());
          });
        });
      })));
      bp.setCenter(pane);
    })));
  }

}
