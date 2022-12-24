package org.stanford.ncbo.owlapi.wrapper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.File;

import static org.junit.Assert.*;

public class OntologyParserTest {

    @Before
    public void setUp() throws Exception {

    }
    
    // Run this test: mvn -Dtest=OntologyParserTest#sifrTestParse test
    // parse_OntologyENVO_ReturnsTrue fonctionne pour comparer
    @Test
    public void sifrTestParse() throws Exception {
        ParserInvocation pi = new ParserInvocation("./src/test/resources/repo/input/sifr",
                "./src/test/resources/repo/output/sifr", "foodon.owl", true);
        // SKOS: anaee-france-thesaurus.rdf
        // OWL: AnimalDiseasesOntology.owl.xml
        OntologyParser parser = new OntologyParser(pi);
        parser.parse();
        //assertFalse(parser.parse());
    }

    @Test
    public void getLocalOntologies_MultipleOntologies_Found() throws Exception {
        ParserInvocation pi = new ParserInvocation("./src/test/resources/repo/input/hsdb",
                "./src/test/resources/repo/output/hsdb", "HSDB_OCRe.owl", true);
        OntologyParser parser = new OntologyParser(pi);
        parser.parse();
        assertEquals(8, parser.getLocalOntologies().size());
    }

    @Test
    public void getLocalOntologies_SingleOntology_Found() throws Exception {
        ParserInvocation pi = new ParserInvocation("./src/test/resources/repo/input/bvga",
                "./src/test/resources/repo/output/bvga", "basic-vertebrate-gross-anatomy_v1.1.owl", true);
        OntologyParser parser = new OntologyParser(pi);
        parser.parse();
        assertEquals(1, parser.getLocalOntologies().size());
    }

    @Test
    public void getParsedOntologies_MultipleOntologies_Found() throws Exception {
        ParserInvocation pi = new ParserInvocation("./src/test/resources/repo/input/hsdb",
                "./src/test/resources/repo/output/hsdb", "HSDB_OCRe.owl", true);
        OntologyParser parser = new OntologyParser(pi);
        parser.parse();
        assertEquals(8, parser.getParsedOntologies().size());
    }

    @Test
    public void getParsedOntologies_SingleOntology_Found() throws Exception {
        ParserInvocation pi = new ParserInvocation("./src/test/resources/repo/input/bvga",
                "./src/test/resources/repo/output/bvga", "basic-vertebrate-gross-anatomy_v1.1.owl", true);
        OntologyParser parser = new OntologyParser(pi);
        parser.parse();
        assertEquals(1, parser.getParsedOntologies().size());
    }

    @Test
    public void parse_OntologyBVGA_ReturnsTrue() throws Exception {
        ParserInvocation pi = new ParserInvocation("./src/test/resources/repo/input/bvga",
                "./src/test/resources/repo/output/bvga", "basic-vertebrate-gross-anatomy_v1.1.owl", true);
        OntologyParser parser = new OntologyParser(pi);
        assertTrue(parser.parse());
    }

    @Test
    public void parse_OntologyBVGA_Serialized() throws Exception {
        String inputFolder = "./src/test/resources/repo/input/bvga";
        String outputFolder = "./src/test/resources/repo/output/bvga";

        File f = new File(outputFolder + File.separator + "owlapi.xrdf");
        if (f.exists()) f.delete();

        ParserInvocation pi = new ParserInvocation(inputFolder, outputFolder,
                "basic-vertebrate-gross-anatomy_v1.1.owl", true);
        OntologyParser parser = new OntologyParser(pi);
        parser.parse();

        f = new File(outputFolder + File.separator + "owlapi.xrdf");
        assertTrue(f.exists());
        assertNotEquals(0, f.length());
    }

