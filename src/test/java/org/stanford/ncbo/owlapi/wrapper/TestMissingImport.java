package org.stanford.ncbo.owlapi.wrapper;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.File;
import org.junit.Test;

public class TestMissingImport {
	private static final String outputRepositoryFolder = "./src/test/resources/repo/output/cno";

	@Test
	public void testCNOParse() throws Exception {
		ParserInvocation pi = new ParserInvocation("./src/test/resources/repo/input/cno",
				outputRepositoryFolder, "cnov0_5.owl", true);
		assertTrue(pi.valid());

		OntologyParser parser = new OntologyParser(pi);
		assertTrue(parser.parse());
		assertEquals(1, parser.getLocalOntologies().size());

		File f = new File(outputRepositoryFolder + File.separator + "owlapi.xrdf");
		assertTrue(f.exists());
	}
}
