package io.vacco.gemory;

import io.vacco.gemory.core.GmRoot;
import io.vacco.gemory.messaging.*;
import io.vacco.shax.logging.ShArgument;
import j8spec.junit.J8SpecRunner;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.junit.Assume;
import org.junit.runner.RunWith;
import org.slf4j.*;
import java.awt.GraphicsEnvironment;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static io.vacco.gemory.messaging.GmStore.*;
import static io.vacco.gemory.util.GmCss.*;
import static io.vacco.gemory.core.GmContext.rootWithIds;
import static j8spec.J8Spec.*;
import static org.junit.Assert.*;

@RunWith(J8SpecRunner.class)
public class GmContextSpec {

  public static class FooAction extends GmAction<String> { }
  public static class MomoAction extends GmAction<Boolean> { }
  public static class FooState {
    public String message;
    public boolean momoCalled;
  }

  public static class ViewFoo extends StackPane {

    private static final Logger log = LoggerFactory.getLogger(ViewFoo.class);

    private final GmRoot root;

    @SuppressWarnings("this-escape")
    public ViewFoo() {
      on(FooAction.class, act -> log.info("External Foo listener: [{}]", act.payload));
      root = rootWithIds(r$ -> r$.borderPane((bp, bp$) -> {
        bp$.top(t$ -> t$.menuBar(mb$ -> {
          mb$.menu((m, m$) -> {
            m.setText("Menu 0");
            m$.item(it -> it.setText("Item 0"));
            m$.item(it -> it.setText("Item 1"));
          });
          mb$.menu((m, m$) -> {
            m.setText("Menu 1");
            m$.item(it -> it.setText("Item 2"));
            m$.item(it -> it.setText("Item 3"));
          });
        }));

        bp$.center(c$ -> c$.tabPane((tp, tp$) -> {
          tp$.tab((tab, tab$) -> {
            tab.setText("Tab 0");
            tab$.vBox((vb, vb$) -> {
              vb$.onMount(sc -> log.info("Tab 0 vbox mounted: " + sc));
              vb$.onUnmount(() -> log.info("Tab 0 vbox unmounted."));
              vb$.textField(tf -> {
                tf.setText("text field content");
                tf.setUserData(on(FooAction.class, fa -> tf.setText(fa.payload)));
              });
              vb$.node(empty(4, 4, 4, 4));
              vb$.button(bt -> {
                bt.setId("submit");
                bt.setText("Submit");
                bt.setOnAction(e -> {
                  dispatch(new FooAction().withPayload("MamaMax is watching you..."));
                  inState(FooState.class, fst -> log.warn("State load inside ui... [{}]", ShArgument.kv("state", fst)));
                });
              });
              vb$.node(empty(4, 4, 4, 4));
              vb$.menuButton((mb, mb$) -> {
                mb.setText("Menu button");
                mb$.item(mi -> mi.setText("Menu button item"));
              });
              vb$.node(empty(4, 4, 4, 4));
              vb$.splitMenuButton(mb -> {
                mb.menuButton.setText("Split Menu button");
                mb.item(mi -> mi.setText("Split menu item"));
              });
              vb$.label(lb -> {
                lb.setText("A label");
                pad(lb, 8, 8);
              });
              pad(vb, 8);
            });
          });

          tp$.tab((tab, tab$) -> {
            tab.setText("Tab 1");
            tab$.vBox((vb, vb$) -> {
              vb$.onMount(sc -> log.info("Tab 1 vbox mounted: " + sc));
              vb$.onUnmount(() -> log.info("Tab 1 vbox unmounted."));
              vb$.label(lb -> lb.setText("Hello World"));
              vb$.separator(s -> s.orientationProperty().setValue(Orientation.HORIZONTAL));
              vb$.checkBox(cb -> {
                cb.setId("momo");
                cb.setText("Check box");
                cb.setOnAction(e -> dispatch(new MomoAction().withPayload(new Random().nextBoolean())));
              });
              vb$.choiceBox(cb -> cb.setValue("Choice box"));
              vb$.comboBox(cb -> cb.setPromptText("Combo box"));
              vb$.textArea(ta -> ta.setPromptText("Text area..."));
              vb$.toggleButton(tb -> {
                tb.setId("toggle");
                tb.setText("Toggle button");
                tb.setOnAction(e -> dispatch(new FooAction().withPayload("Papa Muta agrees...")));
              });
              vb$.slider(sl -> sl.valueProperty().setValue(50));
              vb$.spinner(sn -> sn.setPromptText("Spinner"));
              vb$.datePicker(dp -> dp.valueProperty().setValue(LocalDate.now()));
              vb$.hyperLink(hl -> hl.setText("https://vacco.io"));
            });
          });

          tp$.tab((tab, tab$) -> {
            tab.setText("Tab 2");
            tab$.scrollPane((sp, sp$) -> {
              sp.setFitToWidth(true);
              sp$.vBox((v, vb$) -> {
                vb$.passwordField(pf -> pf.setPromptText("Password input"));
                vb$.progressBar(pb -> pb.progressProperty().setValue(50));
                vb$.progressIndicator(pi -> pi.progressProperty().setValue(0));
                vb$.radioButton(rb -> rb.setText("Radio button"));
                vb$.scrollBar(sb -> sb.setValue(50));
                vb$.colorPicker(cp -> cp.setValue(Color.ALICEBLUE));
                vb$.imageView(Objects.requireNonNull(ViewFoo.class.getResource("/gir-dog.png")), false, iv -> {
                  iv.maxWidth(266);
                  iv.maxHeight(333);
                });
              });
            });
          });
        }));
      }));

      on(MomoAction.class, act -> log.info("External Momo listener: [{}]", act.payload));

      getChildren().add(root.root());
      applyNord(root.root().getParent());

      useProcessor(GmProcessor.of(
          (act, fst) -> new GmMatch<FooState>()
              .on(FooAction.class, fa -> {
                fst.message = fa.payload;
                GmStore.slice(FooState.class).markChanged();
                return fst;
              })
              .on(MomoAction.class, ma -> {
                fst.momoCalled = ma.payload;
                GmStore.slice(FooState.class).markChanged();
                return fst;
              })
              .apply(act)
          // ^----- MATCHER ABOVE
          // GmSelector.combineSelectors(new GmSelector<>(Function.identity(), , (fst0, fst1) -> fst1))
          , new FooState()
      ));
    }

