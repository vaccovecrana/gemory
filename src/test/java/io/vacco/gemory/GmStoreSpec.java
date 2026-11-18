package io.vacco.gemory;

import io.vacco.gemory.messaging.*;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;

import java.util.*;
import java.util.concurrent.atomic.*;

import static io.vacco.gemory.messaging.GmStore.*;
import static j8spec.J8Spec.*;
import static org.junit.Assert.*;

@RunWith(J8SpecRunner.class)
public class GmStoreSpec {

  public static class CounterState {
    public int value;
  }

  public static class OtherState {
    public int value;
  }

  public static class Inc extends GmAction<Void> { }
  public static class NoOp extends GmAction<Void> { }
  public static class OtherAction extends GmAction<Void> { }

  private static GmProcessor<CounterState> counter() {
    return GmProcessor.of((act, state) -> {
      if (act instanceof Inc) {
        state.value++;
      }
      return state;
    }, new CounterState());
  }

  private static GmProcessor<CounterState> markingCounter() {
    return GmProcessor.of((act, state) -> {
      if (act instanceof Inc) {
        state.value++;
        GmStore.currentSlice().markChanged();
      }
      return state;
    }, new CounterState());
  }

  private static GmProcessor<CounterState> doubleMarkingCounter() {
    return GmProcessor.of((act, state) -> {
      if (act instanceof Inc) {
        state.value++;
        GmStore.currentSlice().markChanged();
        GmStore.currentSlice().markChanged();
      }
      return state;
    }, new CounterState());
  }

  private static GmProcessor<CounterState> markingBoom() {
    return GmProcessor.of((act, state) -> {
      if (act instanceof Inc) {
        state.value++;
        GmStore.currentSlice().markChanged();
        throw new IllegalStateException("boom");
      }
      return state;
    }, new CounterState());
  }

  private static GmProcessor<OtherState> markingOther() {
    return GmProcessor.of((act, state) -> {
      if (act instanceof OtherAction) {
        state.value++;
        GmStore.currentSlice().markChanged();
      }
      return state;
    }, new OtherState());
  }

  static {
    beforeEach(GmStore::reset);
    afterEach(GmStore::reset);
  }

