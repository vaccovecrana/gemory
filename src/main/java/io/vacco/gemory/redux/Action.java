package io.vacco.gemory.redux;

/** @param <T> Defines all possible actions for given store. */
public class Action<T extends Enum<?>, V> {

  public final T type;
  public final V payload;

  public Action(T type, V value) {
    this.type = type;
    this.payload = value;
  }

  public Action(T type) {
    this(type, null);
  }

  @Override public String toString() { return type.toString(); }

}
