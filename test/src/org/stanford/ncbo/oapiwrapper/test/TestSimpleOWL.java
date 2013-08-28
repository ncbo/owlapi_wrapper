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

public class TestSimpleOWL extends TestCase {
	private static final String inputRepositoryFolder = "./test/repo/input/bvga";
	private static final String outputRepositoryFolder = "./test/repo/output/bvga";
	private static final String masterFileName = "basic-vertebrate-gross-anatomy_v1.1.owl";

	//private static final String inputRepositoryFolder = "./test/repo/input/custom";
	//private static final String outputRepositoryFolder = "./test/repo/output/custom";
	//private static final String masterFileName = "custom_properties.owl";

	
	private final static Logger log = Logger.getLogger(TestSimpleOWL.class .getName()); 

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
	public void testInputBasicVertebrateGrossAnatomyKO() {
        
		File out = new File(outputRepositoryFolder);
        if (out.exists()) {
        	try {
				FileUtils.deleteDirectory(out);
			} catch (IOException e) {
				assert(false);
				
			}
        }
        
		ParserInvocation pi = new ParserInvocation(
				inputRepositoryFolder, 
				outputRepositoryFolder,
				masterFileName);
		boolean valid = pi.valid();
		if (!valid) {
			log.log(Level.SEVERE,pi.getParserLog().toString());
		}
		assert(valid);
		pi.setInputRepositoryFolder("this/does/not/exit");
		assertEquals(false, pi.valid());
		assertEquals(1, pi.getParserLog().getErrors().size());
		assertEquals(ParserError.INPUT_REPO_MISSING,pi.getParserLog().getErrors().get(0).getParserError());
		assert(new File(outputRepositoryFolder).exists());
		pi.setInputRepositoryFolder(inputRepositoryFolder);
		assert(pi.valid());
		assertEquals(0, pi.getParserLog().getErrors().size());
		
		log.info("Output repo created " + out.getAbsolutePath());
		assertEquals(true, out.exists());
	}
	
	@Test
	public void testInputBasicVertebrateGrossAnatomyKOOutputFolder() {
		ParserInvocation pi = new ParserInvocation(
				inputRepositoryFolder, 
				"/var/zyxx1234xx9",
				masterFileName);
		assertEquals(false, pi.valid());
		assertEquals(1, pi.getParserLog().getErrors().size());
		assertEquals(ParserError.OUPUT_REPO_CANNOT_BE_CREATED,pi.getParserLog().getErrors().get(0).getParserError());
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
