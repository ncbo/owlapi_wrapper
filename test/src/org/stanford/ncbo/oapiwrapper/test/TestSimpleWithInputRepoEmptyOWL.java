package org.stanford.ncbo.oapiwrapper.test;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.stanford.ncbo.oapiwrapper.OntologyParser;
import org.stanford.ncbo.oapiwrapper.OntologyParserException;
import org.stanford.ncbo.oapiwrapper.ParserError;
import org.stanford.ncbo.oapiwrapper.ParserInvocation;

public class TestSimpleWithInputRepoEmptyOWL extends TestCase {
	private static final String inputRepositoryFolder = null;
	private static final String outputRepositoryFolder = "./test/repo/output/bvga";
	private static final String masterFileName = "./test/repo/input/bvga/basic-vertebrate-gross-anatomy_v1.1.owl";
	
	private final static Logger log = Logger.getLogger(TestSimpleWithInputRepoEmptyOWL.class .getName()); 

	@Override
    protected void setUp() throws Exception  {
        super.setUp();
    }
	 
	@Test
	public void testInputBasicVertebrateGrossAnatomyOK() {
		ParserInvocation pi = new ParserInvocation(
				inputRepositoryFolder, 
				outputRepositoryFolder,
				masterFileName);
		assertEquals(true, pi.valid());
	}
	
	@Test
	public void testLocalFiles() {
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
