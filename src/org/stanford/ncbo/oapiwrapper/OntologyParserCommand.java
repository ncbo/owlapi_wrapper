package org.stanford.ncbo.oapiwrapper;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class OntologyParserCommand {
	private final static Logger log = Logger.getLogger(OntologyParserCommand.class .getName()); 
	public static void main(String[] args) {
		final Options gnuOptions = new Options(); 
		gnuOptions.addOption("i", "input-repository", true, 
				"Path to folder where input data repository is located")
				.addOption("m","master-filename", true,
						"Name of the ontology file to load first")
				.addOption("o","output-repository", true,
						"Path to folder where input data repository is located");
		
		CommandLineParser clp = new GnuParser();
		try {
			CommandLine call = clp.parse(gnuOptions,args);
			String inputRepoPath = call.getOptionValue("i");
			String outputRepoPath = call.getOptionValue("o");
			String masterFileName = call.getOptionValue("m");

			ParserInvocation pi = new ParserInvocation(inputRepoPath,
					outputRepoPath, masterFileName);
			if (!pi.valid()) {
				log.info("Parsing invocation with values " +pi.toString());
				log.log(Level.SEVERE, "invocation is not valid");
				log.log(Level.SEVERE,pi.getParserLog().toString());
				System.exit(-1);
			}
			log.info("Parsing invocation with values " +pi.toString());
			
			OntologyParser parser = null;
			try {
				parser = new OntologyParser(pi);
			} catch (OntologyParserException e) {
				log.log(Level.SEVERE, "Error creating parser", e);
				log.log(Level.SEVERE,pi.getParserLog().toString());
				System.exit(-1);
			}
			boolean parseResult = false;
			try {
				parseResult = parser.parse();
			} catch (Exception e) {
				log.log(Level.SEVERE, "Error parsing", e);
				log.log(Level.SEVERE,pi.getParserLog().toString());
				System.exit(-1);
			}
			File f = new File(pi.getOutputRepositoryFolder() + File.separator + "owlapi.xrdf");
			log.info("Parse result " + parseResult+ ".Output triples in " + f.getAbsolutePath());		
		} catch (ParseException e) {
			log.log(Level.SEVERE, e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}
		log.info("Finished parsing.");
	}
}
