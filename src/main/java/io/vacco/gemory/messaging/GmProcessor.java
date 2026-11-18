package io.vacco.gemory.messaging;

import java.util.Objects;

/**
 * Applies actions to the state held by a {@link GmSlice}.
 *
 * <p>
 *   The slice owns both the current state object and the version counters. Create processors
 *   with {@link #of(GmReducer, Object)}.
 * </p>
 *
 * <p>
 *   Reducers may mutate state in place. A reducer marks a change by calling
 *   {@code GmStore.currentSlice().markChanged()}. The slice version advances only when a
 *   reducer marks a change; the processor does not count passes.
 * </p>
 *
 * <p>
 *   The slice version is rolled back if the reducer throws, so a failed pass does not
 *   advance the version. The state object may still have been partially mutated, because
 *   Gemory does not copy or restore state.
 * </p>
 *
 * @param <S> state type
 * @since 0.7.0
 */
public class GmProcessor<S> {

  private GmReducer<S> reducer;
  private final GmSlice<S> slice = new GmSlice<>();

  /**
   * Applies an action and commits or rolls back the slice change for this pass.
   *
   * <p>
   *   The reducer receives the slice’s current state; the returned state is stored back in
   *   the slice.
   * </p>
   *
   * @param act action to apply
   */
  public void apply(GmAction<?> act) {
    slice.begin();
    try {
      this.slice.state(reducer.reduce(act, slice.state()));
    } catch (RuntimeException | Error e) {
      slice.rollback();
      throw e;
    }
  }

  /**
   * Creates a processor whose slice holds {@code init}.
   *
   * @param r    reducer
   * @param init initial state
   * @param <T>  state type
   * @return a new processor with a clean slice
   */
  public static <T> GmProcessor<T> of(GmReducer<T> r, T init) {
    GmProcessor<T> p = new GmProcessor<>();
    p.reducer = Objects.requireNonNull(r, "reducer");
    p.slice.state(Objects.requireNonNull(init, "initial state"));
    return p;
  }

  GmSlice<S> slice() {
    return slice;
  }

}
