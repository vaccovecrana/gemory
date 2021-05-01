package io.vacco.gemory.util;

import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import static io.vacco.gemory.core.GmContext.*;

public class GmStyleEditor extends StackPane {

  public GmStyleEditor(Pane pane) {

    pane.getStyleClass().add("root");

    getChildren().add(root(r -> r.borderPane((bp, ctx) -> {
      ctx.top(tc -> tc.vBox((vb, vbc) -> {
        vbc.hBox((hb, hbc) -> {
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
        });
        vbc.separator(s -> {});
      }));
      ctx.center(cc -> cc.node(pane));
    })));
  }

}
