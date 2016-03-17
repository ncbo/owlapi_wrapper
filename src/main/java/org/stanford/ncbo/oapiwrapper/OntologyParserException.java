package org.stanford.ncbo.oapiwrapper;

public class OntologyParserException extends Exception {
	private static final long serialVersionUID = 1L;
	public ParserLog parserLog = null;
	
	public OntologyParserException(ParserLog log) {
		this.parserLog = log;
	}
	
	
}
