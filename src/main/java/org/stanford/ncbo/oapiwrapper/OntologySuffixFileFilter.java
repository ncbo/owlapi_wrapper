package org.stanford.ncbo.oapiwrapper;

import java.io.File;

import org.apache.commons.io.filefilter.AbstractFileFilter;

public class OntologySuffixFileFilter extends AbstractFileFilter {
	
	@Override
	public boolean accept(File dir, String name) {
		return name.toLowerCase().endsWith("owl") || 
				name.toLowerCase().endsWith("obo") ||
				name.toLowerCase().endsWith("xml");
	}
}
