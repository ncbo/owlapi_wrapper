package org.stanford.ncbo.owlapi.wrapper.util;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.stanford.ncbo.owlapi.wrapper.metrics.Graph;
import org.stanford.ncbo.owlapi.wrapper.metrics.Node;

import java.util.HashSet;
import java.util.Set;

public class OntologyBasedGraph extends Graph
{
    private OWLOntology ontology;

    static public OntologyBasedGraph getGraph(OWLOntology ontology) {
        OntologyBasedGraph graph = new OntologyBasedGraph(ontology);
        graph.initEquivalenceClasses();
        return graph;
    }

    private OntologyBasedGraph(OWLOntology ontology) {
        this.ontology = ontology;
    }

    @Override
    public Set<Node> getNodes() {
        Set<Node> nodes = new HashSet<>();
        Set<OWLClass> classes = ontology.getClassesInSignature();
        for (OWLClass clazz : classes) {
            OntologyBasedNode node = new OntologyBasedNode(this, clazz, false);
            nodes.add(node);
        }
        return nodes;
    }

    @Override
    public Set<Node> getSubNodes(Node x) {
        x.checkValid(this);
        OntologyBasedNode y = (OntologyBasedNode) x;
        Set<Node> nodes = new HashSet<>();
        OWLClass superClazz = y.getClazz();
        Set<OWLSubClassOfAxiom> axioms = ontology.getSubClassAxiomsForSuperClass(superClazz);
        for (OWLSubClassOfAxiom axiom : axioms) {
            OWLClassExpression oce = axiom.getSubClass();
            if (oce.isNamed()) {
                OWLClass clazz = oce.asOWLClass();
                OntologyBasedNode node = new OntologyBasedNode(this, clazz);
                nodes.add(node);
            }
        }
        return nodes;

    }
}
