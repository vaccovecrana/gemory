# Testing Gemory applications

## State-only tests

No JavaFX toolkit is needed, and the store has no `javafx.*` dependency:

```java
@Before public void resetStore() { GmStore.reset(); }
@After  public void cleanStore() { GmStore.reset(); }

@Test public void incrementsCounter() {
  GmStore.useProcessor(GmProcessor.of((action, state) -> {
    if (action instanceof Inc) {
      state.value++;
      GmStore.slice(CounterState.class).markChanged();
    }
    return state;
  }, new CounterState()));

  var slice = GmStore.slice(CounterState.class);
  GmStore.dispatch(new Inc());

  assertEquals(1, GmStore.state(CounterState.class).value);
  assertTrue(slice.changed());
  assertEquals(1, slice.version());
}
```

The store is a single static instance per JVM. Run stateful tests sequentially
and call `GmStore.reset()` in setup and teardown; do not share state between
parallel test classes.

## JavaFX component tests

UI tests must construct controls and dispatch actions on the JavaFX Application
Thread. Gemory does not depend on TestFX; stable ids plus the store/slice APIs
are the contract.

```java
Platform.startup(() -> {});
Platform.runLater(() -> {
  var root = GmContext.rootWithIds(c -> {
    c.toggleButton(tb -> {
      tb.setId("engine-toggle");
      tb.setText("RUN");
      tb.setOnAction(e -> GmStore.dispatch(new ToggleEngine()));
    });
  });

  var slice = GmStore.slice(DyState.class);
  var toggle = (ToggleButton) root.lookup("engine-toggle").orElseThrow();
  toggle.fire();

  if (slice.changed()) {
    // read GmStore.state(DyState.class)
  }
});
```

`GmContextSpec` in this repository contains a complete example. It initializes
the toolkit once, runs each test body on the FX thread, and never shows a
stage, so it does not open a window during `./gradlew test`.

## Slice granularity

`GmStore.slice(type)` is one counter per registered state type, not one per
field. If a probe needs to distinguish engine changes from timeline changes,
split state into multiple processors/state types (`EngineState`,
`TimelineState`, etc.) or keep app-level counters inside the state. The slice is
the coarse “this state type was marked changed” signal.

## `changed()` versus sticky checks

- `changed()` reports whether the **most recent completed pass** marked a
  change. A later untouched pass makes it `false`.
- `version()` is cumulative.
- `changedSince(seen)` reports whether the version moved after `seen`.

Use `changed()` immediately after the action you care about, or
`changedSince(seen)` / `version()` if other actions may run before the probe
checks.

## Reset semantics

`GmStore.reset()`:

- removes processors and weak action listeners;
- restores the default exception handler;
- does **not** retain slice versions.

Re-fetch `GmStore.slice(type)` after reset, processor replacement or
`removeProcessor(type)`. Cached slice handles become detached.

If a test installs a custom exception handler, reinstall it after `reset()`.

## Dynamic rebuilds and stable ids

The id registry is live for the lifetime of a build. Registering the same id on
a **different** node instance throws `IllegalArgumentException`. Rebuilding a
subtree, timeline row or inspector panel with reused stable ids will therefore
fail unless you either:

- reuse the same node instances, or
- build a fresh `GmContext`/`GmRoot` for the rebuilt subtree.

Ids are captured when a node is added; changing `setId(...)` later does not
update the registry, and ids are not unregistered when nodes leave the scene.

## Thread confinement

The store is not thread-safe. Dispatch, `snapshot()`, `state()`, slice reads and
listener registration must run on one thread, normally the JavaFX Application
Thread in UI code. Gemory does not enforce this because state-only tests must run
without a toolkit. If a probe can be called from a worker, assert
`Platform.isFxApplicationThread()` at the probe boundary.

`GmSlice.markChanged()` is valid only during reducer application. It throws
`IllegalStateException` outside a reducer, including from action listeners.

## Non-`Node` controls

Only `Node` ids are registered. `MenuItem`/`CheckMenuItem` and other non-`Node`
controls cannot be looked up through `GmRoot` yet. For menu-driven probes, have
the menu handler dispatch an action and dispatch the same action directly from
the test/probe.

## Partial mutations on failure

A reducer may mutate state in place and then throw. The processor rolls the slice
version back, so a failed pass does not advance `changed()` or `version()`, but
the state object is **not** restored. Treat failed reducers as leaving
potentially partial state.

## Dispatch order

For each dispatch:

1. all registered processors apply the action and may mark their slices;
2. typed weak `GmStore.on(...)` action listeners run.

There are no state/slice listener callbacks; probes read the slice and state
after the dispatch.

## Headless environments

The UI spec skips itself when `java.awt.GraphicsEnvironment.isHeadless()` is
true, which is the normal GitHub Actions environment. The store and node-id
specs still run.

Useful commands:

```bash
# Run everything; UI specs run only when a display exists
./gradlew test

# Force the UI spec to skip on a desktop machine
./gradlew test -Dgemory.test.desktop=false
# or, for the whole test JVM:
JAVA_TOOL_OPTIONS=-Djava.awt.headless=true ./gradlew test

# Run UI tests on a headless Linux host
xvfb-run -a ./gradlew test
```

If your OpenJFX distribution includes Monocle, it can also be used with
`-Dglass.platform=Monocle -Dmonocle.platform=Headless -Dprism.order=sw`.
Xvfb is the more portable option.

## Manual demo

The old windowed demo is still available, but it is no longer a JUnit test.

Run it from the IDE:

```
Run io.vacco.gemory.GmSpecShell.main from your IDE.
```

Or from the command line on a desktop machine:

```bash
./gradlew runDemo
```

The `runDemo` task uses the test classpath and moves the JavaFX jars onto the
module path. It is desktop-only and is not part of `./gradlew test`.

It opens the sample view with the Nord theme and the original controls. The
automated button-click tests use the same `ViewFoo` and stable ids
(`submit`, `toggle`, `momo`) without showing a window.
