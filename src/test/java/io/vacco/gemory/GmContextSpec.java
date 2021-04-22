package io.vacco.gemory;

import j8spec.junit.J8SpecRunner;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.runner.RunWith;

import static io.vacco.gemory.GmContext.root;
import static j8spec.J8Spec.*;

@RunWith(J8SpecRunner.class)
public class GmContextSpec {

  public static class ViewFoo extends StackPane {
    public ViewFoo() {
      Node root = root(r -> r.vBox((v, c) -> {
        v.setSpacing(5);
        c.label(lb -> lb.setText("Hello World"));
        c.textField(tf -> tf.setText("How do we turn this into a controlled component?"));
        c.button(bt -> bt.setText("Submit"));
        c.menuButton((mb, mbc) -> {
          mb.setText("Menu button");
          mbc.item(mi -> mi.setText("Some option"));
        });
        c.label(lb -> lb.setText("A label"));
        c.checkBox(cb -> {});
        c.choiceBox(cb -> cb.setValue("lol"));
        c.comboBox(cb -> cb.setPromptText("Combo box"));
      }));
      getChildren().add(root);
    }
  }

  public static class SpecShell extends Application {
    @Override public void start(Stage stage) {
      GmStyleEditor editor = new GmStyleEditor(new ViewFoo());
      Scene scene = new Scene(editor, 640, 480);
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
