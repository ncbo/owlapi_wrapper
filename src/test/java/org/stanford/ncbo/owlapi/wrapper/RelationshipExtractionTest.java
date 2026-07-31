package org.stanford.ncbo.owlapi.wrapper;

import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Verifies that class-to-class (and class-to-individual) relationships are
 * extracted from a range of axiom shapes -- not just SubClassOf(A, some(p, B)) --
 * and for ANY object property (no OBO privilege). The parser emits each
 * relationship as a ground triple subject --p--> filler using the property's own
 * IRI. Because the property is declared an object property in the export, on
 * reload these triples come back as ObjectPropertyAssertion axioms, which the test
 * reads directly.
 */
public class RelationshipExtractionTest {

    private static final String NS = "http://example.org/relations#";

    private Set<String> extractedRelationships() throws Exception {
        ParserInvocation pi = new ParserInvocation(
                "./src/test/resources/repo/input/relations",
                "./src/test/resources/repo/output/relations",
                "relations.ttl", true);
        assertTrue("parse failed", new OntologyParser(pi).parse());

        OWLOntology out = OWLManager.createOWLOntologyManager()
                .loadOntologyFromOntologyDocument(
                        new File("./src/test/resources/repo/output/relations/owlapi.xrdf"));

        Set<String> rels = new HashSet<>();
        for (OWLObjectPropertyAssertionAxiom ax : out.getAxioms(AxiomType.OBJECT_PROPERTY_ASSERTION)) {
            if (!ax.getProperty().isAnonymous()
                    && ax.getSubject().isNamed() && ax.getObject().isNamed()) {
                rels.add(individualOrClassIri(ax.getSubject()) + " "
                        + ax.getProperty().asOWLObjectProperty().getIRI() + " "
                        + individualOrClassIri(ax.getObject()));
            }
        }
        return rels;
    }

    private String individualOrClassIri(OWLIndividual ind) {
        return ind.asOWLNamedIndividual().getIRI().toString();
    }

    private boolean rel(Set<String> rels, String s, String p, String o) {
        return rels.contains(NS + s + " " + NS + p + " " + NS + o);
    }

    @Test
    public void extractsAllShapes() throws Exception {
        Set<String> rels = extractedRelationships();

        // Extracted:
        assertTrue("bare some: A hasPart B", rel(rels, "A", "hasPart", "B"));
        assertTrue("nested intersection: A connectedTo D", rel(rels, "A", "connectedTo", "D"));
        assertTrue("equivalent + intersection: E connectedTo F", rel(rels, "E", "connectedTo", "F"));
        assertTrue("hasValue: A hasColour red", rel(rels, "A", "hasColour", "red"));
        assertTrue("min-cardinality >=1: A adjacentTo D", rel(rels, "A", "adjacentTo", "D"));
        // someValuesFrom with an intersection filler -> a relationship to each named conjunct.
        assertTrue("intersection filler: G hasPart B", rel(rels, "G", "hasPart", "B"));
        assertTrue("intersection filler: G hasPart C1", rel(rels, "G", "hasPart", "C1"));
        // nested intersection filler -> to each named class at any depth.
        assertTrue("nested intersection filler: H hasPart B", rel(rels, "H", "hasPart", "B"));
        assertTrue("nested intersection filler: H hasPart C1", rel(rels, "H", "hasPart", "C1"));
        assertTrue("nested intersection filler: H hasPart C2", rel(rels, "H", "hasPart", "C2"));

        // Not extracted:
        assertFalse("allValuesFrom must not extract", rel(rels, "A", "hasPart", "E"));
        assertFalse("union operand C1 must not extract", rel(rels, "A", "hasPart", "C1"));
        assertFalse("union operand C2 must not extract", rel(rels, "A", "hasPart", "C2"));
        assertFalse("union filler operand B must not extract", rel(rels, "I", "hasPart", "B"));
        assertFalse("union filler operand C1 must not extract", rel(rels, "I", "hasPart", "C1"));
    }
}
