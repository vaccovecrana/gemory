package io.vacco.gemory;

import io.vacco.gemory.context.GmStyleEditor;
import j8spec.junit.J8SpecRunner;
import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.runner.RunWith;

import java.time.LocalDate;
import java.util.Objects;

import static io.vacco.gemory.context.GmContext.root;
import static j8spec.J8Spec.*;

@RunWith(J8SpecRunner.class)
public class GmContextSpec {

  public static class ViewFoo extends StackPane {
    public ViewFoo() {
      Node root = root(r -> r.scrollPane((sp, c) -> {
        sp.setFitToWidth(true);
        c.vBox((v, $) -> {
          $.label(lb -> lb.setText("Hello World"));
          $.textField(tf -> tf.setText("How do we turn this into a controlled component?"));
          $.button(bt -> bt.setText("Submit"));
          $.menuButton((mb, mbc) -> {
            mb.setText("Menu button");
            mbc.item(mi -> mi.setText("Menu button item"));
          });
          $.splitMenuButton(mb -> {
            mb.mb.setText("Split Menu button");
            mb.item(mi -> mi.setText("Split menu item"));
          });
          $.label(lb -> lb.setText("A label"));
          $.separator(s -> s.orientationProperty().setValue(Orientation.HORIZONTAL));
          $.checkBox(cb -> cb.setText("Check box"));
          $.choiceBox(cb -> cb.setValue("Choice box"));
          $.comboBox(cb -> cb.setPromptText("Combo box"));
          $.textArea(ta -> ta.setPromptText("Text area..."));
          $.toggleButton(tb -> tb.setText("Toggle button"));
          $.slider(sl -> sl.valueProperty().setValue(50));
          $.spinner(sn -> sn.setPromptText("Spinner"));
          $.datePicker(dp -> dp.valueProperty().setValue(LocalDate.now()));
          $.hyperLink(hl -> hl.setText("https://vacco.io"));
          $.passwordField(pf -> pf.setPromptText("Password input"));
          $.progressBar(pb -> pb.progressProperty().setValue(50));
          $.progressIndicator(pi -> pi.progressProperty().setValue(0));
          $.radioButton(rb -> rb.setText("Radio button"));
          $.scrollBar(sb -> sb.setValue(50));
          $.colorPicker(cp -> cp.setValue(Color.ALICEBLUE));
          $.imageView(Objects.requireNonNull(GmContextSpec.class.getResource("/gir-dog.png")), iv -> {
            iv.maxWidth(266);
            iv.maxHeight(333);
          });
        });
      }));
      getChildren().add(root);
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
      System.setProperty("prism.text", "t2k");
      System.setProperty("prism.lcdtext", "false");
      Application.launch(SpecShell.class);
    });
  }

}
