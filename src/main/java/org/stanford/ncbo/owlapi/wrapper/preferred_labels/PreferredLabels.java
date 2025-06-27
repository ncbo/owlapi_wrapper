package org.stanford.ncbo.owlapi.wrapper.preferred_labels;

import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stanford.ncbo.owlapi.wrapper.ParserInvocation;
import org.stanford.ncbo.owlapi.wrapper.metrics.OntologyMetrics;

public class PreferredLabels {
    private static final Logger logger = LoggerFactory.getLogger(OntologyMetrics.class);

    private OWLOntology ontology;

    private ParserInvocation parserInvocation;

    public PreferredLabels(OWLOntology ontology, ParserInvocation parserInvocation) {
        this.ontology = ontology;
        this.parserInvocation = parserInvocation;
    }
}
