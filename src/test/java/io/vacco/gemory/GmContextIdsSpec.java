package io.vacco.gemory;

import io.vacco.gemory.core.GmContext;
import j8spec.junit.J8SpecRunner;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.junit.runner.RunWith;

import static io.vacco.gemory.core.GmContext.rootWithIds;
import static j8spec.J8Spec.*;
import static org.junit.Assert.*;

@RunWith(J8SpecRunner.class)
public class GmContextIdsSpec {

  static {
    it("registers explicit ids and looks them up", () -> {
      var pane = new Pane();
      var root = rootWithIds(c -> c.node("root-pane", pane));

      assertSame(pane, root.root());
      assertEquals("root-pane", pane.getId());
      assertSame(pane, root.lookup("root-pane").orElseThrow());
      assertTrue(root.lookup("missing").isEmpty());
    });

    it("auto-registers ids set inside builders", () -> {
      var root = rootWithIds(c -> c.vBox((vb, vb$) -> {
        var pane = new Pane();
        pane.setId("inner");
        vb$.node(pane);
      }));

      assertTrue(root.lookup("inner").isPresent());
      assertTrue(root.nodesById().containsKey("inner"));
    });

    it("skips auto-registration for blank ids", () -> {
      var pane = new Pane();
      pane.setId("  ");
      var root = rootWithIds(c -> c.vBox((vb, vb$) -> vb$.node(pane)));

      assertTrue(root.nodesById().isEmpty());
      assertTrue(root.lookup("  ").isEmpty());
    });

    it("shares the id registry across nested contexts", () -> {
      var root = rootWithIds(c -> c.borderPane((bp, bp$) -> bp$.center(c$ -> c$.vBox((vb, vb$) -> {
        var pane = new Pane();
        pane.setId("deep");
        vb$.node(pane);
      }))));

      assertTrue(root.lookup("deep").isPresent());
    });

    it("rejects duplicate ids for different nodes", () -> {
      var first = new Pane();
      var second = new Pane();

      assertThrows(IllegalArgumentException.class, () -> rootWithIds(c -> {
        c.node("dup", first);
        c.node("dup", second);
      }));
    });

    it("allows the same node to be registered twice", () -> {
      var pane = new Pane();
      var root = rootWithIds(c -> {
        c.node("same", pane);
        c.node("same", pane);
      });

      assertSame(pane, root.lookup("same").orElseThrow());
      assertEquals(1, root.nodesById().size());
    });

    it("rejects null or blank explicit ids", () -> {
      var pane = new Pane();
      assertThrows(IllegalArgumentException.class, () -> new GmContext().node(null, pane));
      assertThrows(IllegalArgumentException.class, () -> new GmContext().node("  ", pane));
    });

    it("keeps root() unchanged", () -> {
      Node n = GmContext.root(c -> c.vBox((vb, vb$) -> vb$.node(new Pane())));
      assertTrue(n instanceof VBox);
    });

    it("exposes a live unmodifiable id view", () -> {
      var c = new GmContext();
      c.node("one", new Pane());
      var view = c.nodesById();
      assertEquals(1, view.size());

      c.node("two", new Pane());
      assertEquals(2, view.size());
      assertThrows(UnsupportedOperationException.class, () -> view.put("x", new Pane()));
    });

    it("keeps the GmRoot id view live", () -> {
      var holder = new GmContext[1];
      var root = rootWithIds(c -> {
        holder[0] = c;
        c.node("one", new Pane());
      });

      assertEquals(1, root.nodesById().size());
      holder[0].node("two", new Pane());
      assertEquals(2, root.nodesById().size());
      assertTrue(root.lookup("two").isPresent());
    });

    it("rejects empty roots", () -> {
      assertThrows(IllegalStateException.class, () -> rootWithIds(c -> { }));
    });

    it("supports typed lookups", () -> {
      var pane = new Pane();
      var root = rootWithIds(c -> c.node("pane", pane));

      assertSame(pane, root.lookup("pane", Pane.class).orElseThrow());
      assertTrue(root.lookup("pane", VBox.class).isEmpty());
    });
  }
}
