package org.stanford.ncbo.oapiwrapper.test;

import java.io.File;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLOntology;
import org.stanford.ncbo.oapiwrapper.OntologyBean;
import org.stanford.ncbo.oapiwrapper.OntologyParser;
import org.stanford.ncbo.oapiwrapper.OntologyParserException;
import org.stanford.ncbo.oapiwrapper.ParserInvocation;

public class TestMultipleUnzippedOWL extends TestCase {
	private static final String inputRepositoryFolder = "./test/repo/input/hsdb";
	private static final String outputRepositoryFolder = "./test/repo/output/hsdb";
	private static final String masterFileName = "HSDB_OCRe.owl";
	
	private final static Logger log = Logger.getLogger(TestMultipleUnzippedOWL.class.getName()); 

	@Override
    protected void setUp() throws Exception  {
        super.setUp();
    }
	
	@Test
	public void testInputOK() {
		ParserInvocation pi = new ParserInvocation(
				inputRepositoryFolder, 
				outputRepositoryFolder,
				masterFileName); 
		assertEquals(true, pi.valid());
	}
	
	@Test
	public void testParse() {
		ParserInvocation pi = new ParserInvocation(
				inputRepositoryFolder, 
				outputRepositoryFolder,
				masterFileName);
		OntologyParser parser = null;
		try {
			parser = new OntologyParser(pi);
		} catch (OntologyParserException e) {
			System.out.println("Errors " + e.parserLog.toString());
			assert(false);
		}
		boolean ok = false;
		try {
			ok = parser.parse();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.info(e.toString());
			e.printStackTrace();
		}
		log.info("after parse");		
		assertEquals(8, parser.getLocalOntologies().size());
		assertEquals(true, ok);
		log.info("Only ontology " + parser.getLocalOntologies().get(0).toString());
		File f = new File(outputRepositoryFolder + File.separator + "owlapi.xrdf");
		log.info("Output triples in " + f.getAbsolutePath());		
		assertEquals(8, parser.getParsedOntologies().size());
	}
}
