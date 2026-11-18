package io.vacco.gemory.messaging;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.*;

import static io.vacco.gemory.core.GmLog.*;

/**
 * Process-wide action dispatcher and state store.
 *
 * <p>
 *   Gemory keeps a single static store per JVM. There is intentionally no per-instance
 *   store API: call {@link #reset()} to isolate tests or rebuild the store from scratch.
 * </p>
 *
 * <p>
 *   <b>Threading:</b> the store is not thread-safe. Confine dispatch, snapshots, slice reads
 *   and listener registration to a single thread. In a JavaFX application this is normally
 *   the JavaFX Application Thread. State-only tests do not need a JavaFX toolkit, but they
 *   must still run on one thread.
 * </p>
 *
 * <p>
 *   <b>Slices:</b> each processor owns a {@link GmSlice}. Reducers declare changes by
 *   calling {@code GmStore.currentSlice().markChanged()}. Clients read
 *   {@link GmSlice#changed()}, {@link GmSlice#version()} or
 *   {@link GmSlice#changedSince(long)} and poll on the dispatch thread. There are no
 *   automatic pass counters, state-change listeners or action history: the reducer is the
 *   authority on whether its state changed.
 * </p>
 *
 * <p>
 *   {@link GmSlice#changed()} reports whether the most recent completed pass marked a
 *   change. If a later pass leaves the slice untouched, it returns {@code false}. Use
 *   {@link GmSlice#version()} or {@link GmSlice#changedSince(long)} for a sticky check
 *   across passes.
 * </p>
 *
 * <p>
 *   Processors and action listeners are kept in registration order, so dispatch and
 *   {@link #snapshot()} order are deterministic for a single-threaded store.
 * </p>
 *
 * <p>
 *   <b>Subscriptions:</b> {@link #on(Class, Consumer)} keeps weak references and is the
 *   existing action-listener API. Hold the returned consumer for as long as the listener is
 *   needed. {@link #reset()} clears the listener index.
 * </p>
 */
public class GmStore {

  // Action types to consumers of actions. Weak references: existing behavior.
  private static final Map<Class<?>, List<WeakReference<Consumer<GmAction<?>>>>> actIdx = new LinkedHashMap<>();
  // State types to state processors, in registration order.
  private static final Map<Class<?>, GmProcessor<?>> prcIdx = new LinkedHashMap<>();

  private static final Consumer<Throwable> defaultExceptionHandler = e -> error("Action processing cycle error.", e);
  private static Consumer<Throwable> exceptionHandler = defaultExceptionHandler;
  private static GmSlice activeSlice;

  /**
   * Creates a store instance. All store state is static; this constructor exists for source
   * and binary compatibility with earlier releases.
   */
  public GmStore() { }

  /**
   * Registers a weak action listener for the exact action type.
   *
   * <p>
   *   The store keeps only a weak reference to {@code c}. Hold the returned consumer for as
   *   long as the listener is needed.
   * </p>
   *
   * @param pattern exact action type to observe
   * @param c       action consumer
   * @param <A>     action type
   * @return the same consumer, for callers that want to keep a strong reference
   */
  @SuppressWarnings("unchecked")
  public static <A extends GmAction<?>> Consumer<A> on(Class<A> pattern, Consumer<A> c) {
    info("Adding action listener: [{}] -> [{}]", pattern.getSimpleName(), c);
    WeakReference<Consumer<GmAction<?>>> wc = new WeakReference<>((Consumer<GmAction<?>>) c);
    actIdx.computeIfAbsent(pattern, p -> new ArrayList<>()).add(wc);
    return c;
  }

  /**
   * Reads the current state for a registered processor type.
   *
   * @param stc state type
   * @param c   consumer of the current state
   * @param <S> state type
   */
  @SuppressWarnings("unchecked")
  public static <S> void inState(Class<S> stc, Consumer<S> c) {
    Objects.requireNonNull(stc, "state type");
    Objects.requireNonNull(c, "consumer");
    GmProcessor<?> p = prcIdx.get(stc);
    if (p != null) {
      c.accept((S) p.getState());
    }
  }

  /**
   * Maps the current state for a registered processor type.
   *
   * @param stc state type
   * @param oFn mapping function
   * @param <S> state type
   * @param <O> result type
   * @return the mapped value
   * @throws IllegalArgumentException if no processor is registered for {@code stc}
   */
  public static <S, O> O mapState(Class<S> stc, Function<S, O> oFn) {
    Objects.requireNonNull(oFn, "mapping function");
    return oFn.apply(state(stc));
  }

  /**
   * Registers or replaces the processor for its initial state type.
   *
   * <p>
   *   The processor is keyed by the exact runtime class of its initial state. Registering a
   *   second processor with the same state class replaces the first one and its slice.
   * </p>
   *
   * @param p processor to register
   */
  public static void useProcessor(GmProcessor<?> p) {
    Objects.requireNonNull(p, "processor");
    Objects.requireNonNull(p.getState(), "processor initial state");
    prcIdx.put(p.getState().getClass(), p);
  }

  /**
   * Removes the processor registered for an exact state type.
   *
   * @param type state type
   * @return {@code true} if a processor was removed
   */
  public static boolean removeProcessor(Class<?> type) {
    Objects.requireNonNull(type, "state type");
    return prcIdx.remove(type) != null;
  }

  /**
   * Returns an immutable copy of the currently registered state references.
   *
   * <p>
   *   The map is copied, but the state objects are not. Reducers may mutate state in place,
   *   so treat the values as read-only and only use snapshots on the dispatch thread.
   * </p>
   *
   * @return state type to current state reference
   */
  public static Map<Class<?>, Object> snapshot() {
    Map<Class<?>, Object> states = new LinkedHashMap<>();
    prcIdx.forEach((type, processor) -> states.put(type, processor.getState()));
    return Collections.unmodifiableMap(states);
  }

