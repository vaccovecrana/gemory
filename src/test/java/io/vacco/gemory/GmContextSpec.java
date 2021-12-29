package io.vacco.gemory;

import io.vacco.gemory.messaging.*;
import io.vacco.gemory.util.GmStyleEditor;
import io.vacco.shax.logging.ShArgument;
import j8spec.junit.J8SpecRunner;
import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.runner.RunWith;
import org.slf4j.*;

import java.awt.*;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;

import static io.vacco.shax.logging.ShOption.*;
import static io.vacco.gemory.messaging.GmStore.*;
import static io.vacco.gemory.core.GmContext.root;
import static j8spec.J8Spec.*;

@RunWith(J8SpecRunner.class)
public class GmContextSpec {

  static {
    setSysProp(IO_VACCO_SHAX_DEVMODE, "true");
  }

  private static final Logger log = LoggerFactory.getLogger(GmContextSpec.class);

  public static class FooAction extends GmAction<String> {}
  public static class MomoAction extends GmAction<Boolean> {}

  public static class FooState {
    public String message;
    public boolean momoCalled;
  }

  public static class ViewFoo extends StackPane {
    public ViewFoo() {

      on(FooAction.class, act -> {
        log.info("External Foo listener: [{}]", act.payload);
      });

      Node root = root(r$ -> r$.borderPane((bp, bp$) -> {

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
                tf.setUserData((Consumer<FooAction>) fa -> tf.setText(fa.payload));
                on(FooAction.class, (Consumer<FooAction>) tf.getUserData());
                tf.setText("text field content");
              });
              vb$.button(bt -> {
                bt.setText("Submit");
                bt.setOnAction(e -> {
                  dispatch(new FooAction().withPayload("MamaMax is watching you..."));
                  inState(FooState.class, fst -> {
                    log.warn("State load inside ui... [{}]", ShArgument.kv("state", fst));
                  });
                });
              });
              vb$.menuButton((mb, mb$) -> {
                mb.setText("Menu button");
                mb$.item(mi -> mi.setText("Menu button item"));
              });
              vb$.splitMenuButton(mb -> {
                mb.menuButton.setText("Split Menu button");
                mb.item(mi -> mi.setText("Split menu item"));
              });
              vb$.label(lb -> lb.setText("A label"));
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
                cb.setText("Check box");
                cb.setOnAction(e -> dispatch(new MomoAction().withPayload(new Random().nextBoolean())));
              });
              vb$.choiceBox(cb -> cb.setValue("Choice box"));
              vb$.comboBox(cb -> cb.setPromptText("Combo box"));
              vb$.textArea(ta -> ta.setPromptText("Text area..."));
              vb$.toggleButton(tb -> {
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
                vb$.imageView(Objects.requireNonNull(GmContextSpec.class.getResource("/gir-dog.png")), false, iv -> {
                  iv.maxWidth(266);
                  iv.maxHeight(333);
                });
              });
            });
          });
        }));
      }));

      on(MomoAction.class, act -> {
        log.info("External Momo listener: [{}]", act.payload);
      });

      getChildren().add(root);

      useProcessor(GmProcessor.of(
          (act, fst) -> new GmMatch<FooState>()
              .on(FooAction.class, fa -> {
                fst.message = fa.payload;
                return fst;
              })
              .on(MomoAction.class, ma -> {
                fst.momoCalled = ma.payload;
                return fst;
              })
              .apply(act)
          /*
          GmSelector.combineSelectors(
              new GmSelector<>(
                  Function.identity(),
                                        ^----- MATCHER ABOVE,
                  (fst0, fst1) -> fst1
              )
          )
          */
          ,
          new FooState()
      ));
    }
  }

  public static class SpecShell extends Application {
    @Override public void start(Stage stage) {
      GmStyleEditor editor = new GmStyleEditor(new ViewFoo());
      Scene scene = new Scene(editor, 640, 720);
      stage.setScene(scene);
      stage.show();
    }
  }

  static {
    it("Can open a JavaFx shell", () -> {
      if (GraphicsEnvironment.isHeadless()) {
        log.info("Skipping UI test, most likely inside CI environment.");
      } else {
        System.setProperty("prism.text", "t2k");
        System.setProperty("prism.lcdtext", "false");
        Application.launch(SpecShell.class);
      }
    });
  }

}
