package org.stanford.ncbo.oapiwrapper.test;

import org.junit.Test;
import org.stanford.ncbo.oapiwrapper.OntologyParser;
import org.stanford.ncbo.oapiwrapper.OntologyParserException;
import org.stanford.ncbo.oapiwrapper.ParserInvocation;

import java.io.File;
import java.util.logging.Logger;

import static org.junit.Assert.*;

public class EmbeddedXMLTest {
    private static final Logger log = Logger.getLogger(EmbeddedXMLTest.class.getName());
   	private static final String inputRepositoryFolder = "./test/repo/input/misc";
	private static final String outputRepositoryFolder = "./test/repo/output/misc";
	private static final String masterFileName = "testXMLLiteral.owl";

    @Test
    public void testEmbeddedXML() {
        ParserInvocation pi = new ParserInvocation(inputRepositoryFolder,
                outputRepositoryFolder,
                masterFileName, true);
        assertTrue(pi.valid());

        OntologyParser parser = null;
        try {
            parser = new OntologyParser(pi);
        } catch (OntologyParserException e) {
            System.out.println("Errors " + e.parserLog.toString());
            assertFalse("Invalid parser invocation", false);
        }

        boolean parseResult = false;
        try {
            parseResult = parser.parse();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertTrue(parseResult);
        File f = new File(outputRepositoryFolder + File.separator + "owlapi.xrdf");
        log.info("Output triples to " + f.getAbsolutePath());
        assertTrue(f.exists());
    }
}