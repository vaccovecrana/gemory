package io.vacco.gemory.messaging;

public abstract class GmAction<V> {

  public V payload;

  public GmAction<V> withPayload(V payload) {
    this.payload = payload;
    return this;
  }

  @Override public String toString() { return getClass().getCanonicalName(); }

}
