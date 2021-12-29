package io.vacco.gemory.messaging;

import java.util.function.BiFunction;
import java.util.function.Function;

public class GmSelector<R, S> implements GmReducer<R> {

  private final BiFunction<R, S, R> merger;
  private final Function<R, S> mapper;
  private final GmReducer<S> reducer;

  public GmSelector(Function<R, S> mapper, GmReducer<S> reducer, BiFunction<R, S, R> merger) {
    this.mapper = mapper;
    this.reducer = reducer;
    this.merger = merger;
  }

  @Override public R reduce(GmAction<?> action, R rootState) {
    return merger.apply(rootState, reducer.reduce(action, mapper.apply(rootState)));
  }

  @SafeVarargs
  public static <A extends Enum<?>, R> GmReducer<R> combineSelectors(GmSelector<R, ?>... selectors) {
    return ((action, currentState) -> {
      for (GmSelector<R, ?> sel : selectors) {
        currentState = sel.reduce(action, currentState);
      }
      return currentState;
    });
  }
}
