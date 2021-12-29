package io.vacco.gemory.messaging;

import java.util.*;
import java.util.function.*;

public class GmMatch<S> {

  private final Map<Class<?>, Function<?, S>> matchFn = new HashMap<>();

  public <A extends GmAction<?>> GmMatch<S> on(Class<? extends A> pattern, Function<A, S> cn) {
    matchFn.put(pattern, cn);
    return this;
  }

  @SuppressWarnings("unchecked")
  public <A extends GmAction<?>> S apply(A action) {
    Function<A, S> fn = (Function<A, S>) matchFn.get(action.getClass());
    if (fn != null) { return fn.apply(action); }
    return null;
  }

}
