package org.stanford.ncbo.owlapi.wrapper;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class OntologyParserCommand {
	private final static Logger log = LoggerFactory.getLogger(OntologyParserCommand.class .getName());

	public static void main(String[] args) {
		final Options options = new Options();
		options.addOption("i", "input-repository", true,
				"Path to folder where input data repository is located")
				.addOption("m","master-filename", true,
						"Name of the ontology file to load first")
				.addOption("o","output-repository", true,
						"Path to folder where output data repository is located")
				.addOption("r","reasoner", true,
						"Option to use the reasoner");
		
		CommandLineParser clp = new DefaultParser();
		try {
			CommandLine call = clp.parse(options,args);
			String inputRepoPath = call.getOptionValue("i");
			String outputRepoPath = call.getOptionValue("o");
			String masterFileName = call.getOptionValue("m");
			String reasoner = call.getOptionValue("r");
			Boolean bReasoner = reasoner == null || reasoner.equals("true");

			ParserInvocation pi = new ParserInvocation(inputRepoPath, outputRepoPath, masterFileName, bReasoner);
			if (!pi.valid()) {
				log.info("Parsing invocation with values: {}", pi.toString());
				log.error("Invalid invocation!");
				log.error(pi.getParserLog().toString());
				System.exit(-1);
			}
			log.info("Parsing invocation with values: {}", pi.toString());
			
			OntologyParser parser = null;
			try {
				parser = new OntologyParser(pi);
			} catch (OntologyParserException e) {
				log.error("Parser creation error", e);
				log.error(pi.getParserLog().toString());
				System.exit(-1);
			}
			boolean parseResult = false;
			try {
				parseResult = parser.parse();
			} catch (Exception e) {
				log.error("Parsing error", e);
				log.error(pi.getParserLog().toString());
				System.exit(-1);
			}
			File f = new File(pi.getOutputRepositoryFolder() + File.separator + "owlapi.xrdf");
			log.info("Parse result: {}", parseResult);
			log.info("Output triples in: {}" + f.getAbsolutePath());
		} catch (ParseException e) {
			log.error(e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}
		log.info("Finished parsing!");
	}
}
