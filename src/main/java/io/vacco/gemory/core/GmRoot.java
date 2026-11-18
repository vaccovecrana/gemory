package io.vacco.gemory.core;

import javafx.scene.Node;
import java.util.*;

/**
 * Root node plus the stable-id registry built with it.
 *
 * <p>
 *   The id map is an unmodifiable, live view of the registry retained by the build. Nodes
 *   registered after construction (for example by dynamic content) are visible through
 *   {@link #lookup(String)} as long as the registry is alive.
 * </p>
 *
 * @param root      root node
 * @param nodesById node id to node map
 * @since 0.7.0
 */
public record GmRoot(Node root, Map<String, Node> nodesById) {

  public GmRoot {
    Objects.requireNonNull(root, "root");
    Objects.requireNonNull(nodesById, "nodesById");
    nodesById = Collections.unmodifiableMap(nodesById);
  }

  /**
   * Looks up a node by id.
   *
   * @param id node id
   * @return the registered node, if present
   */
  public Optional<Node> lookup(String id) {
    if (id == null || id.isBlank()) {
      return Optional.empty();
    }
    return Optional.ofNullable(nodesById.get(id));
  }

  /**
   * Looks up a node by id and checks its type.
   *
   * @param id   node id
   * @param type expected node type
   * @param <N>  node type
   * @return the registered node if it is an instance of {@code type}
   */
  public <N extends Node> Optional<N> lookup(String id, Class<N> type) {
    Objects.requireNonNull(type, "node type");
    return lookup(id).filter(type::isInstance).map(type::cast);
  }
}
