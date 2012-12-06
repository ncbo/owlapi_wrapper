package org.stanford.ncbo.oapiwrapper;

import java.util.ArrayList;
import java.util.List;

public class ParserLog {
	public class Error {
		public ParserError getParserError() {
			return e;
		}

		public String getMessage() {
			return message;
		}

		ParserError e;
		String message;

		public Error(ParserError e2, String message2) {
			e = e2;
			message = message2;
		}

		public Error(ParserError e2) {
			e = e2;
			message = null;
		}
				
		@Override
		public String toString() {
			return e.toString() + (message != null ? message : "");
		}
	}

	List<ParserLog.Error> logErrors = new ArrayList<ParserLog.Error>();
	
	public void addError(ParserError e, String message) {
		logErrors.add(new Error(e, message));
	}

	public void addError(ParserError e) {
		logErrors.add(new Error(e));
	}
	
	public List<ParserLog.Error> getErrors() {
		return new ArrayList<ParserLog.Error>(this.logErrors);
	}
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (Error e : logErrors) {
			str.append(e);
		}
		return str.toString();
	}

	public void flush() {
		logErrors = new ArrayList<ParserLog.Error>();
	}

}
