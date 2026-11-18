package io.vacco.gemory.messaging;

/**
 * Reducer-owned version counter for one registered state type.
 *
 * <p>
 *   The processor keeps two counters: the version at the start of the current (or last)
 *   pass, and the current version. A reducer marks a change through
 *   {@code GmStore.currentSlice().markChanged()}; the first mark in a pass increments the
 *   current version. If the reducer marks nothing, or the pass fails, both counters stay
 *   equal.
 * </p>
 *
 * <p>
 *   {@link #changed()} therefore answers “did the most recent completed pass of this slice
 *   mark a change?”. Use {@link #version()} or {@link #changedSince(long)} when a client
 *   needs a sticky check across several passes.
 * </p>
 *
 * <p>
 *   <b>Reducer-only:</b> {@link #markChanged()} is valid only on the slice returned by
 *   {@link GmStore#currentSlice()} while a reducer is applying. Clients only read
 *   {@link #changed()}, {@link #version()} and {@link #changedSince(long)}.
 * </p>
 *
 * @since 0.7.0
 */
public final class GmSlice {

  private long v0;
  private long v1;

  GmSlice() { }

  /**
   * @return cumulative number of changed passes
   */
  public long version() {
    return v1;
  }

  /**
   * @return {@code true} if the most recent completed pass marked a change
   */
  public boolean changed() {
    return v0 != v1;
  }

  /**
   * @param version previously observed version
   * @return {@code true} if the slice changed after {@code version}
   */
  public boolean changedSince(long version) {
    return v1 != version;
  }

  /**
   * Marks the slice as changed for the current pass.
   *
   * <p>
   *   Calling this more than once in the same pass still counts as one changed pass.
   * </p>
   *
   * @throws IllegalStateException if called outside a reducer application, or on a slice
   *                               other than the active reducer slice
   */
  public void markChanged() {
    if (GmStore.currentSlice() != this) {
      throw new IllegalStateException(
          "markChanged() must be called on GmStore.currentSlice() from the active reducer"
      );
    }
    if (v1 == v0) {
      v1++;
    }
  }

  void begin() {
    v0 = v1;
  }

  void rollback() {
    v1 = v0;
  }
}
