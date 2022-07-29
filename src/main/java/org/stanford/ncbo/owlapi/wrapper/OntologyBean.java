package org.stanford.ncbo.owlapi.wrapper;

import java.io.File;

public class OntologyBean {
	private File file = null;
	
	@Override
	public String toString() {
		return "OntologyBean [file=" + file.getAbsolutePath() + "]";
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public OntologyBean(File file) {
		this.file = file;
	}
}
