package org.stanford.ncbo.owlapi.wrapper.util;

import org.stanford.ncbo.owlapi.wrapper.metrics.Node;

final public class TableDrivenNode<X> extends Node {
    final private X attr;

    public TableDrivenNode(TableDrivenGraph<X> graph, X attr) {
        this(graph, attr, true);

    }

    TableDrivenNode(TableDrivenGraph<X> graph, X attr, boolean checkValid) {
        super(graph);
        this.attr = attr;
        if (checkValid) {
            checkValid();
        }
    }

    public X getAttr() {
        return attr;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TableDrivenNode
                && attr.equals(((TableDrivenNode<?>) obj).getAttr())
                && super.equals(obj);
    }

    @Override
    public int hashCode() {
        return 10 * attr.hashCode() + super.hashCode();
    }

    @Override
    public String toString() {
        return "(Node @" + attr + ")";
    }
}