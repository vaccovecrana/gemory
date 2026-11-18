package io.vacco.gemory.messaging;

import java.util.Objects;

/**
 * Applies actions to a single state object and owns its {@link GmSlice} version counter.
 *
 * <p>
 *   Reducers may mutate state in place. A reducer marks a change by calling
 *   {@code GmStore.currentSlice().markChanged()}. The slice version advances only when a
 *   reducer marks a change; the processor does not count passes.
 * </p>
 *
 * <p>
 *   The slice is rolled back if the reducer throws, so a failed pass does not advance the
 *   version. The state object may still have been partially mutated, because Gemory does
 *   not copy or restore state.
 * </p>
 *
 * @param <S> state type
 * @since 0.7.0
 */
public class GmProcessor<S> {

  private S state;
  private GmReducer<S> reducer;
  private final GmSlice slice = new GmSlice();

  /**
   * Creates an empty processor. Prefer {@link #of(GmReducer, Object)}.
   */
  public GmProcessor() { }

  /**
   * Applies an action and commits or rolls back the slice change for this pass.
   *
   * @param act action to apply
   */
  public void apply(GmAction<?> act) {
    slice.begin();
    try {
      this.state = reducer.reduce(act, state);
    } catch (RuntimeException | Error e) {
      slice.rollback();
      throw e;
    }
  }

  /**
   * Creates a processor.
   *
   * @param r    reducer
   * @param init initial state
   * @param <T>  state type
   * @return a new processor with a clean slice
   */
  public static <T> GmProcessor<T> of(GmReducer<T> r, T init) {
    GmProcessor<T> p = new GmProcessor<>();
    p.reducer = Objects.requireNonNull(r, "reducer");
    p.state = Objects.requireNonNull(init, "initial state");
    return p;
  }

  /**
   * @return current state reference
   */
  public S getState() { return state; }

  GmSlice getSlice() { return slice; }
}
