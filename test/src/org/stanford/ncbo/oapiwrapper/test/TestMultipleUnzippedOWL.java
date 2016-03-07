package org.stanford.ncbo.oapiwrapper.test;

import org.junit.Before;
import org.junit.Test;
import org.stanford.ncbo.oapiwrapper.OntologyParser;
import org.stanford.ncbo.oapiwrapper.OntologyParserException;
import org.stanford.ncbo.oapiwrapper.ParserInvocation;

import java.io.File;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

public class TestMultipleUnzippedOWL {
	private static final String inputRepositoryFolder = "./test/repo/input/hsdb";
	private static final String outputRepositoryFolder = "./test/repo/output/hsdb";
	private static final String masterFileName = "HSDB_OCRe.owl";
	
	private final static Logger log = Logger.getLogger(TestMultipleUnzippedOWL.class.getName()); 

	@Before
	public void setUp() throws Exception  {
	}
	
	@Test
	public void testInputOK() {
		ParserInvocation pi = new ParserInvocation(
				inputRepositoryFolder, 
				outputRepositoryFolder,
				masterFileName, true); 
		assertEquals(true, pi.valid());
	}
	
	@Test
	public void testParse() {
		ParserInvocation pi = new ParserInvocation(
				inputRepositoryFolder, 
				outputRepositoryFolder,
				masterFileName, true);
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
