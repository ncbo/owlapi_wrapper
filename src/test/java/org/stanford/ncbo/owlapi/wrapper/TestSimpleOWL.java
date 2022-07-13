package org.stanford.ncbo.owlapi.wrapper;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.*;

public class TestSimpleOWL {
	private static final String inputRepositoryFolder = "./src/test/resources/repo/input/bvga";
	private static final String outputRepositoryFolder = "./src/test/resources/repo/output/bvga";
	private static final String masterFileName = "basic-vertebrate-gross-anatomy_v1.1.owl";

	private final static Logger log = Logger.getLogger(TestSimpleOWL.class .getName());

	@Before
	public void setUp() {
	}
	 
	@Test
	public void testBVGAParserInvocationOK() {
		ParserInvocation pi = new ParserInvocation(
				inputRepositoryFolder, 
				outputRepositoryFolder,
				masterFileName, true);
		assertTrue(pi.valid());
	}
	
	@Test
	public void testBVGAParserInvocationFoldersOK() {
		// Delete BVGA's output directory
		File out = new File(outputRepositoryFolder);
        if (out.exists()) {
        	try {
				FileUtils.deleteDirectory(out);
			} catch (IOException e) {
				assertFalse(false);
			}
        }
        
		// Restore BVGA's output folder
		ParserInvocation pi = new ParserInvocation(
				inputRepositoryFolder, 
				outputRepositoryFolder,
				masterFileName, true);
		boolean valid = pi.valid();
		if (!valid) {
			log.log(Level.SEVERE,pi.getParserLog().toString());
		}
		assertTrue(valid);

		// Set invalid input folder
		pi.setInputRepositoryFolder("this/does/not/exit");
		assertFalse(pi.valid());
		assertEquals(1, pi.getParserLog().getErrors().size());
		assertEquals(ParserError.INPUT_REPO_MISSING, pi.getParserLog().getErrors().get(0).getParserError());
		assertTrue(new File(outputRepositoryFolder).exists());

		// Restore valid input folder
		pi.setInputRepositoryFolder(inputRepositoryFolder);
		assertTrue(pi.valid());
		assertEquals(0, pi.getParserLog().getErrors().size());
		
		log.info("Output repo created " + out.getAbsolutePath());
		assertTrue(out.exists());
	}
	
	@Test
	public void testBVGAParserInvocationInvalidOutput() {
		ParserInvocation pi = new ParserInvocation(
				inputRepositoryFolder, 
				"/var/zyxx1234xx9",
				masterFileName,true);
		assertFalse(pi.valid());
		assertEquals(1, pi.getParserLog().getErrors().size());
		assertEquals(ParserError.OUPUT_REPO_CANNOT_BE_CREATED, pi.getParserLog().getErrors().get(0).getParserError());
	}
	
	@Test
	public void testLocalFiles() {
		ParserInvocation pi = new ParserInvocation(
				inputRepositoryFolder, 
				outputRepositoryFolder,
				masterFileName,true);
		OntologyParser parser = null;
		try {
			parser = new OntologyParser(pi);
		} catch (OntologyParserException e) {
			System.out.println("Errors " + e.parserLog.toString());
			assertFalse(false);
		}
		boolean parseResult = false;
		try {
			parseResult = parser.parse();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue(parseResult);
		assertEquals(1, parser.getLocalOntologies().size());
		log.info("Only ontology " + parser.getLocalOntologies().get(0).toString());
		File f = new File(outputRepositoryFolder + File.separator + "owlapi.xrdf");
		log.info("Output triples in " + f.getAbsolutePath());		
		assertTrue(f.exists());
	}
	
}
