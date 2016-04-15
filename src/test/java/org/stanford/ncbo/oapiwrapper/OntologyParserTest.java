package org.stanford.ncbo.oapiwrapper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class OntologyParserTest {

    @Before
    public void setUp() throws Exception {

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
        ParserInvocation pi = new ParserInvocation(inputFolder, outputFolder,
                "basic-vertebrate-gross-anatomy_v1.1.owl", true);
        OntologyParser parser = new OntologyParser(pi);
        parser.parse();
        File f = new File(outputFolder + File.separator + "owlapi.xrdf");
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
    public void parse_OntologyEDAM_ReturnsFalse() throws Exception {
        ParserInvocation pi = new ParserInvocation("./src/test/resources/repo/input/edam",
                "./src/test/resources/repo/output/edam", "EDAM_1.1_v1.1.obo", true);
        OntologyParser parser = new OntologyParser(pi);
        assertFalse(parser.parse());
    }

    @Test
    public void parse_OntologyIDODEN_ReturnsTrue() throws Exception {
        ParserInvocation pi = new ParserInvocation("./src/test/resources/repo/input/idon",
                "./src/test/resources/repo/output/idon", "idoden110712_alpha0.001_v0.001.owl", true);
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

    @After
    public void tearDown() throws Exception {

    }

}