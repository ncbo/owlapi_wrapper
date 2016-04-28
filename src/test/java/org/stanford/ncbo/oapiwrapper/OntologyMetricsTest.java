package org.stanford.ncbo.oapiwrapper;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class OntologyMetricsTest {

    private String metricsPath;

    @Before
    public void setUp() throws Exception {
        String outputFolder = "./src/test/resources/repo/output/pizza";
        metricsPath = outputFolder + File.separator + OntologyParserConstants.METRICS_FILE;

        File file = new File(metricsPath);
        if (file.exists()) {
            assertTrue(file.delete());
        }

        ParserInvocation pi = new ParserInvocation("./src/test/resources/repo/input/pizza", outputFolder, "pizza.owl", true);
        OntologyParser parser = new OntologyParser(pi);
        assertTrue(parser.parse());
    }

    @Test
    public void generate_MetricsForPizza_FileCreated() throws Exception {
        File file = new File(metricsPath);
        assertTrue(file.exists());
    }

    @Test
    public void generate_MetricsForPizza_2RecordsCreated() throws Exception {
        String data = FileUtils.readFileToString(new File(metricsPath));
        CSVFormat format = CSVFormat.DEFAULT.withHeader(OntologyParserConstants.METRICS_FILE_HEADERS);
        CSVParser parser = CSVParser.parse(data, format);

        // One row for column headers, one row for counts
        assertEquals(2, parser.getRecords().size());
    }

    @Test
    public void generate_MetricsForPizza_ClassCountsCalculated() throws Exception {
        CSVRecord record = getMetricsRecord();
        String classCount = record.get(OntologyParserConstants.METRICS_CLASS_COUNT);
        assertEquals(100, Integer.parseInt(classCount));
    }

    @Test
    public void generate_MetricsForPizza_PropertyCountsCalculated() throws Exception {
        CSVRecord record = getMetricsRecord();
        String propertyCount = record.get(OntologyParserConstants.METRICS_PROPERY_COUNT);
        assertEquals(8, Integer.parseInt(propertyCount));
    }

    @Test
    public void generate_MetricsForPizza_IndividualCountsCalculated() throws Exception {
        CSVRecord record = getMetricsRecord();
        String individualCount = record.get(OntologyParserConstants.METRICS_INDIVIDUAL_COUNT);
        assertEquals(5, Integer.parseInt(individualCount));
    }

    @Test
    public void generate_MetricsForOntologyWithImports_Calculated() throws Exception {
        String path = "./src/test/resources/repo/output/hsdb" + File.separator + OntologyParserConstants.METRICS_FILE;
        String data = FileUtils.readFileToString(new File(path));
        CSVFormat format = CSVFormat.DEFAULT.withHeader(OntologyParserConstants.METRICS_FILE_HEADERS);
        CSVParser parser = CSVParser.parse(data, format);

        // Start at 1 to skip header row
        CSVRecord record = parser.getRecords().get(1);
        int numClasses = Integer.parseInt(record.get(OntologyParserConstants.METRICS_CLASS_COUNT));
        int numIndividuals = Integer.parseInt(record.get(OntologyParserConstants.METRICS_INDIVIDUAL_COUNT));
        int numProperties = Integer.parseInt(record.get(OntologyParserConstants.METRICS_PROPERY_COUNT));

        int[] expected = { 356, 35, 208 };
        int[] actual = { numClasses, numIndividuals, numProperties };
        assertArrayEquals(expected, actual);
    }

    @After
    public void tearDown() throws Exception {

    }

    private CSVRecord getMetricsRecord() throws Exception {
        String data = FileUtils.readFileToString(new File(metricsPath));
        CSVFormat format = CSVFormat.DEFAULT.withHeader(OntologyParserConstants.METRICS_FILE_HEADERS);
        CSVParser parser = CSVParser.parse(data, format);

        // Start at 1 to skip header row
        return parser.getRecords().get(1);
    }

}