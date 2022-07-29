package org.stanford.ncbo.owlapi.wrapper;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ParserInvocationTest {
    private static final String inputRepositoryFolder = "./src/test/resources/repo/input/bvga";
    private static final String outputRepositoryFolder = "./src/test/resources/repo/output/bvga";
    private static final String masterFileName = "basic-vertebrate-gross-anatomy_v1.1.owl";

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void valid_SimpleParameters_ReturnsTrue() {
        ParserInvocation pi = new ParserInvocation(inputRepositoryFolder, outputRepositoryFolder,
                masterFileName, true);
        assertTrue(pi.valid());
    }

    @Test
    public void valid_InputFolderIsInvalid_ReturnsFalse() {
        ParserInvocation pi = new ParserInvocation("/invalid/input/folder", outputRepositoryFolder,
                masterFileName, true);
        assertFalse(pi.valid());
    }

    @Test
    public void valid_InputFolderIsAFile_ReturnsFalse() throws IOException {
        File f = new File("./src/test/resources/repo/input/bvga/test.txt");
        FileUtils.writeStringToFile(f, "some data", Charset.defaultCharset());
        ParserInvocation pi = new ParserInvocation(f.getAbsolutePath(), outputRepositoryFolder, masterFileName, true);
        assertFalse(pi.valid());
    }

    @Test
    public void valid_OutputFolderIsInvalid_ReturnsFalse() {
        ParserInvocation pi = new ParserInvocation(inputRepositoryFolder, "/invalid/output/folder",
                masterFileName, true);
        assertFalse(pi.valid());
    }

    @Test
    public void valid_OutputFolderIsAFile_ReturnsFalse() throws IOException {
        File f = new File("./src/test/resources/repo/output/bvga/test.txt");
        FileUtils.writeStringToFile(f, "some data", Charset.defaultCharset());
        ParserInvocation pi = new ParserInvocation(inputRepositoryFolder, f.getAbsolutePath(), masterFileName, true);
        assertFalse(pi.valid());
    }

    @Test
    public void valid_OutputFolderIsCreated_ReturnsTrue() throws IOException {
        File output = new File(outputRepositoryFolder);
        if (output.exists()) {
            FileUtils.deleteDirectory(output);
        }

        ParserInvocation pi = new ParserInvocation(inputRepositoryFolder, outputRepositoryFolder,
                masterFileName, true);
        assertTrue(pi.valid());
    }

    @Ignore
    @Test
    public void valid_MasterFileIsInvalid_ReturnsFalse() {
        ParserInvocation pi = new ParserInvocation(
                inputRepositoryFolder,
                outputRepositoryFolder,
                "missing_master_file.owl", true);
        assertFalse(pi.valid());

        /*
         * TODO
         *
         * Surprisingly, this test fails.
         *
         * What was the original intent of allowing an invalid master file to be passed in, and still end
         * up with a "valid" ParserInvocation object?
         *
         */
    }

    @Test
    public void valid_MultipleUnzippedOntologyFiles_ReturnsTrue() {
        ParserInvocation pi = new ParserInvocation("./src/test/resources/repo/input/hsdb",
                "./src/test/resources/repo/output/hsdb", "HSDB_OCRe.owl", true);
        assertTrue(pi.valid());
    }

    @After
    public void tearDown() throws Exception {
        List<File> files = new ArrayList<File>();
        files.add(new File("./src/test/resources/repo/input/bvga/test.txt"));
        files.add(new File("./src/test/resources/repo/output/bvga/test.txt"));
        for (File f : files) {
            if (f.exists()) f.delete();
        }
    }

}