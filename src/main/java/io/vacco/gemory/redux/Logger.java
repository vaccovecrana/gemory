package io.vacco.gemory.redux;

import java.util.function.Consumer;

/** Middleware which logs all actions and state changes in store. */
public class Logger<A extends Enum<?>, S> implements Middleware<A, S> {

  private final Consumer<Action<A, ?>> onAction;
  private final Consumer<S> onState;

  public Logger(Consumer<Action<A, ?>> onAction, Consumer<S> onState) {
    this.onAction = onAction;
    this.onState = onState;
  }

  @Override public Action<A, ?> handle(Store<A, S> store, Action<A, ?> action, ActionHandler<A> next) {
    this.onAction.accept(action);
    Action<A, ?> out = next.apply(action);
    this.onState.accept(store.getState());
    return out;
  }

}
