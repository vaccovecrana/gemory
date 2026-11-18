package io.vacco.gemory.core;

import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import java.util.*;
import java.util.function.BiConsumer;

public class GmTabContext {

  public TabPane pane = new TabPane();

  private final Map<String, Node> ids;

  /**
   * Creates a tab-pane context with a fresh id registry.
   */
  public GmTabContext() {
    this(new LinkedHashMap<>());
  }

  GmTabContext(Map<String, Node> ids) {
    this.ids = Objects.requireNonNull(ids, "ids");
  }

  public void tab(BiConsumer<Tab, GmContext> init) {
    Tab t = new Tab();
    GmContext c = new GmContext(ids);
    init.accept(t, c);
    t.setContent(c.nodes.get(0));
    pane.getTabs().add(t);
  }

}
