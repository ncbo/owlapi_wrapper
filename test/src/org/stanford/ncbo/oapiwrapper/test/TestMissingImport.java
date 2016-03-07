package org.stanford.ncbo.oapiwrapper.test;

import java.io.File;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.stanford.ncbo.oapiwrapper.OntologyParser;
import org.stanford.ncbo.oapiwrapper.OntologyParserException;
import org.stanford.ncbo.oapiwrapper.ParserInvocation;

import static org.junit.Assert.assertEquals;

public class TestMissingImport {
	private static final String inputRepositoryFolder = "./test/repo/input/cno";
	private static final String outputRepositoryFolder = "./test/repo/output/cno";
	private static final String masterFileName = "cnov0_5.owl";
	
	private final static Logger log = Logger.getLogger(TestMissingImport.class .getName()); 

	@Before
	public void setUp() throws Exception  {
	}
	

	@Test
	public void testCNOParse() {
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
		boolean parseResult = false;
		try {
			parseResult = parser.parse();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals(true, parseResult);
		assertEquals(1, parser.getLocalOntologies().size());
		log.info("Only ontology " + parser.getLocalOntologies().get(0).toString());
		File f = new File(outputRepositoryFolder + File.separator + "owlapi.xrdf");
		log.info("Output triples in " + f.getAbsolutePath());		
		assertEquals(true, f.exists());
	}

}
