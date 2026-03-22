package main;

import java.util.*;

public class Graph {
    private Map<Integer, Set<Integer>> adjList;

    public Graph() {
        this.adjList = new HashMap<>();
    }

    public void addNode(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException();
        }
        this.adjList.putIfAbsent(id, new HashSet<>());
    }

    public void addEdge(Integer from, Integer to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException();
        }
        addNode(from);
        this.adjList.get(from).add(to);
    }

    public Set<Integer> getReachableNodes(int id) {
        if (!this.adjList.containsKey(id)) {
            return Collections.emptySet();
        }
        Set<Integer> result = new HashSet<>();
        result.add(id);
        collectHyponyms(id, result);
        return result;
    }

    public void collectHyponyms(int id, Set<Integer> result) {
        if (!this.adjList.containsKey(id) || this.adjList.get(id) == null) {
            return;
        }
        for (int node : this.adjList.get(id)) {
            result.add(node);
            collectHyponyms(node, result);
        }
    }
}
