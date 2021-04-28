package io.vacco.gemory.redux;

/**
 * Third-party extension point between dispatching an action and
 * the moment it reaches the root reducer.
 *
 * @param <A> Action type
 * @param <S> State type
 */
public interface Middleware<A extends Enum<?>, S> {

  /**
   * Intercepts an action as either:
   * <ul>
   *   <li>Do nothing.</li>
   *   <li>Transform <code>action</code>.</li>
   *   <li>Delegate <code>action</code> to the <code>next</code> middleware.</li>
   * </ul>
   *
   * @param store  allows for optional action dispatching (e. g. for asynchronous processing).
   * @param action the active action inside the store.
   * @param next   the next action handler in the processing chain.
   * @return a new action, or the same input as <code>action</code>.
   */
  Action<A, ?> handle(Store<A, S> store, Action<A, ?> action, ActionHandler<A> next);

}
