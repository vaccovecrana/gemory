package io.vacco.gemory.util;

import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import static io.vacco.gemory.core.GmContext.*;

public class GmStyleEditor extends StackPane {

  public GmStyleEditor(Pane pane) {

    pane.getStyleClass().add("root");

    getChildren().add(root($ -> $.borderPane((bp, bp$) -> {
      bp$.top(t$ -> t$.vBox((vb, vb$) -> {
        vb$.hBox((hb, hb$) -> {
          TextField cssUrl = new TextField();
          cssUrl.setPromptText("Input CSS URL");
          hb$.node(cssUrl);
          hb$.button(bt -> {
            bt.setText("Apply");
            bt.setOnAction(e -> {
              pane.getStylesheets().clear();
              pane.getStylesheets().add(cssUrl.getText());
            });
          });
        });
        vb$.separator(s -> {});
      }));
      bp$.center(c$ -> c$.node(pane));
    })));
  }

}