    public GmRoot root() {
      return root;
    }
  }

  private static volatile boolean toolkitReady;

  private static void assumeDesktop() {
    Assume.assumeFalse("JavaFX UI test requires a display", GraphicsEnvironment.isHeadless());
    Assume.assumeTrue("Set -Dgemory.test.desktop=false to skip desktop UI tests",
        !"false".equalsIgnoreCase(System.getProperty("gemory.test.desktop")));
  }

  private static void ensureToolkit() throws Exception {
    if (toolkitReady) {
      return;
    }
    var latch = new CountDownLatch(1);
    try {
      Platform.startup(latch::countDown);
    } catch (IllegalStateException alreadyStarted) {
      Platform.runLater(latch::countDown);
    }
    if (!latch.await(10, TimeUnit.SECONDS)) {
      throw new IllegalStateException("Timed out starting the JavaFX toolkit");
    }
    toolkitReady = true;
  }

  @FunctionalInterface
  interface FxBlock {
    void run() throws Exception;
  }

  private static void onFxThread(FxBlock block) throws Exception {
    var error = new AtomicReference<Throwable>();
    var latch = new CountDownLatch(1);
    Platform.runLater(() -> {
      try {
        block.run();
      } catch (Throwable t) {
        error.set(t);
      } finally {
        latch.countDown();
      }
    });
    if (!latch.await(10, TimeUnit.SECONDS)) {
      throw new AssertionError("Timed out waiting for the JavaFX thread");
    }
    Throwable t = error.get();
    if (t instanceof Error e) {
      throw e;
    }
    if (t instanceof Exception e) {
      throw e;
    }
    if (t != null) {
      throw new RuntimeException(t);
    }
  }

  static {
    beforeEach(() -> {
      GmStore.reset();
      assumeDesktop();
      GmSpecShell.initLogging();
      ensureToolkit();
    });
    afterEach(GmStore::reset);
    afterAll(() -> {
      if (toolkitReady) {
        Platform.exit();
      }
    });
  }

  static {
    it("exposes stable ids for controls in nested tabs", () -> onFxThread(() -> {
      var view = new ViewFoo();

      assertTrue(view.root().lookup("submit").isPresent());
      assertTrue(view.root().lookup("toggle").isPresent());
      assertTrue(view.root().lookup("momo").isPresent());
    }));

    it("Submit button dispatches FooAction and updates FooState", () -> onFxThread(() -> {
      var view = new ViewFoo();
      var slice = GmStore.slice(FooState.class);

      ((Button) view.root().lookup("submit").orElseThrow()).fire();

      assertEquals("MamaMax is watching you...", GmStore.state(FooState.class).message);
      assertTrue(slice.changed());
      assertEquals(1, slice.version());
    }));

    it("Toggle button dispatches FooAction and updates FooState", () -> onFxThread(() -> {
      var view = new ViewFoo();
      var slice = GmStore.slice(FooState.class);

      ((ToggleButton) view.root().lookup("toggle").orElseThrow()).fire();

      assertEquals("Papa Muta agrees...", GmStore.state(FooState.class).message);
      assertTrue(slice.changed());
      assertEquals(1, slice.version());
    }));

    it("Check box dispatches MomoAction and updates FooState", () -> onFxThread(() -> {
      var view = new ViewFoo();
      var slice = GmStore.slice(FooState.class);
      var momo = new AtomicReference<MomoAction>();
      var listener = GmStore.on(MomoAction.class, momo::set);

      ((CheckBox) view.root().lookup("momo").orElseThrow()).fire();

      assertNotNull(listener);
      assertNotNull(momo.get());
      assertEquals(momo.get().payload.booleanValue(), GmStore.state(FooState.class).momoCalled);
      assertTrue(slice.changed());
      assertEquals(1, slice.version());
    }));
  }
}
