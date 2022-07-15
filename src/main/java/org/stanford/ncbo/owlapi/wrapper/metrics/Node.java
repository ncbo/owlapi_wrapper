package org.stanford.ncbo.owlapi.wrapper.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Node {
    private static final Logger logger = LoggerFactory.getLogger(Node.class);

    final private Graph graph;

    public Node(Graph graph) {
        this.graph = graph;
    }

    public void checkValid(Graph graph) {
        checkValid();
        if (this.graph != graph) {
            throw new IllegalArgumentException("Node " + this + " has the wrong graph.");
        }
    }

    public void checkValid() {
        if (!graph.checkNode(this)) {
            throw new IllegalArgumentException("Node " + this + " is not valid for its graph, " + graph);
        }
    }

    public Graph getGraph() {
        return graph;
    }

    public int maxDepth() {
        return graph.maxDepth(this);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Node && graph.equals(((Node) obj).graph);
    }

    @Override
    public int hashCode() {
        return graph.hashCode();
    }
}

