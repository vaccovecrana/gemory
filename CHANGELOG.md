# Changelog

## 0.7.0

### Added

- `GmSlice<S>`, a state slice container for each registered state type:
  - holds the current state object and two version counters;
  - `GmStore.currentSlice().markChanged()` for reducers;
  - `state()`, `changed()`, `version()` and `changedSince(long)` for clients.
- `GmStore.slice(Class<S>)` returns the typed `GmSlice<S>` (state plus versions).
- `GmStore.currentSlice()` returns the slice of the reducer currently applying;
  marking any other slice throws.
- `GmStore.snapshot()` returns an immutable map of the state references held by
  the registered slices, and `GmStore.state(Class)` reads one typed state.
- `GmStore.removeProcessor(Class)` and `GmStore.reset()` for test isolation
  without restarting the JVM.
- `GmContext.node(String, Node)` and `GmContext.lookup(...)` for explicit stable
  ids, plus automatic registration of non-blank ids set inside existing
  builders.
- `GmContext.rootWithIds(...)` and `GmRoot` retain the shared id registry across
  nested layout, border-pane, tab-pane, split-pane and scroll-pane contexts.
- Automated button-click tests for the sample view.

### Changed

- State change detection is now reducer-declared. `onStateChange`,
  `onAnyStateChange`, `onAnyAction`, action history and pass-based revision
  counters are intentionally not part of this release; probes poll
  `GmStore.slice(type)` and read state. This keeps the store to the existing
  action/processor maps and avoids listener lifecycle and reset complexity.
- `GmContext` mount/unmount tracking now uses the public
  `Node.sceneProperty()` API instead of reflection.
- Processor and action-listener exceptions are isolated: one failure no longer
  aborts the remaining processors or listeners. Exceptions still go through
  `GmStore.setExceptionHandler(...)`.
- `GmContext` throws `IllegalArgumentException` for duplicate, null or blank
  explicit ids.
- `GmProcessor` now stores its state in the processor-owned `GmSlice`; create
  processors with `GmProcessor.of(reducer, init)`.
- Processors and action listeners are kept in registration order, so dispatch and
  `snapshot()` ordering is deterministic for the single-threaded store.

### Removed

- **Breaking:** `GmSelector` and `GmSelector.combineSelectors` were removed. The
  helper composed sub-reducers into a single root state and does not support
  per-state-type slice versions. Use separate state types/processors with
  `GmStore.slice(...)` instead. There are no usages in Gemory or the known
  downstream applications.

### Notes

- `GmSlice.changed()` reports whether the **most recent completed pass** marked
  a change. `version()` is cumulative and `changedSince(seen)` is the sticky
  check across passes.
- `GmSlice.state()` returns the current state reference; `GmSlice.state(S)` is
  used by the owning processor. `GmStore.state(Class)` is the typed convenience.
- There is one slice per state type, not one per field. Split state into multiple
  processors/state types if a probe needs finer granularity.
- A reducer that throws rolls the slice version back, but in-place state
  mutations performed before the throw are not restored.
- Every state-changing reducer branch must call
  `GmStore.currentSlice().markChanged()`. Forgetting is silent: state updates but
  the slice does not move.
- `GmStore.currentSlice()` and `GmSlice.markChanged()` are valid only during
  reducer application; calling them from a client or action listener throws
  `IllegalStateException`.
- Existing static APIs and signatures remain unchanged. `GmStore.on(...)`
  listeners are still weak.
- The store remains a single static instance per JVM. It is not thread-safe;
  confine dispatch, snapshots, slice reads and listener registration to one
  thread (normally the JavaFX Application Thread).