  /**
   * Returns the current state for an exact registered type.
   *
   * @param type state type
   * @param <S>  state type
   * @return current state
   * @throws IllegalArgumentException if no processor is registered for {@code type}
   */
  public static <S> S state(Class<S> type) {
    Objects.requireNonNull(type, "state type");
    GmProcessor<?> p = prcIdx.get(type);
    if (p == null) {
      throw new IllegalArgumentException("No processor registered for state type " + type.getName());
    }
    return type.cast(p.getState());
  }

  /**
   * Returns the reducer-owned version counter for an exact registered state type.
   *
   * <p>
   *   The returned handle is live for the lifetime of the processor. Re-fetch it after
   *   {@link #reset()}, {@link #removeProcessor(Class)} or processor replacement. Reducers
   *   mark changes through {@link #currentSlice()}; clients only read the slice.
   * </p>
   *
   * @param type state type
   * @return slice version handle
   * @throws IllegalArgumentException if no processor is registered for {@code type}
   */
  public static GmSlice slice(Class<?> type) {
    Objects.requireNonNull(type, "state type");
    GmProcessor<?> p = prcIdx.get(type);
    if (p == null) {
      throw new IllegalArgumentException("No processor registered for state type " + type.getName());
    }
    return p.getSlice();
  }

  /**
   * Returns the slice of the processor currently applying an action.
   *
   * <p>
   *   Reducers call {@code GmStore.currentSlice().markChanged()}. Clients use
   *   {@link #slice(Class)} to read a slice. This method throws outside reducer
   *   application.
   * </p>
   *
   * @return active reducer slice
   * @throws IllegalStateException if no reducer is currently applying an action
   */
  public static GmSlice currentSlice() {
    GmSlice current = activeSlice;
    if (current == null) {
      throw new IllegalStateException("currentSlice() is only valid inside a reducer");
    }
    return current;
  }

  /**
   * Resets the store to a clean state.
   *
   * <p>
   *   Removes processors and weak action listeners and restores the default exception
   *   handler. The store remains reusable after this call. Slice handles cached by clients
   *   become detached; re-fetch them after reset.
   * </p>
   *
   * <p>
   *   Do not call this from a processor. Calling it from an action listener is supported.
   * </p>
   */
  public static void reset() {
    actIdx.clear();
    prcIdx.clear();
    exceptionHandler = defaultExceptionHandler;
  }

  /**
   * Dispatches an action to all processors and typed action listeners.
   *
   * <p>
   *   Processors run first. A processor failure is reported to the exception handler and
   *   does not prevent the remaining processors or listeners from running. Typed
   *   {@link #on(Class, Consumer)} listeners run afterwards.
   * </p>
   *
   * <p>
   *   Reducers update slice versions explicitly with
   *   {@code GmStore.currentSlice().markChanged()}. A reducer that throws rolls its
   *   slice version back, but any in-place state mutation it performed before throwing is
   *   not restored.
   * </p>
   *
   * <p>
   *   Dispatching from an action listener is allowed and produces nested processing.
   *   Dispatching from a processor is not supported; the active slice is saved and restored
   *   around each processor so an accidental nested dispatch cannot leave the outer pass
   *   without one.
   * </p>
   *
   * @param act action to dispatch
   */
  public static void dispatch(GmAction<?> act) {
    Objects.requireNonNull(act, "action");
    GmSlice previous = activeSlice;
    try {
      for (GmProcessor<?> p : prcIdx.values()) {
        activeSlice = p.getSlice();
        try {
          p.apply(act);
        } catch (Exception e) {
          activeSlice = previous;
          report(e);
        } finally {
          activeSlice = previous;
        }
      }
    } finally {
      activeSlice = previous;
    }
    notifyTypedActions(act);
  }

  /**
   * Sets the handler for action-processing, processor and listener exceptions.
   *
   * @param eh exception handler
   */
  public static void setExceptionHandler(Consumer<Throwable> eh) {
    exceptionHandler = Objects.requireNonNull(eh, "exception handler");
  }

  private static void report(Throwable t) {
    try {
      exceptionHandler.accept(t);
    } catch (Exception e) {
      error("Action exception handler failed.", e);
    }
  }

  private static void notifyTypedActions(GmAction<?> act) {
    List<WeakReference<Consumer<GmAction<?>>>> cl = actIdx.get(act.getClass());
    if (cl != null && !cl.isEmpty()) {
      List<WeakReference<Consumer<GmAction<?>>>> cycleRefs = new ArrayList<>(cl);
      cycleRefs.forEach(cRef -> eval(act, cRef));
      if (cycleRefs.size() != cl.size()) {
        cl.forEach(cRef -> {
          if (!cycleRefs.contains(cRef)) {
            eval(act, cRef);
          }
        });
      }
    } else if (isDebugEnabled()) {
      debug("No listeners found for action [{}]", act.getClass().getSimpleName());
    }
  }

  private static void eval(GmAction<?> act, WeakReference<Consumer<GmAction<?>>> cRef) {
    Consumer<GmAction<?>> c = cRef.get();
    if (c != null) {
      try {
        c.accept(act);
      } catch (Exception e) {
        report(e);
      }
    } else if (isDebugEnabled()) {
      debug("Reference [{}] for action [{}] has been invalidated.", cRef, act.getClass().getSimpleName());
    }
  }
}
