package org.stanford.ncbo.oapiwrapper;

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
				.addOption("m", "master", true, 
						"Master file. First file to be parsed.")
				.addOption("u", "unique-file",false,
							"Only one file is provided")
				.addOption("o","output-repository", true,
						"Path to folder where input data repository is located");
		
		CommandLineParser clp = new GnuParser();
		try {
			CommandLine call = clp.parse(gnuOptions,args);
			String inputRepoPath = call.getOptionValue("i");
			String masterFilePath = call.getOptionValue("m");
			boolean uniqueFile = call.hasOption("u");

			String outputRepoPath = call.getOptionValue("o");
			ParserInvocation pi = new ParserInvocation(inputRepoPath,
					outputRepoPath, masterFilePath, uniqueFile);
			log.info("Parsing invocation with values " +pi.toString());
			
		} catch (ParseException e) {
			log.log(Level.SEVERE, e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}
		log.info("Finished parsing.");
	}
}
