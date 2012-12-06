package org.stanford.ncbo.oapiwrapper;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.AutoIRIMapper;

public class OntologyParser {
	protected ParserInvocation parserInvocation = null;
	private List<OntologyBean> ontologies = null;
	private OWLOntologyManager owlManager = null;
	private OWLOntology owlOntology = null;
	private OWLOntology localMaster = null;
	
			
	public List<OntologyBean> getLocalOntologies() {
		return ontologies;
	}

	private final static Logger log = Logger.getLogger(OntologyParser.class .getName()); 

	public OntologyParser(ParserInvocation parserInvocation) throws OntologyParserException {
		super();
		this.parserInvocation = parserInvocation;
		
	}
	
	private void findLocalOntologies() {
		log.info("["+parserInvocation.getInvocationId()+"] findLocalOntologies in " + parserInvocation.getInputRepositoryFolder());
		File repo = new File(parserInvocation.getInputRepositoryFolder());
		if (repo.isDirectory()) {
		  List<String> suffixes = new ArrayList<String>();
		  suffixes.add("owl");
		  suffixes.add("obo");
		  @SuppressWarnings("unchecked")
		  Iterator<File> files = FileUtils.iterateFiles(repo, new OntologySuffixFileFilter(), new DirectoryFilter());
		  ontologies = new ArrayList<OntologyBean>();
		  while (files.hasNext()) {
			  File f = files.next();
			  ontologies.add(new OntologyBean(f));
			  log.info("["+parserInvocation.getInvocationId()+"] findLocalOntologies in " + f.getName());
		  }
		}
	}
	
	private boolean parseAllFilesInRepo() {
		
       File extractDir = new File(this.parserInvocation.getInputRepositoryFolder());
       List<OntologyBean> ontologiesToParse = new ArrayList<OntologyBean>();
       ontologiesToParse.addAll(this.ontologies);

       this.owlManager = OWLManager.createOWLOntologyManager();
       AutoIRIMapper mapper = new AutoIRIMapper(extractDir, true);
       owlManager.addIRIMapper(mapper);
       while (ontologiesToParse.size() > 0) {
	       OntologyBean ont = ontologiesToParse.get(0);
	       log.info("Parsing " + ont.getFile().getName() + " in the repo queue " + ontologiesToParse.size());
	       try {
	    	   owlManager.loadOntologyFromOntologyDocument(ont.getFile());
			} catch (OWLOntologyCreationException e) {
				log.log(Level.ALL, e.getMessage());
				StringWriter trace = new StringWriter();
				e.printStackTrace(new PrintWriter(trace));
				parserInvocation.getParserLog().addError(ParserError.OWL_PARSE_EXCEPTION, 
						"Error parsing" + ont.getFile().getAbsolutePath() +
						"\n" + e.getMessage() +
						"\n" + trace.toString());
				log.info(e.getMessage());
				return false;
			}
	       Set<Integer> toRemove = new HashSet<Integer>();
	       for(OWLOntology owlOnt : owlManager.getOntologies()) {
	           IRI docIRI = owlManager.getOntologyDocumentIRI(owlOnt);
	           if("file".equalsIgnoreCase(docIRI.getScheme())) {
	    	       File found = new File(docIRI.toString().substring(5));
	    	       for (int i=0;i<ontologiesToParse.size();i++) {
	    	    	   OntologyBean other = ontologiesToParse.get(i);
	    	    	   if (FilenameUtils.equalsNormalized(found.getAbsolutePath(), 
	    	    			   other.getFile().getAbsolutePath())) {
	    	    		   toRemove.add(i);
	    	    	   }
	    	       }
	           } else {
	    	       log.info("Remote " + docIRI.toString());
	           }
	       }
	       if (toRemove.size() == 0) {
	    	log.info("Error parsing, ont not removed from queue " + ont.getFile().getName());
				parserInvocation.getParserLog().addError(ParserError.OWL_FILE_NOT_REMOVED_FROM_QUUE, 
						"Error parsing, ont not removed from queue " + ont.getFile().getAbsolutePath());
					return false;
	       }
	       log.info("Removing " +  toRemove.size() + " ontologies.");
	       List<OntologyBean> ontsTMP = new ArrayList<OntologyBean>();
	       for (int i = 0; i < ontologiesToParse.size(); i++) {
	    	   if (!toRemove.contains(i))
	    		   ontsTMP.add(ontologiesToParse.get(i));
	       }
	       ontologiesToParse = ontsTMP;
	       log.info("In queue " +  ontologiesToParse.size() + " ontologies.");
       }
       return true;
	}
	
	private boolean buildOWLOntology() {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		Set<OWLAxiom> allAxioms = new HashSet<OWLAxiom>();
		for(OWLOntology sourceOnt : this.owlManager.getOntologies()) {
			allAxioms.addAll(sourceOnt.getAxioms());
		}
		try {
			this.owlOntology = manager.createOntology();
		} catch (OWLOntologyCreationException e) {
			log.log(Level.ALL, e.getMessage());
			StringWriter trace = new StringWriter();
			e.printStackTrace(new PrintWriter(trace));
			parserInvocation.getParserLog().addError(ParserError.OWL_CREATE_ONTOLOGY_EXCEPTION, 
					"Error buildOWLOntology" + e.getMessage() +
					"\n" + trace.toString());
			log.info(e.getMessage());
			return false;
		}
		manager.addAxioms(this.owlOntology, allAxioms);
		return true;
	}
	
	public boolean parse() {
		findLocalOntologies();
		this.localMaster = findMasterFile();
		if (this.localMaster == null) {
			String message = "Error cannot find " + this.parserInvocation.getMasterFileName() + " in input folder.";
			parserInvocation.getParserLog().addError(ParserError.MASTER_FILE_MISSING,message);
			log.info(message);
			return false;
		}
		if (parseAllFilesInRepo()) {
			if (buildOWLOntology()) {
				return true;
				
			} else {
				//abort error in parsing
				return false;
			}
		} else {
			//abort error in parsing
			return false;
		}
	}
	
	private OWLOntology findMasterFile() {
		for (OntologyBean b : this.ontologies) {
			if (b.getFile().getName().equals(this.parserInvocation.getMasterFileName())) {
		       try {
		    	   return this.owlManager.loadOntologyFromOntologyDocument(
		    			   b.getFile());
				} catch (OWLOntologyCreationException e) {
					log.log(Level.ALL, e.getMessage());
					StringWriter trace = new StringWriter();
					e.printStackTrace(new PrintWriter(trace));
					parserInvocation.getParserLog().addError(ParserError.OWL_PARSE_EXCEPTION, 
							"Error parsing" + b.getFile().getAbsolutePath() +
							"\n" + e.getMessage() +
							"\n" + trace.toString());
					log.info(e.getMessage());
					return null;
				}
			}
		}
		return null;
	}

	public Set<OWLOntology> getParsedOntologies() {
		return this.owlManager.getOntologies();
	}
}
