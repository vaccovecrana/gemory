package io.vacco.gemory;

import io.vacco.gemory.core.GmLog;
import io.vacco.gemory.util.GmStyleEditor;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.vacco.shax.logging.ShOption.*;

public class GmSpecShell extends Application {

  static {
    setSysProp(IO_VACCO_SHAX_DEVMODE, "true");
    setSysProp(IO_VACCO_SHAX_LOGLEVEL, "debug");
  }

  private static final Logger log = LoggerFactory.getLogger(GmSpecShell.class);

  static void initLogging() {
    GmLog.setInfoLogger(log::info);
    GmLog.setDebugLogger(log::debug);
    GmLog.setErrorLogger(log::error);
  }

  public static void main(String[] args) {
    System.setProperty("prism.text", "t2k");
    System.setProperty("prism.lcdtext", "false");
    initLogging();
    Application.launch(GmSpecShell.class, args);
  }

  @Override
  public void start(Stage stage) {
    GmStyleEditor editor = new GmStyleEditor(new GmContextSpec.ViewFoo());
    Scene scene = new Scene(editor, 640, 720);
    stage.setScene(scene);
    stage.show();
  }

}
