package io.vacco.gemory.util;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.*;
import java.lang.reflect.Field;
import java.util.Optional;

public class GmReflect {

  @SuppressWarnings("unchecked")
  public static Optional<SimpleObjectProperty<Scene>> scenePropOf(Node n) {
    try {
      Class<?> clazz = n.getClass();
      while (clazz != null) {
        for (Field f : clazz.getDeclaredFields()) {
          if (ObservableValue.class.isAssignableFrom(f.getType())) {
            f.setAccessible(true);
            ObservableValue<?> obs = (ObservableValue<?>) f.get(n);
            if (obs instanceof SimpleObjectProperty) {
              SimpleObjectProperty<?> sop = (SimpleObjectProperty<?>) obs;
              if (sop.getName().equals("scene")) {
                SimpleObjectProperty<Scene> sp = (SimpleObjectProperty<Scene>) sop;
                return Optional.of(sp);
              }
            }
          }
        }
        clazz = clazz.getSuperclass();
      }
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    return Optional.empty();
  }

}
