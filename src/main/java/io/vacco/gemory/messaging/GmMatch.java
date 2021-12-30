package io.vacco.gemory.messaging;

import java.util.*;
import java.util.function.*;

public class GmMatch<S> {

  private final Map<Class<?>, Function<?, S>> matchFnIdx = new HashMap<>();
  private Supplier<S> defaultFn;

  public <A extends GmAction<?>> GmMatch<S> on(Class<? extends A> pattern, Function<A, S> cn) {
    matchFnIdx.put(pattern, cn);
    return this;
  }

  public GmMatch<S> orElse(Supplier<S> defaultFn) {
    this.defaultFn = defaultFn;
    return this;
  }

  @SuppressWarnings("unchecked")
  public <A extends GmAction<?>> S apply(A action) {
    Function<A, S> fn = (Function<A, S>) matchFnIdx.get(action.getClass());
    if (fn != null) { return fn.apply(action); }
    else if (defaultFn != null) { return defaultFn.get(); }
    String msg = String.format(
        "No action handler defined for action [%s], and no fallback handler provided. Registered handlers: %s",
        action.getClass(), matchFnIdx
    );
    throw new IllegalStateException(msg);
  }

}
