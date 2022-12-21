package org.stanford.ncbo.owlapi.wrapper;

import org.semanticweb.owlapi.model.MissingImportEvent;
import org.semanticweb.owlapi.model.MissingImportListener;

public class LogMissingImports implements MissingImportListener{
	private ParserLog log = null;
	public LogMissingImports(ParserLog log) {
		this.log = log;
	}
	public void importMissing(MissingImportEvent event) {
		log.addError(ParserError.OWL_IMPORT_MISSING, event.getImportedOntologyURI().toString());
	}

}