  static {
    it("starts slices clean", () -> {
      useProcessor(counter());
      var slice = slice(CounterState.class);

      assertEquals(0, slice.version());
      assertFalse(slice.changed());
      assertFalse(slice.changedSince(0));
    });

    it("marks one changed pass", () -> {
      useProcessor(markingCounter());
      var slice = slice(CounterState.class);

      dispatch(new Inc());

      assertEquals(1, state(CounterState.class).value);
      assertTrue(slice.changed());
      assertEquals(1, slice.version());
      assertTrue(slice.changedSince(0));
      assertFalse(slice.changedSince(1));
    });

    it("clears changed() on the next untouched pass", () -> {
      useProcessor(markingCounter());
      var slice = slice(CounterState.class);

      dispatch(new Inc());
      assertTrue(slice.changed());

      dispatch(new NoOp());

      assertFalse(slice.changed());
      assertEquals(1, slice.version());
      assertTrue(slice.changedSince(0));
    });

    it("counts multiple marks in one pass once", () -> {
      useProcessor(doubleMarkingCounter());
      var slice = slice(CounterState.class);

      dispatch(new Inc());

      assertEquals(1, slice.version());
      assertTrue(slice.changed());
    });

    it("does not advance slices when the reducer does not mark", () -> {
      useProcessor(counter());
      var slice = slice(CounterState.class);

      dispatch(new Inc());

      assertEquals(1, state(CounterState.class).value);
      assertEquals(0, slice.version());
      assertFalse(slice.changed());
    });

    it("rolls the slice back when a reducer throws", () -> {
      var errors = new ArrayList<Throwable>();
      setExceptionHandler(errors::add);
      useProcessor(markingBoom());
      var slice = slice(CounterState.class);

      dispatch(new Inc());

      assertEquals(1, errors.size());
      assertEquals(1, state(CounterState.class).value);
      assertEquals(0, slice.version());
      assertFalse(slice.changed());
    });

    it("exposes the active slice inside a reducer", () -> {
      var seen = new AtomicReference<GmSlice<?>>();
      var p = GmProcessor.of((act, state) -> {
        seen.set(GmStore.currentSlice());
        GmStore.currentSlice().markChanged();
        return state;
      }, new CounterState());
      useProcessor(p);
      var slice = slice(CounterState.class);

      dispatch(new NoOp());

      assertSame(slice, seen.get());
      assertTrue(slice.changed());
      assertEquals(1, slice.version());
    });

    it("rejects currentSlice() outside a reducer", () -> {
      assertThrows(IllegalStateException.class, GmStore::currentSlice);
    });

    it("rejects markChanged() outside a reducer", () -> {
      useProcessor(counter());
      assertThrows(IllegalStateException.class, () -> slice(CounterState.class).markChanged());
    });

    it("rejects marking another processor's slice", () -> {
      var errors = new ArrayList<Throwable>();
      setExceptionHandler(errors::add);
      var bad = GmProcessor.of((act, state) -> {
        GmStore.slice(OtherState.class).markChanged();
        return state;
      }, new CounterState());
      useProcessor(bad);
      useProcessor(markingOther());

      dispatch(new NoOp());

      assertEquals(1, errors.size());
      assertTrue(errors.getFirst() instanceof IllegalStateException);
      assertEquals(0, slice(CounterState.class).version());
      assertEquals(0, slice(OtherState.class).version());
    });

    it("restores the active slice after a nested dispatch", () -> {
      var nested = new AtomicBoolean();
      var seen = new AtomicReference<GmSlice<?>>();
      var p = GmProcessor.of((act, state) -> {
        if (act instanceof Inc && nested.compareAndSet(false, true)) {
          GmStore.dispatch(new NoOp());
          seen.set(GmStore.currentSlice());
        }
        GmStore.currentSlice().markChanged();
        return state;
      }, new CounterState());
      useProcessor(p);
      var slice = slice(CounterState.class);

      dispatch(new Inc());

      assertSame(slice, seen.get());
      // Nested dispatch from a reducer is unsupported; this pins the current
      // active-slice restoration and single-increment behavior.
      assertEquals(1, slice.version());
    });

    it("preserves processor registration order", () -> {
      useProcessor(counter());
      useProcessor(markingOther());

      assertEquals(
          List.of(CounterState.class, OtherState.class),
          new ArrayList<>(snapshot().keySet())
      );
    });

    it("rejects slices for unregistered state types", () -> {
      assertThrows(IllegalArgumentException.class, () -> slice(CounterState.class));
    });

    it("returns an immutable snapshot of all states", () -> {
      useProcessor(counter());
      useProcessor(markingOther());
      dispatch(new Inc());

      var snap = snapshot();

      assertEquals(2, snap.size());
      assertSame(state(CounterState.class), snap.get(CounterState.class));
      assertThrows(UnsupportedOperationException.class,
          () -> snap.put(CounterState.class, new CounterState()));
    });

    it("resets processors and slices", () -> {
      useProcessor(markingCounter());
      dispatch(new Inc());

      reset();

      assertTrue(snapshot().isEmpty());
      assertThrows(IllegalArgumentException.class, () -> slice(CounterState.class));
    });

    it("removes processors and slices", () -> {
      useProcessor(markingCounter());

      assertTrue(removeProcessor(CounterState.class));
      assertFalse(removeProcessor(CounterState.class));
      assertTrue(snapshot().isEmpty());
      assertThrows(IllegalArgumentException.class, () -> slice(CounterState.class));
    });

    it("reads typed state", () -> {
      useProcessor(counter());
      dispatch(new Inc());
      assertEquals(1, state(CounterState.class).value);
    });

    it("keeps weak action listeners working", () -> {
      useProcessor(counter());
      var calls = new AtomicInteger();
      var listener = on(Inc.class, act -> calls.incrementAndGet());

      dispatch(new Inc());

      assertEquals(1, calls.get());
      assertNotNull(listener);
    });

    it("isolates processor failures", () -> {
      var errors = new ArrayList<Throwable>();
      setExceptionHandler(errors::add);
      var bad = GmProcessor.of((act, state) -> {
        throw new IllegalStateException("bad");
      }, new CounterState());
      useProcessor(bad);
      useProcessor(markingOther());

      dispatch(new OtherAction());

      assertEquals(1, errors.size());
      assertEquals(0, slice(CounterState.class).version());
      assertEquals(1, slice(OtherState.class).version());
      assertTrue(slice(OtherState.class).changed());
    });

    it("rejects markChanged() from an action listener", () -> {
      useProcessor(counter());
      var slice = slice(CounterState.class);
      var listener = on(Inc.class, act ->
          assertThrows(IllegalStateException.class, slice::markChanged));

      dispatch(new Inc());

      assertNotNull(listener);
    });
  }
}
