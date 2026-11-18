# Changelog

## 0.7.0

### Added

- `GmSlice`, a reducer-owned version counter for each registered state type:
  - `markChanged()` for reducers;
  - `changed()`, `version()` and `changedSince(long)` for clients.
- `GmStore.slice(Class)` returns the live slice handle for a state type.
- `GmStore.snapshot()` returns an immutable map of every registered state
  reference, and `GmStore.state(Class)` reads one typed state.
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

### Notes

- `GmSlice.changed()` reports whether the **most recent completed pass** marked
  a change. `version()` is cumulative and `changedSince(seen)` is the sticky
  check across passes.
- There is one slice per state type, not one per field. Split state into multiple
  processors/state types if a probe needs finer granularity.
- A reducer that throws rolls the slice version back, but in-place state
  mutations performed before the throw are not restored.
- `GmSlice.markChanged()` is valid only during reducer application; calling it
  from a client or action listener throws `IllegalStateException`.
- Existing static APIs and signatures remain unchanged. `GmStore.on(...)`
  listeners are still weak.
- The store remains a single static instance per JVM. It is not thread-safe;
  confine dispatch, snapshots, slice reads and listener registration to one
  thread (normally the JavaFX Application Thread).
