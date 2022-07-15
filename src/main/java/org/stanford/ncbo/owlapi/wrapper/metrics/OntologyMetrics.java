package org.stanford.ncbo.owlapi.wrapper.metrics;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.semanticweb.owlapi.metrics.ReferencedClassCount;
import org.semanticweb.owlapi.metrics.ReferencedDataPropertyCount;
import org.semanticweb.owlapi.metrics.ReferencedIndividualCount;
import org.semanticweb.owlapi.metrics.ReferencedObjectPropertyCount;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stanford.ncbo.owlapi.wrapper.OntologyParserConstants;
import org.stanford.ncbo.owlapi.wrapper.ParserInvocation;
import org.stanford.ncbo.owlapi.wrapper.util.OntologyBasedGraph;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class OntologyMetrics {

    private static final Logger logger = LoggerFactory.getLogger(OntologyMetrics.class);

    private OWLOntology ontology;

    private ParserInvocation parserInvocation;

    private int total_classes;

    private int total_individuals;

    private int total_properties;

    private int max_depth;


    public OntologyMetrics(OWLOntology ontology, ParserInvocation parserInvocation) {
        this.ontology = ontology;
        this.parserInvocation = parserInvocation;
    }

    public void generate() {
        String ontologyFileName = parserInvocation.getMasterFileName();

        logger.info(String.format("Calculating metrics for %s", ontologyFileName));
        long startTime = System.nanoTime();

        ReferencedClassCount referencedClassCount = new ReferencedClassCount(ontology);
        referencedClassCount.setImportsClosureUsed(true);
        total_classes = referencedClassCount.getValue();

        ReferencedIndividualCount referencedIndividualCount = new ReferencedIndividualCount(ontology);
        referencedIndividualCount.setImportsClosureUsed(true);
        total_individuals = referencedIndividualCount.getValue();

        ReferencedDataPropertyCount referencedDataPropertyCount = new ReferencedDataPropertyCount(ontology);
        referencedDataPropertyCount.setImportsClosureUsed(true);
        ReferencedObjectPropertyCount referencedObjectPropertyCount = new ReferencedObjectPropertyCount(ontology);
        referencedObjectPropertyCount.setImportsClosureUsed(true);
        total_properties = referencedDataPropertyCount.getValue() + referencedObjectPropertyCount.getValue();

        OntologyBasedGraph graph = OntologyBasedGraph.getGraph(ontology);
        max_depth = graph.maxDepth();

        long estimatedTime = (System.nanoTime() - startTime) / 1000000;
        logger.info(String.format("Finished metrics calculation for %s in %d milliseconds", ontologyFileName, estimatedTime));

        write();
    }

    private void write() {
        String path = parserInvocation.getOutputRepositoryFolder() + File.separator + OntologyParserConstants.METRICS_FILE;
        FileWriter fileWriter = null;
        CSVPrinter csvPrinter = null;

        try {
            fileWriter = new FileWriter(new File(path));
            CSVFormat format = CSVFormat.Builder.create()
                    .setHeader(OntologyParserConstants.METRICS_FILE_HEADERS)
                    .build();
            csvPrinter = new CSVPrinter(fileWriter, format);

            List countRecord = Arrays.asList(total_classes, total_individuals, total_properties, max_depth);
            csvPrinter.printRecord(countRecord);
            logger.info(String.format("Generated metrics CSV file for %s", parserInvocation.getMasterFileName()));
        } catch (IOException e) {
            logger.error("Error generating metrics CSV file", e);
            e.printStackTrace();
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
                csvPrinter.close();
            } catch (IOException e) {
                logger.error("Error flushing/closing metrics CSV file", e);
                e.printStackTrace();
            }
        }
    }

}
