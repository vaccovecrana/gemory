package io.vacco.gemory.messaging;

import org.slf4j.*;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class GmStore {

  private static final Logger log = LoggerFactory.getLogger(GmStore.class);

  // Action types to consumers of actions.
  private static final Map<Class<?>, List<WeakReference<Consumer<GmAction<?>>>>> actIdx = new ConcurrentHashMap<>();
  // State types to state processors.
  private static final Map<Class<?>, GmProcessor<?>> prcIdx = new ConcurrentHashMap<>();

  private static Consumer<Throwable> exceptionHandler = e -> log.error("Action processing cycle error.", e);

  @SuppressWarnings("unchecked")
  public static <A extends GmAction<?>> Consumer<A> on(Class<A> pattern, Consumer<A> c) {
    log.info("Adding action listener: [{}] -> [{}]", pattern.getSimpleName(), c);
    WeakReference<Consumer<GmAction<?>>> wc = new WeakReference<>((Consumer<GmAction<?>>) c);
    actIdx.computeIfAbsent(pattern, p -> new ArrayList<>()).add(wc);
    return c;
  }

  @SuppressWarnings("unchecked")
  public static <S> void inState(Class<S> stc, Consumer<S> c) {
    GmProcessor<?> p = prcIdx.get(stc);
    if (p != null) {
      c.accept((S) p.getState());
    }
  }

  public static void useProcessor(GmProcessor<?> p) {
    prcIdx.put(p.getState().getClass(), p);
  }

  public static void dispatch(GmAction<?> act) {
    try {
      for (GmProcessor<?> p : prcIdx.values()) {
        p.apply(act);
      }
      List<WeakReference<Consumer<GmAction<?>>>> cl = actIdx.get(act.getClass());
      if (cl != null && !cl.isEmpty()) {
        for (WeakReference<Consumer<GmAction<?>>> cRef : cl) {
          Consumer<GmAction<?>> c = cRef.get();
          if (c != null) {
            c.accept(act);
          } else if (log.isDebugEnabled()) {
            log.debug("Reference [{}] for action [{}] has been invalidated.", cRef, act.getClass().getSimpleName());
          }
        }
      } else if (log.isDebugEnabled()) {
        log.debug("No listeners found for action [{}]", act.getClass().getSimpleName());
      }
    } catch (Exception e) {
      exceptionHandler.accept(e);
    }
  }

  public static void setExceptionHandler(Consumer<Throwable> eh) { exceptionHandler = eh; }

}
