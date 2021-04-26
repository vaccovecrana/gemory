package io.vacco.gemory;

import io.vacco.gemory.redux.*;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static j8spec.J8Spec.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class ReduxSpec {

  public enum CounterActionType {Increase, Decrease}

  public static class CounterAction extends Action<CounterActionType, Integer> {}

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

  public static final Middleware<CounterActionType, Integer> logger = new Logger<>(a -> System.out.printf("%s%n", a), System.out::println);

  static {
    it("Logs a counter's increase/decrease actions", () -> {
      Store<CounterActionType, Integer> counterStore = new Store<>(
          (action, currentState) -> {
            if (action instanceof CounterAction) {
              CounterAction ca = (CounterAction) action;
              switch (ca.type) {
                case Increase: return currentState + ca.payload;
                case Decrease: return currentState - ca.payload;
              }
            }
            return currentState;
          }, 0, logger, print1, print2
      );
      counterStore.dispatch(new CounterAction().withType(CounterActionType.Increase).withPayload(1));
      counterStore.dispatch(new CounterAction().withType(CounterActionType.Decrease).withPayload(1));
    });
  }

  public static class Ingredient {}
  public static final class Tomato extends Ingredient {}
  public static final class Lettuce extends Ingredient {}
  public static final class Onion extends Ingredient {}
  public static final class Beef extends Ingredient {}
  public static final class Chicken extends Ingredient {}
  public static final class Ketchup extends Ingredient {}
  public static final class Mustard extends Ingredient {}
  public static final class Mayo extends Ingredient {}

  public enum Bread {Wheat, White, Flat, Italian}

  public static final class Sandwich {
    public Bread breadType;
    public List<Ingredient> ingredients = new ArrayList<>();
  }

  public enum SandwichActionType { AddIngredient, SelectBread }

  public static final class AddIngredientAction extends Action<SandwichActionType, Ingredient> {}
  public static final class SelectBreadAction extends Action<SandwichActionType, Bread> {}

  public static final Middleware<SandwichActionType, Sandwich> sandwichLogger =
      new Logger<>(a -> System.out.printf("%s%n", a), System.out::println);

  static {
    it("Makes a sandwich", () -> {
      Store<SandwichActionType, Sandwich> sandwichStore = new Store<>(
          Selector.combineSelectors(
              new Selector<>(
                  sandwich -> sandwich.breadType,
                  (action, state) -> {
                    if (action instanceof SelectBreadAction) {
                      SelectBreadAction sba = (SelectBreadAction) action;
                      return sba.payload;
                    }
                    return state;
                  },
                  (sandwich, bread) -> {
                    sandwich.breadType = bread;
                    return sandwich;
                  }
              ),
              new Selector<>(
                  sandwich -> sandwich.ingredients,
                  (action, state) -> {
                    if (action instanceof AddIngredientAction) {
                      AddIngredientAction aa = (AddIngredientAction) action;
                      state.add(aa.payload);
                    }
                    return state;
                  },
                  (sandwich, ingredients) -> {
                    sandwich.ingredients = ingredients;
                    return sandwich;
                  }
              )
          ), new Sandwich(), sandwichLogger
      );

      sandwichStore.dispatch(new SelectBreadAction().withType(SandwichActionType.SelectBread).withPayload(Bread.Flat));
      for (Ingredient ing : new Ingredient[]{new Beef(), new Mayo(), new Onion(), new Lettuce(), new Tomato(), new Ketchup()}) {
        sandwichStore.dispatch(new AddIngredientAction().withType(SandwichActionType.AddIngredient).withPayload(ing));
      }

      System.out.println();
    });
  }

}
