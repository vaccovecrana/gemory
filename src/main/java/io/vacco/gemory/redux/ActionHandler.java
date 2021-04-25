package io.vacco.gemory.redux;

/**
 * Base action handler definition.
 * @param <A> Action type
 */
public interface ActionHandler<A extends Enum<?>> {

  /**
   * @param action the active action.
   * @return a new action, or the same input as <code>action</code>.
   */
  Action<A, ?> apply(Action<A, ?> action);

}
