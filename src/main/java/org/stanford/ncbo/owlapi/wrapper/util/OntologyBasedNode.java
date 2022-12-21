package org.stanford.ncbo.owlapi.wrapper.util;

import org.semanticweb.owlapi.model.OWLClass;
import org.stanford.ncbo.owlapi.wrapper.metrics.Node;

public class OntologyBasedNode extends Node {
    private OWLClass clazz;

    OntologyBasedNode(OntologyBasedGraph graph, OWLClass clazz) {
        this(graph, clazz, true);
    }
    OntologyBasedNode(OntologyBasedGraph graph, OWLClass clazz, boolean checkValid) {
        super(graph);
        this.clazz = clazz;
        if (checkValid) {
            checkValid();
        }
    }

    public OWLClass getClazz() {
        return clazz;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) && obj instanceof OntologyBasedNode &&
                clazz.equals(((OntologyBasedNode) obj).getClazz());
    }

    @Override
    public int hashCode() {
        return clazz.hashCode() + 10 * super.hashCode();
    }

    @Override
    public String toString() {
        return clazz.toString();
    }
}
