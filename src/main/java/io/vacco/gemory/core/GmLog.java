package io.vacco.gemory.core;

import java.util.Objects;
import java.util.function.BiConsumer;

public class GmLog {

  private static BiConsumer<String, Object[]> infoFn, debugFn, errorFn;

  public static void setInfoLogger(BiConsumer<String, Object[]> logFn) {
    infoFn = Objects.requireNonNull(logFn);
  }

  public static void setDebugLogger(BiConsumer<String, Object[]> logFn) {
    debugFn = Objects.requireNonNull(logFn);
  }

  public static void setErrorLogger(BiConsumer<String, Object[]> logFn) {
    errorFn = Objects.requireNonNull(logFn);
  }

  public static void info(String msg, Object... args) {
    if (infoFn != null) {
      infoFn.accept(msg, args);
    }
  }

  public static void debug(String msg, Object... args) {
    if (debugFn != null) {
      debugFn.accept(msg, args);
    }
  }

  public static void error(String msg, Object... args) {
    if (errorFn != null) {
      errorFn.accept(msg, args);
    }
  }

  public static boolean isDebugEnabled() {
    return debugFn != null;
  }

}
