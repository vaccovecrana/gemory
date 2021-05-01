package io.vacco.gemory.core;

import javafx.scene.control.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class GmMenus {

  public static class GmMenuBase {
    public void setItem(Consumer<MenuItem> miInit, Consumer<MenuItem> miSet) {
      MenuItem mi = new MenuItem();
      miInit.accept(mi);
      miSet.accept(mi);
    }
  }

  public static class GmMenuButtonContext<T extends MenuButton> extends GmMenuBase {
    public final T menuButton;
    public GmMenuButtonContext(T menuButton) { this.menuButton = menuButton; }
    public void item(Consumer<MenuItem> miInit) {
      setItem(miInit, mi -> menuButton.getItems().add(mi));
    }
  }

  public static class GmMenuContext extends GmMenuBase {
    public final Menu menu = new Menu();
    public void item(Consumer<MenuItem> miInit) {
      setItem(miInit, mi -> menu.getItems().add(mi));
    }
  }

  public static class GmMenuBarContext extends GmMenuBase {
    public final MenuBar menuBar = new MenuBar();
    public void menu(BiConsumer<Menu, GmMenuContext> mInit) {
      GmMenuContext mc = new GmMenuContext();
      mInit.accept(mc.menu, mc);
      menuBar.getMenus().add(mc.menu);
    }
  }

}
