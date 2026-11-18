package io.vacco.gemory.core;

import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.function.*;

/**
 * Functional JavaFX node builder.
 *
 * <p>
 *   Every context carries a stable-id registry. Nested contexts created by layout,
 *   border-pane and tab-pane builders reuse the same registry, so an id set anywhere in the
 *   tree can be resolved from the root. Use {@link #rootWithIds(Consumer)} to retain the
 *   registry, or {@link #root(Consumer)} when ids are not needed.
 * </p>
 *
 * <p>
 *   Ids are captured when a node is added to a context. Auto-registration happens for any
 *   non-blank {@link javafx.scene.Node#getId()} present at that moment; changing an id later
 *   does not update the registry. Ids must be unique across the whole tree.
 * </p>
 *
 * <p>
 *   Ids are not unregistered when nodes leave the scene. Rebuilding a subtree with the same
 *   ids on different node instances throws {@link IllegalArgumentException}; reuse the node
 *   instances or build a fresh {@link GmContext}/{@link GmRoot} for the rebuilt subtree.
 * </p>
 */
public class GmContext {

  private Consumer<Scene> onMountCons;
  private Runnable onUnmountRun;

  public final List<Node> nodes = new ArrayList<>();

  private final Map<String, Node> ids;

  /**
   * Creates a context with a fresh stable-id registry.
   */
  public GmContext() {
    this(new LinkedHashMap<>());
  }

  GmContext(Map<String, Node> ids) {
    this.ids = Objects.requireNonNull(ids, "ids");
  }

  /**
   * Adds a node to this context.
   *
   * <p>
   *   If the node already has a non-blank id, it is registered automatically. Set ids inside
   *   builder callbacks to get stable automation handles without changing the builders:
   * </p>
   *
   * <pre>{@code
   * c.toggleButton(tb -> { tb.setId("engine-toggle"); tb.setText("RUN"); });
   * }</pre>
   *
   * @param n node to add
   */
  public void node(Node n) {
    Objects.requireNonNull(n, "node");
    String id = n.getId();
    if (id != null && !id.isBlank()) {
      register(id, n);
    }
    nodes.add(n);
  }

  /**
   * Sets a stable id on a node, registers it, and adds it to this context.
   *
   * @param id  stable node id
   * @param n   node to add
   * @param <N> node type
   * @return the same node
   * @throws IllegalArgumentException if {@code id} is null or blank, or already used by a
   *                                  different node
   */
  public <N extends Node> N node(String id, N n) {
    if (id == null || id.isBlank()) {
      throw new IllegalArgumentException("Node id must not be null or blank");
    }
    Objects.requireNonNull(n, "node");
    register(id, n);
    n.setId(id);
    nodes.add(n);
    return n;
  }

  /**
   * Looks up a node by id in this context's shared registry.
   *
   * @param id node id
   * @return the registered node, if present
   */
  public Optional<Node> lookup(String id) {
    if (id == null || id.isBlank()) {
      return Optional.empty();
    }
    return Optional.ofNullable(ids.get(id));
  }

  /**
   * Returns an unmodifiable, live view of the id registry.
   *
   * <p>
   *   The map reflects nodes registered later in the same build or in dynamic content. It is
   *   not a snapshot.
   * </p>
   *
   * @return live unmodifiable id map
   */
  public Map<String, Node> nodesById() {
    return Collections.unmodifiableMap(ids);
  }

  private void register(String id, Node n) {
    Node existing = ids.get(id);
    if (existing == null) {
      ids.put(id, n);
    } else if (existing != n) {
      throw new IllegalArgumentException(
          "Duplicate node id [" + id + "]: already registered to " + existing + ", cannot register " + n
      );
    }
  }

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

  public <T> void listView(Consumer<ListView<T>> lvInit) {
    ListView<T> lv = new ListView<>();
    lvInit.accept(lv);
    node(lv);
  }

  private void imageView(String url, boolean background, Consumer<ImageView> ivInit) {
    Image i = new Image(url, background);
    ImageView iv = new ImageView(i);
    ivInit.accept(iv);
    node(iv);
  }

  public void imageView(URL input, boolean background, Consumer<ImageView> ivInit) {
    imageView(input.toString(), background, ivInit);
  }

  public void imageView(File input, boolean background, Consumer<ImageView> ivInit) {
    try { imageView(input.toURI().toURL().toString(), background, ivInit); }
    catch (MalformedURLException fne) { throw new IllegalArgumentException(fne); }
  }

  public void onMount(Consumer<Scene> sc) {
    this.onMountCons = sc;
  }

  public void onUnmount(Runnable r) {
    this.onUnmountRun = r;
  }

  private <T extends Pane> void pane(Supplier<T> ps, BiConsumer<T, GmContext> init) {
    T p = ps.get();
    GmContext c = new GmContext(ids);
    init.accept(p, c);
    p.sceneProperty().addListener((obs, ov, nv) -> {
      if (nv != null && c.onMountCons != null) {
        c.onMountCons.accept(nv);
      } else if (nv == null && c.onUnmountRun != null) {
        c.onUnmountRun.run();
      }
    });
    p.getChildren().addAll(c.nodes);
    node(p);
  }

  public void vBox(BiConsumer<VBox, GmContext> init) { pane(VBox::new, init); }
  public void hBox(BiConsumer<HBox, GmContext> init) { pane(HBox::new, init); }
  public void flowPane(BiConsumer<FlowPane, GmContext> init) { pane(FlowPane::new, init); }
  public void tilePane(BiConsumer<TilePane, GmContext> init) { pane(TilePane::new, init); }
  public void stackPane(BiConsumer<StackPane, GmContext> init) { pane(StackPane::new, init); }

  public void splitPane(BiConsumer<SplitPane, GmContext> init) {
    GmContext c = new GmContext(ids);
    SplitPane sp = new SplitPane();
    init.accept(sp, c);
    sp.getItems().addAll(c.nodes);
    node(sp);
  }

  public void borderPane(BiConsumer<BorderPane, GmBorderPaneContext> init) {
    GmBorderPaneContext bpc = new GmBorderPaneContext(ids);
    init.accept(bpc.borderPane, bpc);
    node(bpc.borderPane);
  }

  public void tabPane(BiConsumer<TabPane, GmTabContext> init) {
    GmTabContext tc = new GmTabContext(ids);
    init.accept(tc.pane, tc);
    node(tc.pane);
  }

  public void scrollPane(BiConsumer<ScrollPane, GmContext> init) {
    GmContext c = new GmContext(ids);
    ScrollPane sp = new ScrollPane();
    init.accept(sp, c);
    sp.setContent(c.nodes.get(0));
    node(sp);
  }

  public static Node root(Consumer<GmContext> init) {
    GmContext c = new GmContext();
    init.accept(c);
    return c.nodes.get(0);
  }

  /**
   * Builds a root node and retains its shared stable-id registry.
   *
   * @param init context initializer
   * @return the root node and its live id view
   * @throws IllegalStateException if the initializer creates no nodes
   */
  public static GmRoot rootWithIds(Consumer<GmContext> init) {
    GmContext c = new GmContext();
    init.accept(c);
    if (c.nodes.isEmpty()) {
      throw new IllegalStateException("No root node was created");
    }
    return new GmRoot(c.nodes.get(0), c.nodesById());
  }

  /* TODO: Pagination, TableView, TableColumn */

}
