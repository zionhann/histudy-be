package edu.handong.csee.histudy.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DFS<T> {
  private final Set<T> visited = new HashSet<>();
  private final Map<T, List<T>> graph;

  public DFS(Map<T, List<T>> graph) {
    this.graph = graph;
  }

  public List<List<T>> execute() {
    List<List<T>> results = new ArrayList<>();

    for (T node : graph.keySet()) {
      if (!visited.contains(node)) {
        List<T> component = new ArrayList<>();
        traverse(node, component);

        if (component.size() > 1) {
          results.add(component);
        }
      }
    }
    return results;
  }

  private void traverse(T current, List<T> component) {
    visited.add(current);
    component.add(current);

    for (T node : graph.getOrDefault(current, List.of())) {
      if (!visited.contains(node)) {
        traverse(node, component);
      }
    }
  }
}
