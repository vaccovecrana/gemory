package io.vacco.gemory;

import j8spec.junit.J8SpecRunner;
import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.runner.RunWith;

import java.time.LocalDate;

import static io.vacco.gemory.GmContext.root;
import static j8spec.J8Spec.*;

@RunWith(J8SpecRunner.class)
public class GmContextSpec {

  public static class ViewFoo extends StackPane {
    public ViewFoo() {
      Node root = root(r -> r.scrollPane((sp, c) -> {
        c.vBox((v, vc) -> {
          vc.label(lb -> lb.setText("Hello World"));
          vc.textField(tf -> tf.setText("How do we turn this into a controlled component?"));
          vc.button(bt -> bt.setText("Submit"));
          vc.menuButton((mb, mbc) -> {
            mb.setText("Menu button");
            mbc.item(mi -> mi.setText("Menu button item"));
          });
          vc.splitMenuButton(mb -> {
            mb.mb.setText("Split Menu button");
            mb.item(mi -> mi.setText("Split menu item"));
          });
          vc.label(lb -> lb.setText("A label"));
          vc.separator(s -> s.orientationProperty().setValue(Orientation.HORIZONTAL));
          vc.checkBox(cb -> cb.setText("Combo box"));
          vc.choiceBox(cb -> cb.setValue("Choice box"));
          vc.comboBox(cb -> cb.setPromptText("Combo box"));
          vc.textArea(ta -> ta.setPromptText("Text area..."));
          vc.toggleButton(tb -> tb.setText("Toggle button"));
          vc.slider(sl -> sl.valueProperty().setValue(50));
          vc.spinner(sn -> sn.setPromptText("Spinner??"));
          vc.datePicker(dp -> dp.valueProperty().setValue(LocalDate.now()));
          vc.hyperLink(hl -> hl.setText("https://vacco.io"));
          vc.passwordField(pf -> pf.setPromptText("Password input"));
          vc.progressBar(pb -> pb.progressProperty().setValue(50));
          vc.progressIndicator(pi -> pi.progressProperty().setValue(0));
          vc.radioButton(rb -> rb.setText("Radio button"));
          vc.scrollBar(sb -> sb.setValue(50));
          vc.colorPicker(cp -> cp.setValue(Color.ALICEBLUE));
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
