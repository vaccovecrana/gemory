package io.vacco.gemory.redux;

import java.util.function.BiFunction;
import java.util.function.Function;

public class Selector<A extends Enum<?>, R, S> implements Reducer<A, R> {

  private final BiFunction<R, S, R> merger;
  private final Function<R, S> mapper;
  private final Reducer<A, S> reducer;

  public Selector(Function<R, S> mapper, Reducer<A, S> reducer, BiFunction<R, S, R> merger) {
    this.mapper = mapper;
    this.reducer = reducer;
    this.merger = merger;
  }

  @Override public R reduce(Action<A, ?> action, R rootState) {
    return merger.apply(rootState, reducer.reduce(action, mapper.apply(rootState)));
  }

  @SafeVarargs
  public static <A extends Enum<?>, R> Reducer<A, R> combineSelectors(Selector<A, R, ?> ... selectors) {
    return ((action, currentState) -> {
      for (Selector<A, R, ?> sel : selectors) {
        currentState = sel.reduce(action, currentState);
      }
      return currentState;
    });
  }
}
