package org.stanford.ncbo.owlapi.wrapper;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.io.filefilter.AbstractFileFilter;

public class OntologySuffixFileFilter extends AbstractFileFilter {
	
	public final static String[] acceptedFileExtensions = new String[] {
		"owl",
		"obo",
		"ttl",
		"xml",
		"n3",
		"nt",
		"nq",
		"skos",
		"rdf",
		"rdfs",
	};
	
	@Override
	public boolean accept(File dir, String name) {
		final String[] fileNameParts = name.toLowerCase().split("\\.");
		final String fileExtension = fileNameParts[fileNameParts.length - 1];
		return Arrays.asList(acceptedFileExtensions).contains(fileExtension);
	}
}
