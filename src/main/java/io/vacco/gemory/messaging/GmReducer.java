package io.vacco.gemory.messaging;

public interface GmReducer<S> {
  S reduce(GmAction<?> action, S currentState);
}
