package io.vacco.gemory.core;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.function.*;

public class GmContext {

  public final List<Node> nodes = new ArrayList<>();

  public void node(Node n) { nodes.add(n); }

  public void label(Consumer<Label> lbInit) {
    Label lb = new Label();
    lbInit.accept(lb);
    node(lb);
  }

  public void textField(Consumer<TextField> tfInit) {
    TextField tf = new TextField();
    tfInit.accept(tf);
    node(tf);
  }

  public void textArea(Consumer<TextArea> taInit) {
    TextArea ta = new TextArea();
    taInit.accept(ta);
    node(ta);
  }

  public void toggleButton(Consumer<ToggleButton> tbInit) {
    ToggleButton tb = new ToggleButton();
    tbInit.accept(tb);
    node(tb);
  }

  public void button(Consumer<Button> btInit) {
    Button bt = new Button();
    btInit.accept(bt);
    node(bt);
  }

  public void slider(Consumer<Slider> sInit) {
    Slider s = new Slider();
    sInit.accept(s);
    node(s);
  }

  public <T> void spinner(Consumer<Spinner<T>> spInit) { // TODO is this ok?
    Spinner<T> s = new Spinner<>();
    spInit.accept(s);
    node(s);
  }

  public void checkBox(Consumer<CheckBox> cbInit) {
    CheckBox cb = new CheckBox();
    cbInit.accept(cb);
    node(cb);
  }

  public void colorPicker(Consumer<ColorPicker> cpInit) {
    ColorPicker cp = new ColorPicker();
    cpInit.accept(cp);
    node(cp);
  }

  public void menuButton(BiConsumer<MenuButton, GmMenus.GmMenuButtonContext<MenuButton>> mbInit) {
    GmMenus.GmMenuButtonContext<MenuButton> mbc = new GmMenus.GmMenuButtonContext<>(new MenuButton());
    mbInit.accept(mbc.menuButton, mbc);
    node(mbc.menuButton);
  }

  public void splitMenuButton(Consumer<GmMenus.GmMenuButtonContext<SplitMenuButton>> mbInit) {
    GmMenus.GmMenuButtonContext<SplitMenuButton> mbc = new GmMenus.GmMenuButtonContext<>(new SplitMenuButton());
    mbInit.accept(mbc);
    node(mbc.menuButton);
  }

  public void menuBar(Consumer<GmMenus.GmMenuBarContext> mInit) {
    GmMenus.GmMenuBarContext mc = new GmMenus.GmMenuBarContext();
    mInit.accept(mc);
    node(mc.menuBar);
  }

  public <T> void comboBox(Consumer<ComboBox<T>> cbInit) {
    ComboBox<T> cb = new ComboBox<>();
    cbInit.accept(cb);
    node(cb);
  }

  public <T> void choiceBox(Consumer<ChoiceBox<T>> cbInit) {
    ChoiceBox<T> cb = new ChoiceBox<>();
    cbInit.accept(cb);
    node(cb);
  }

  public void datePicker(Consumer<DatePicker> dpInit) {
    DatePicker dp = new DatePicker();
    dpInit.accept(dp);
    node(dp);
  }

  public void hyperLink(Consumer<Hyperlink> hlInit) {
    Hyperlink hl = new Hyperlink();
    hlInit.accept(hl);
    node(hl);
  }

  public void passwordField(Consumer<PasswordField> pfInit) {
    PasswordField pf = new PasswordField();
    pfInit.accept(pf);
    node(pf);
  }

  public void progressBar(Consumer<ProgressBar> pbInit) {
    ProgressBar pb = new ProgressBar();
    pbInit.accept(pb);
    node(pb);
  }

  public void progressIndicator(Consumer<ProgressIndicator> piInit) {
    ProgressIndicator pi = new ProgressIndicator();
    piInit.accept(pi);
    node(pi);
  }

  public void radioButton(Consumer<RadioButton> rbInit) {
    RadioButton rb = new RadioButton();
    rbInit.accept(rb);
    node(rb);
  }

  public void scrollBar(Consumer<ScrollBar> sbInit) {
    ScrollBar sb = new ScrollBar();
    sbInit.accept(sb);
    node(sb);
  }

  public void separator(Consumer<Separator> spInit) {
    Separator s = new Separator();
    spInit.accept(s);
    node(s);
  }

  private void imageView(InputStream is, Consumer<ImageView> ivInit) {
    Image i = new Image(is);
    ImageView iv = new ImageView(i);
    ivInit.accept(iv);
    node(iv);
  }

  public void imageView(URL input, Consumer<ImageView> ivInit) {
    try { imageView(input.openStream(), ivInit); }
    catch (IOException ioe) { throw new IllegalArgumentException(ioe); }
  }

  public void imageView(File input, Consumer<ImageView> ivInit) {
    try { imageView(new FileInputStream(input), ivInit); }
    catch (FileNotFoundException fne) { throw new IllegalArgumentException(fne); }
  }

  private <T extends Pane> void pane(Supplier<T> ps, BiConsumer<T, GmContext> init) {
    GmContext c = new GmContext();
    T p = ps.get();
    init.accept(p, c);
    p.getChildren().addAll(c.nodes);
    node(p);
  }

  public void vBox(BiConsumer<VBox, GmContext> init) { pane(VBox::new, init); }
  public void hBox(BiConsumer<HBox, GmContext> init) { pane(HBox::new, init); }
  public void flowPane(BiConsumer<FlowPane, GmContext> init) { pane(FlowPane::new, init); }
  public void tilePane(BiConsumer<TilePane, GmContext> init) { pane(TilePane::new, init); }
  public void stackPane(BiConsumer<StackPane, GmContext> init) { pane(StackPane::new, init); }

  public void splitPane(BiConsumer<SplitPane, GmContext> init) {
    GmContext c = new GmContext();
    SplitPane sp = new SplitPane();
    init.accept(sp, c);
    node(sp);
  }

  public void borderPane(BiConsumer<BorderPane, GmBorderPaneContext> init) {
    GmBorderPaneContext bpc = new GmBorderPaneContext();
    init.accept(bpc.borderPane, bpc);
    node(bpc.borderPane);
  }

  public void tabPane(BiConsumer<TabPane, GmTabContext> init) {
    GmTabContext tc = new GmTabContext();
    init.accept(tc.pane, tc);
    node(tc.pane);
  }

  public void scrollPane(BiConsumer<ScrollPane, GmContext> init) {
    GmContext c = new GmContext();
    ScrollPane sp = new ScrollPane();
    init.accept(sp, c);
    sp.setContent(c.nodes.get(0));
    node(sp);
  }

  // TODO missing gridPane support.

  public static Node root(Consumer<GmContext> init) {
    GmContext c = new GmContext();
    init.accept(c);
    return c.nodes.get(0);
  }

  /* TODO: ListView, Pagination, TableView, TableColumn */
}
