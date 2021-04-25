package io.vacco.gemory;

import io.vacco.gemory.redux.*;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;

import static j8spec.J8Spec.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class ReduxSpec {

  public enum CounterActionType { INCREASE, DECREASE }

  public static class CounterAction extends Action<CounterActionType, Integer> {
    public CounterAction(CounterActionType type, Integer value) {
      super(type, value);
    }
  }

  public static final Middleware<CounterActionType, Integer> print1 = ((store, action, next) -> {
    System.out.println("Print 1 start");
    action = next.apply(action);
    System.out.println("Print 1 end");
    return action;
  });

  public static final Middleware<CounterActionType, Integer> print2 = ((store, action, next) -> {
    System.out.println("Print 2 start");
    action = next.apply(action);
    System.out.println("Print 2 end");
    return action;
  });

  static {
    it("Logs a counter's increase/decrease actions", () -> {
      Store<CounterActionType, Integer> counterStore = new Store<>(
          (action, currentState) -> {
            if (action instanceof CounterAction) {
              CounterAction ca = (CounterAction) action;
              switch (ca.type) {
                case INCREASE: return currentState + ca.payload;
                case DECREASE: return currentState - ca.payload;
              }
            }
            return currentState;
          }, 0,
          new Logger<>(a -> System.out.printf("%s%n", a), System.out::println),
          print1, print2
      );

      counterStore.dispatch(new CounterAction(CounterActionType.INCREASE, 1));
      counterStore.dispatch(new CounterAction(CounterActionType.DECREASE, 1));
    });
  }

}
