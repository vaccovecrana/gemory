package io.vacco.gemory.messaging;

public class GmProcessor<S> {

  private S state;
  private GmReducer<S> reducer;

  public void apply(GmAction<?> act) {
    this.state = reducer.reduce(act, state);
  }

  public static <T> GmProcessor<T> of(GmReducer<T> r, T init) {
    GmProcessor<T> p = new GmProcessor<>();
    p.reducer = r;
    p.state = init;
    return p;
  }

  public S getState() { return state; }
}
