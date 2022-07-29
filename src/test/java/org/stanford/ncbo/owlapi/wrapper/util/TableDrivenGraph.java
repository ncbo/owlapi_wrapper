package org.stanford.ncbo.owlapi.wrapper.util;

import org.stanford.ncbo.owlapi.wrapper.metrics.Graph;
import org.stanford.ncbo.owlapi.wrapper.metrics.Node;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final public class TableDrivenGraph<X> extends Graph {
    Map<X, Set<X>> map;

    static public <Y> TableDrivenGraph<Y> getGraph(Map<Y, Set<Y>> map) {
        TableDrivenGraph<Y> graph = new TableDrivenGraph<>(map);
        graph.initEquivalenceClasses();
        return graph;
    }

    private TableDrivenGraph(Map<X, Set<X>> map) {
        this.map = map;
    }

    public Set<Node> getSubNodes(Node x) {
        TableDrivenNode<X> node = (TableDrivenNode<X>) x;
        X attr = node.getAttr();
        Set<Node> set = new HashSet<>();
        for (X y : map.get(attr)) {
            set.add(new TableDrivenNode<>(this, y));
        }
        return set;
    }

    public Set<Node> getNodes() {
        Set<Node> set = new HashSet<>();
        for (X x : map.keySet()) {
            set.add(new TableDrivenNode<X>(this, x, false));
        }
        return set;
    }
}