    @Test
    public void parse_OntologyCNO_ReturnsTrue() throws Exception {
        ParserInvocation pi = new ParserInvocation("./src/test/resources/repo/input/cno",
                "./src/test/resources/repo/output/cno", "cnov0_5.owl", true);
        OntologyParser parser = new OntologyParser(pi);
        assertTrue(parser.parse());
    }

    @Test
    public void parse_OntologyEDAM_ReturnsTrue() throws Exception {
        ParserInvocation pi = new ParserInvocation("./src/test/resources/repo/input/edam",
                "./src/test/resources/repo/output/edam", "EDAM_1.16.owl", true);
        OntologyParser parser = new OntologyParser(pi);
        assertTrue(parser.parse());
    }

    @Test
    public void parse_OntologyENVO_ReturnsTrue() throws Exception {
        ParserInvocation pi = new ParserInvocation("./src/test/resources/repo/input/envo",
                "./src/test/resources/repo/output/envo", "envo-basic.obo", true);
        OntologyParser parser = new OntologyParser(pi);
        assertTrue(parser.parse());
    }

    @Test
    public void parse_OntologyIDODEN_ReturnsTrue() throws Exception {
        ParserInvocation pi = new ParserInvocation("./src/test/resources/repo/input/idon",
                "./src/test/resources/repo/output/idon", "idoden_beta0.15b.owl", true);
        OntologyParser parser = new OntologyParser(pi);
        assertTrue(parser.parse());
    }

    @Test
    public void parse_OntologySNOMEDORG_ReturnsTrue() throws Exception {
        ParserInvocation pi = new ParserInvocation("./src/test/resources/repo/input/snomedorg",
                "./src/test/resources/repo/output/snomedorg", "snomed_organism_1.1.owl", true);
        OntologyParser parser = new OntologyParser(pi);
        assertTrue(parser.parse());
    }

    @Test
    public void parse_OntologyWithEmbeddedXML_ReturnsTrue() throws Exception {
        ParserInvocation pi = new ParserInvocation("./src/test/resources/repo/input/misc",
                "./src/test/resources/repo/output/embedded_xml", "testXMLLiteral.owl", true);
        OntologyParser parser = new OntologyParser(pi);
        assertTrue(parser.parse());
    }


    @Test
    public void parse_DetectsOntologyIRI_ReturnsTrue() throws Exception {
        String outputRepositoryFolder = "./src/test/resources/repo/output/cno";
        ParserInvocation pi = new ParserInvocation("./src/test/resources/repo/input/cno",
                outputRepositoryFolder, "cnov0_5.owl", true);
        assertTrue(pi.valid());

        OntologyParser parser = new OntologyParser(pi);
        assertTrue(parser.parse());
        assertEquals(1, parser.getLocalOntologies().size());

        IRI targetIRI = parser.getTargetOwlOntology().getOntologyID().getOntologyIRI().orNull();
        IRI sourceIRI = parser.getParsedOntologies().stream().findFirst().get().getOntologyID().getOntologyIRI().orNull();
        assertNotNull(targetIRI);
        assertEquals(sourceIRI, targetIRI);

    }
    @Test
    public void parse_OntologyAnnotationCount_ReturnsTrue() throws Exception {
        String outputRepositoryFolder = "./src/test/resources/repo/output/cno";
        ParserInvocation pi = new ParserInvocation("./src/test/resources/repo/input/cno",
                outputRepositoryFolder, "cnov0_5.owl", true);
        assertTrue(pi.valid());

        OntologyParser parser = new OntologyParser(pi);
        assertTrue(parser.parse());
        assertEquals(1, parser.getLocalOntologies().size());

        OWLOntology targetOwlOntology = parser.getTargetOwlOntology();
        OWLOntology sourceOntology = parser.getParsedOntologies().stream().findFirst().orElse(null);

        assertNotNull(sourceOntology);
        assertEquals(sourceOntology.getAnnotations().size(), targetOwlOntology.getAnnotations().size());

    }


    @After
    public void tearDown() throws Exception {

    }

}