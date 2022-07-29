package org.stanford.ncbo.owlapi.wrapper.experiment;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.stanford.ncbo.owlapi.wrapper.util.OntologyBasedGraph;

import java.io.File;

public class OntologyExperiment {

    public static void main(String[] args) throws OWLOntologyCreationException {
        String path = "src/test/resources/BRO_v3.4.owl";
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(path));
        OntologyBasedGraph graph = OntologyBasedGraph.getGraph(ontology);
        long start = System.currentTimeMillis();
        System.out.println("depth = " + graph.maxDepth());
        System.out.println("Took " + (start-System.currentTimeMillis()) + " ms");
    }
}
