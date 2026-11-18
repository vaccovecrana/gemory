[gemory](https://en.wikipedia.org/wiki/Gemory) is a minimal JavaFx functional replacement for FXML.

Install from Maven central:

```
io.vacco.gemory:gemory:<LATEST_VERSION>
```

## Runtime introspection and automation

Gemory exposes a single process-wide store for state slices: each registered state
type has a `GmSlice<S>` that holds the current state object and two version
counters. Reducers decide when a slice changed; clients poll the slice and read
state. There are no state-change listeners or action history.

```java
GmStore.reset();
GmStore.useProcessor(GmProcessor.of((action, state) -> {
  if (action instanceof ToggleEngine) {
    state.engineOn = !state.engineOn;
    GmStore.currentSlice().markChanged();
  }
  return state;
}, new DyState()));

var root = GmContext.rootWithIds(c -> {
  c.toggleButton(tb -> {
    tb.setId("engine-toggle");
    tb.setText("RUN");
  });
  c.label(l -> {
    l.setId("status");
    l.setText("OFF");
  });
});

var slice = GmStore.slice(DyState.class);
var toggle = (ToggleButton) root.lookup("engine-toggle").orElseThrow();
toggle.fire(); // run on the JavaFX Application Thread

if (slice.changed()) {
  var state = slice.state();
  // changed() is last-pass; use version() or changedSince(seen) for sticky checks
}

var version = slice.version();
var snapshot = GmStore.snapshot(); // Map<Class<?>, Object>, state references from slices

GmStore.reset();
```

### Slice semantics

Each registered state type owns one `GmSlice<S>` containing the state object and
two version counters:

- Reducers call `GmStore.currentSlice().markChanged()`. The call is valid only on
  the slice of the reducer currently applying; marking another slice throws.
- `state()` reads the current state reference; `GmStore.state(Class)` is the typed
  convenience. The owning processor writes state through `state(S)`.
- `changed()`, `version()` and `changedSince(long)` are client reads.
- `changed()` answers “did the most recent completed pass of this slice mark a
  change?”. A later pass that marks nothing makes it return `false`.
- `version()` counts changed passes cumulatively.
- `changedSince(seen)` is a sticky check across passes.
- Multiple `markChanged()` calls in one pass count once.
- If a reducer throws, the slice version is rolled back. Any in-place state
  mutation performed before the throw is not restored.
- Every reducer branch that changes state must call `markChanged()`. Forgetting
  is silent: the state updates, but the slice version does not move.

There is **one slice per state type**, not one per field. For finer granularity,
split state into multiple processors/state types (`EngineState`,
`TimelineState`, etc.), each with its own slice.

### Stable ids

Set ids inside existing builders; they register automatically. Nested layout,
border-pane, tab-pane, split-pane and scroll-pane contexts share the root
registry, so `GmRoot.lookup("id")` works for controls in nested tabs and panes.
Explicit registration is also available with `c.node("id", node)`.

```java
var root = GmContext.rootWithIds(c -> {
  c.label(l -> {
    l.setId("status");
    l.setText("OFF");
  });
});
var status = (Label) root.lookup("status").orElseThrow();
```

`MenuItem` and other non-`Node` controls are not part of the id registry yet;
menu handlers should dispatch actions, and probes can dispatch the same actions
directly. If a node’s `setId(...)` value is a domain key rather than an
automation handle, use explicit `c.node("ui/...", node)` or a namespaced id to
avoid capturing domain ids in the registry. A cached `GmRoot` goes stale after a
subtree rebuild; re-capture the root returned by each build.

See [docs/testing.md](docs/testing.md) for reset behavior, dynamic rebuilds,
thread confinement and headless/desktop test guidance.
