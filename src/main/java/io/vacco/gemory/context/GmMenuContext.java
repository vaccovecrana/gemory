package io.vacco.gemory.context;

import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import java.util.function.Consumer;

public class GmMenuContext<T extends MenuButton> {

  public T mb;

  public GmMenuContext(T menuButton) { this.mb = menuButton; }

  public void item(Consumer<MenuItem> miInit) {
    MenuItem mi = new MenuItem();
    miInit.accept(mi);
    mb.getItems().add(mi);
  }

}
