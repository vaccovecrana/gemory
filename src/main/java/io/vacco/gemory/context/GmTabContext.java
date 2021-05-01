package io.vacco.gemory.context;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import java.util.function.BiConsumer;

public class GmTabContext {

  public TabPane pane = new TabPane();

  public void tab(BiConsumer<Tab, GmContext> init) {
    Tab t = new Tab();
    GmContext c = new GmContext();
    init.accept(t, c);
    t.setContent(c.nodes.get(0));
    pane.getTabs().add(t);
  }

}
