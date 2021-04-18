package io.vacco.gemory;

import j8spec.junit.J8SpecRunner;
import javafx.application.Application;
import org.junit.runner.RunWith;

import static j8spec.J8Spec.*;

@RunWith(J8SpecRunner.class)
public class HelloSpec {
  static {
    it("Can open a JavaFx shell", () -> {
      Application.launch(HelloFx.class);
    });
  }
}
