package io.vacco.gemory.redux;

import java.util.*;

/**
 * Brings actions and reducers together, and holds state of the app.
 * It allows to get current state, subscribe to state changes, and to update state by dispatching actions.
 * It is the single point of truth in your application.
 *
 * @param <A> Action type.
 * @param <S> State type. Describes the state of your app. Should be immutable.
 */
public final class Store<A extends Enum<?>, S> {

  private S currentState;
  private final Reducer<A, S> reducer;
  private final List<Runnable> subscribers = new ArrayList<>();

  private final ActionHandler<A> rootSink;
  private final Map<Integer, ActionHandler<A>> sinkCache = new HashMap<>();

  /**
   * @param reducer     Root reducer
   * @param state       Initial state
   * @param middlewares Middlewares to register in store
   */
  @SafeVarargs public Store(Reducer<A, S> reducer, S state, Middleware<A, S> ... middlewares) {
    this.reducer = reducer;
    this.currentState = state;
    this.rootSink = next(0, middlewares);
  }

  private ActionHandler<A> next(int index, Middleware<A, S>[] middlewares) {
    return sinkCache.computeIfAbsent(index, k -> index == middlewares.length ? action -> {
      currentState = reducer.reduce(action, currentState);
      for (Runnable subscriber : subscribers) { subscriber.run(); }
      return action;
    } : action -> middlewares[index].handle(this, action, next(index + 1, middlewares)));
  }

  public S dispatch(Action<A, ?> action) {
    rootSink.apply(action);
    return this.getState();
  }

  public S getState() { return this.currentState; }

  /**
   * Add new subscriber listening for store's state changes.
   *
   * @param r Runnable which will be called on store's state change.
   * @return Runnable which will unsubscribe Runnable r from this store's state changes.
   */
  public Runnable subscribe(final Runnable r) {
    this.subscribers.add(r);
    return () -> subscribers.remove(r);
  }

}
