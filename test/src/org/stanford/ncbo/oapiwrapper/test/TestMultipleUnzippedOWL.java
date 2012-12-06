package org.stanford.ncbo.oapiwrapper.test;

import java.io.File;
import java.util.Set;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLOntology;
import org.stanford.ncbo.oapiwrapper.OntologyBean;
import org.stanford.ncbo.oapiwrapper.OntologyParser;
import org.stanford.ncbo.oapiwrapper.OntologyParserException;
import org.stanford.ncbo.oapiwrapper.ParserError;
import org.stanford.ncbo.oapiwrapper.ParserInvocation;
import org.stanford.ncbo.oapiwrapper.ParserLog;

import static org.junit.Assert.*;

public class TestMultipleUnzippedOWL extends TestCase {
	private static final String inputRepositoryFolder = "./test/repo/input/hsdb";
	private static final String outputRepositoryFolder = "./test/repo/output/hsdb";
	
	private final static Logger log = Logger.getLogger(TestMultipleUnzippedOWL.class.getName()); 


	@Override
    protected void setUp() throws Exception  {
        super.setUp();
        File out = new File(outputRepositoryFolder);
        if (out.exists()) {
        	FileUtils.deleteDirectory(out);
        }
    }
	
	@Test
	public void testInputOK() {
		ParserInvocation pi = new ParserInvocation(
				inputRepositoryFolder, 
				outputRepositoryFolder); 
		assertEquals(true, pi.valid());
	}
	
	@Test
	public void testParse() {
		ParserInvocation pi = new ParserInvocation(
				inputRepositoryFolder, 
				outputRepositoryFolder);
		OntologyParser parser = null;
		try {
			parser = new OntologyParser(pi);
		} catch (OntologyParserException e) {
			System.out.println("Errors " + e.parserLog.toString());
			assert(false);
		}
		boolean ok = parser.parse();
		assertEquals(8, parser.getLocalOntologies().size());
		assertEquals(true, ok);
		assertEquals(8, parser.getParsedOntologies().size());
		
		/** checking that parsed worked and that for
		 *  every local file we have an ontology in the manager 
		 */
		for(OntologyBean bean: parser.getLocalOntologies()) {
			int count = 0;
			for (OWLOntology owl : parser.getParsedOntologies()) {
				if (bean.getFile().getName().equals(
						owl.getOntologyID().getDefaultDocumentIRI().getFragment())) {
					count ++;
				}
			}
			if (count == 0) {
				log.info("Not found ontology for " + bean.getFile().getName());
			}
			assertEquals(1, count);
		}
	}
}
