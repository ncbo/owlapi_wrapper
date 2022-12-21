package org.stanford.ncbo.owlapi.wrapper.metrics;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stanford.ncbo.owlapi.wrapper.OntologyParser;
import org.stanford.ncbo.owlapi.wrapper.OntologyParserConstants;
import org.stanford.ncbo.owlapi.wrapper.ParserInvocation;

import java.io.File;
import java.nio.charset.Charset;

import static org.junit.Assert.*;

public class OntologyMetricsTest {

    private static final Logger logger = LoggerFactory.getLogger(OntologyMetricsTest.class);

    @BeforeClass
    public static void oneTimeSetup() throws Exception {
        // Parse pizza ontology
        logger.info("Parsing pizza ontology");
        ParserInvocation pi = new ParserInvocation("./src/test/resources/repo/input/pizza",
                "./src/test/resources/repo/output/pizza", "pizza.owl", true);
        OntologyParser parser = new OntologyParser(pi);
        assertTrue(parser.parse());
        logger.info("Completed parsing pizza ontology\n");

        // Parse HSDB ontology
        logger.info("Parsing HSDB ontology");
        pi = new ParserInvocation("./src/test/resources/repo/input/hsdb",
                "./src/test/resources/repo/output/hsdb", "HSDB_OCRe.owl", true);
        parser = new OntologyParser(pi);
        assertTrue(parser.parse());
        logger.info("Completed parsing HSDB ontology\n");
    }

    @Test
    public void generate_MetricsForPizza_FileCreated() throws Exception {
        String path = "./src/test/resources/repo/output/pizza" + File.separator + OntologyParserConstants.METRICS_FILE;
        File file = new File(path);
        assertTrue(file.exists());
    }

    @Test
    public void generate_MetricsForPizza_2RecordsCreated() throws Exception {
        String path = "./src/test/resources/repo/output/pizza" + File.separator + OntologyParserConstants.METRICS_FILE;
        String data = FileUtils.readFileToString(new File(path), Charset.defaultCharset());
        CSVFormat format = CSVFormat.Builder.create()
                .setHeader(OntologyParserConstants.METRICS_FILE_HEADERS)
                .build();
        CSVParser parser = CSVParser.parse(data, format);

        // One row for column headers, one row for counts
        assertEquals(2, parser.getRecords().size());
    }

    @Test
    public void generate_MetricsForOntologyWithoutImports_Calculated() throws Exception {
        String path = "./src/test/resources/repo/output/pizza" + File.separator + OntologyParserConstants.METRICS_FILE;
        String data = FileUtils.readFileToString(new File(path), Charset.defaultCharset());
        CSVFormat format = CSVFormat.Builder.create()
                .setHeader(OntologyParserConstants.METRICS_FILE_HEADERS)
                .build();
        CSVParser parser = CSVParser.parse(data, format);

        // Start at 1 to skip header row
        CSVRecord record = parser.getRecords().get(1);
        int numClasses = Integer.parseInt(record.get(OntologyParserConstants.METRICS_CLASS_COUNT));
        int numIndividuals = Integer.parseInt(record.get(OntologyParserConstants.METRICS_INDIVIDUAL_COUNT));
        int numProperties = Integer.parseInt(record.get(OntologyParserConstants.METRICS_PROPERTY_COUNT));

        int[] expected = { 100, 5, 8 };
        int[] actual = { numClasses, numIndividuals, numProperties };
        assertArrayEquals(expected, actual);
    }

    @Test
    public void generate_MetricsForOntologyWithImports_Calculated() throws Exception {
        String path = "./src/test/resources/repo/output/hsdb" + File.separator + OntologyParserConstants.METRICS_FILE;
        String data = FileUtils.readFileToString(new File(path), Charset.defaultCharset());
        CSVFormat format = CSVFormat.Builder.create()
                .setHeader(OntologyParserConstants.METRICS_FILE_HEADERS)
                .build();
        CSVParser parser = CSVParser.parse(data, format);

        // Start at 1 to skip header row
        CSVRecord record = parser.getRecords().get(1);
        int numClasses = Integer.parseInt(record.get(OntologyParserConstants.METRICS_CLASS_COUNT));
        int numIndividuals = Integer.parseInt(record.get(OntologyParserConstants.METRICS_INDIVIDUAL_COUNT));
        int numProperties = Integer.parseInt(record.get(OntologyParserConstants.METRICS_PROPERTY_COUNT));

        int[] expected = { 356, 35, 208 };
        int[] actual = { numClasses, numIndividuals, numProperties };
        assertArrayEquals(expected, actual);
    }

}