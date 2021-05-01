package io.vacco.gemory.core;

import javafx.scene.layout.BorderPane;
import java.util.function.Consumer;

public class GmBorderPaneContext {

  public BorderPane borderPane = new BorderPane();

  private void build(Consumer<GmContext> init, Consumer<GmContext> setPosition) {
    GmContext c = new GmContext();
    init.accept(c);
    setPosition.accept(c);
  }

  public void top(Consumer<GmContext> init) {
    build(init, c -> borderPane.setTop(c.nodes.get(0)));
  }

  public void left(Consumer<GmContext> init) {
    build(init, c -> borderPane.setLeft(c.nodes.get(0)));
  }

  public void bottom(Consumer<GmContext> init) {
    build(init, c -> borderPane.setBottom(c.nodes.get(0)));
  }

  public void right(Consumer<GmContext> init) {
    build(init, c -> borderPane.setRight(c.nodes.get(0)));
  }

  public void center(Consumer<GmContext> init) { build(init, c -> borderPane.setCenter(c.nodes.get(0))); }

}
