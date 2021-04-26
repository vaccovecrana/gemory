package io.vacco.gemory.redux;

/** @param <T> Defines all possible actions for given store. */
public class Action<T extends Enum<?>, V> {

  public T type;
  public V payload;

  public Action<T, V> withType(T type) {
    this.type = type;
    return this;
  }

  public Action<T, V> withPayload(V payload) {
    this.payload = payload;
    return this;
  }

  @Override public String toString() { return type.toString(); }

}
