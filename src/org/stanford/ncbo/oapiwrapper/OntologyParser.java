package org.stanford.ncbo.oapiwrapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.coode.owlapi.obo.parser.OBOOntologyFormat;
import org.coode.owlapi.obo.parser.OBOPrefix;
import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.AutoIRIMapper;
import org.semanticweb.owlapi.util.InferredSubClassAxiomGenerator;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

public class OntologyParser {
	protected ParserInvocation parserInvocation = null;
	private List<OntologyBean> ontologies = new ArrayList<OntologyBean>();
	private OWLOntologyManager sourceOwlManager = null;
	private OWLOntologyManager targetOwlManager = null;
	private OWLOntology targetOwlOntology = null;
	private OWLOntology localMaster = null;
	
			
	public List<OntologyBean> getLocalOntologies() {
		return ontologies;
	}

	private final static Logger log = Logger.getLogger(OntologyParser.class .getName()); 

	public OntologyParser(ParserInvocation parserInvocation) throws OntologyParserException {
		super();
		log.info("executor ...");
		this.parserInvocation = parserInvocation;
		if (!parserInvocation.valid())
			throw new OntologyParserException(this.parserInvocation.getParserLog());

		this.sourceOwlManager = OWLManager.createOWLOntologyManager();
		//this.sourceOwlManager.setSilentMissingImportsHandling(true);
		if (this.parserInvocation.getInputRepositoryFolder() != null) {
			File rooDirectory = new File(this.parserInvocation.getInputRepositoryFolder());
			this.sourceOwlManager.addIRIMapper(new AutoIRIMapper(rooDirectory, true));
		}
		this.targetOwlManager = OWLManager.createOWLOntologyManager();
				
		//this.targetOwlManager.setSilentMissingImportsHandling(true);
		log.info("executor created");
	}
	
	public String getOBODataVersion(String file) {
		System.out.println("@@ Trying to get obo data version from " + file);
		String result = null;
		String line = null;
	    try {
	        BufferedReader reader = new BufferedReader(new FileReader(file));
	        while((line = reader.readLine()) != null){
	        	if (line.contains("data-version:")) {
	        		String[] version = line.split(" ");
	        		if (version.length > 1) {
	        			System.out.println("@@ Indetified obo data version " + version[1]);
	        			return version[1];
	        		}
	        	}
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
		return result;
	}
	
	private void findLocalOntologies() {
		String oboVersion = null;
		if (parserInvocation.getInputRepositoryFolder() != null) {
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
				  if (f.getAbsolutePath().toLowerCase().endsWith("obo")) {
					  oboVersion = getOBODataVersion(f.getAbsolutePath());
   				  }
				  ontologies.add(new OntologyBean(f));
				  log.info("["+parserInvocation.getInvocationId()+"] findLocalOntologies in " + f.getName());
			  }
			}
		} else {
			if (this.parserInvocation.getMasterFileName().toLowerCase().endsWith("obo")) {
				oboVersion = getOBODataVersion(this.parserInvocation.getMasterFileName());
			}
			this.ontologies.add(new OntologyBean(new File(this.parserInvocation.getMasterFileName())));
			log.info("getInputRepositoryFolder is not provided. Unique file being parse.");
		}
		if (oboVersion != null) {
			parserInvocation.setOBOVersion(oboVersion);
		}
	}
	
	private boolean buildOWLOntology() {
		Set<OWLAxiom> allAxioms = new HashSet<OWLAxiom>();
		boolean isOBO = false;
		OWLDataFactory fact = sourceOwlManager.
				getOWLDataFactory();
		
		OWLAxiom owlAnnVersion = null;
		for(OWLOntology sourceOnt : this.sourceOwlManager.getOntologies()) {
			OWLOntologyFormat format = this.sourceOwlManager.getOntologyFormat(sourceOnt);
			isOBO = isOBO || (format instanceof OBOOntologyFormat);
			
			
			for (OWLAnnotation ann : sourceOnt.getAnnotations()) {
				System.out.println("@@ adding version for ?? " + ann.getProperty().toString());
				if (ann.getProperty().toString().contains("versionInfo")) {
					System.out.println("@@ adding version " + ann.getValue());
					owlAnnVersion = fact.getOWLAnnotationAssertionAxiom(ann.getProperty(), 
							IRI.create("http://bioportal.bioontology.org/ontologies/versionSubject"),
							ann.getValue());
				}
			}
			
			for (OWLAxiom axiom : sourceOnt.getAxioms()) {
				allAxioms.add(axiom);

				if (axiom instanceof OWLSubClassOfAxiom) {
					OWLSubClassOfAxiom sc = (OWLSubClassOfAxiom) axiom;
					OWLClassExpression ce = sc.getSuperClass();
					if (ce instanceof OWLObjectSomeValuesFrom) {
						OWLObjectSomeValuesFrom some = (OWLObjectSomeValuesFrom) ce;
						if (!some.getProperty().isAnonymous() && !some.getFiller().isAnonymous()) {
							String propSome = some.getProperty().asOWLObjectProperty().getIRI().toString().toLowerCase();
							if (propSome.contains("obo")) {
								if (propSome.endsWith("part_of") || propSome.endsWith("bfo_0000050") ||
										propSome.endsWith("contains") || propSome.endsWith("ro_0001019") || 
										propSome.endsWith("develops_from") || propSome.endsWith("ro_0002202") ) {
									OWLAnnotationProperty prop = null;
									if (propSome.endsWith("contains") || propSome.endsWith("ro_0001019")) {
										OWLSubClassOfAxiom ax = fact.getOWLSubClassOfAxiom(some.getFiller(),sc.getSubClass());
										allAxioms.add(ax);
										prop = fact.getOWLAnnotationProperty(IRI.create("http://data.bioontology.org/metadata/obo/contains"));
										OWLAxiom annAsse = fact.getOWLAnnotationAssertionAxiom(prop, 
												some.getFiller().asOWLClass().getIRI(),
												sc.getSubClass().asOWLClass().getIRI());
										allAxioms.add(annAsse);
									} else {
										OWLSubClassOfAxiom ax = fact.getOWLSubClassOfAxiom(sc.getSubClass(), some.getFiller());
										allAxioms.add(ax);
										if (propSome.endsWith("part_of") || propSome.endsWith("bfo_0000050"))
											prop = fact.getOWLAnnotationProperty(IRI.create("http://data.bioontology.org/metadata/obo/part_of"));
										else
											prop = fact.getOWLAnnotationProperty(IRI.create("http://data.bioontology.org/metadata/obo/develops_from"));
										OWLAxiom annAsse = fact.getOWLAnnotationAssertionAxiom(prop, 
												sc.getSubClass().asOWLClass().getIRI(),
												some.getFiller().asOWLClass().getIRI());
										allAxioms.add(annAsse);
									}
								}else{
									if (!some.getFiller().isAnonymous() && !sc.getSubClass().isAnonymous()) {
										OWLAnnotationProperty prop = fact.getOWLAnnotationProperty(some.getProperty().asOWLObjectProperty().getIRI());
										OWLAxiom annAsse = fact.getOWLAnnotationAssertionAxiom(prop, 
												sc.getSubClass().asOWLClass().getIRI(),
												some.getFiller().asOWLClass().getIRI());
										allAxioms.add(annAsse);
									}
								}
							}
						}
					}
				}
			}
			if (isOBO) {
				Set<OWLClass> classes = sourceOnt.getClassesInSignature();
				for (OWLClass cls : classes) {
					if (!cls.isAnonymous()) {
						String oboID = toOBOId(cls.getIRI());
						OWLAnnotationProperty prop = fact.getOWLAnnotationProperty(IRI.create("http://www.w3.org/2004/02/skos/core#notation"));
						OWLAxiom annAsse = fact.getOWLAnnotationAssertionAxiom(prop, 
								cls.getIRI(),
								fact.getOWLLiteral(oboID));
						allAxioms.add(annAsse);
					}
				}
			}
			
		}
		

		
		try {
			this.targetOwlOntology = targetOwlManager.createOntology();
		} catch (OWLOntologyCreationException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
			StringWriter trace = new StringWriter();
			e.printStackTrace(new PrintWriter(trace));
			parserInvocation.getParserLog().addError(ParserError.OWL_CREATE_ONTOLOGY_EXCEPTION, 
					"Error buildOWLOntology" + e.getMessage() +
					"\n" + trace.toString());
			log.info(e.getMessage());
			return false;
		}
		

		
		targetOwlManager.addAxioms(this.targetOwlOntology, allAxioms);
		for (OWLAnnotation ann: this.targetOwlOntology.getAnnotations()) {
			AddOntologyAnnotation addAnn = new AddOntologyAnnotation(this.targetOwlOntology, ann);
			targetOwlManager.applyChange(addAnn);
		}
		
		if (isOBO) {
			if (parserInvocation.getOBOVersion() != null) {
				System.out.println("@@ adding version " + parserInvocation.getOBOVersion());
				OWLAnnotationProperty prop = fact.getOWLAnnotationProperty(IRI.create(OWLRDFVocabulary.OWL_VERSION_INFO.toString()));
				OWLAnnotationAssertionAxiom annVersion = fact.getOWLAnnotationAssertionAxiom(prop, 
						IRI.create("http://bioportal.bioontology.org/ontologies/versionSubject"),
						fact.getOWLLiteral(parserInvocation.getOBOVersion()));
				targetOwlManager.addAxiom(targetOwlOntology, annVersion);
			}
		} else {
			if (owlAnnVersion != null) {
				targetOwlManager.addAxiom(targetOwlOntology, owlAnnVersion);
			}
		}
		
		for(OWLOntology sourceOnt : this.sourceOwlManager.getOntologies()) {
			 for (OWLAnnotation ann : sourceOnt.getAnnotations()) {
				AddOntologyAnnotation addAnn = new AddOntologyAnnotation(this.targetOwlOntology, ann);
				targetOwlManager.applyChange(addAnn);
			 }
		}
		
		OWLReasonerFactory reasonerFactory = null;
		OWLReasoner reasoner = null;
		reasonerFactory = new StructuralReasonerFactory();
		reasoner = reasonerFactory.createReasoner(this.targetOwlOntology);

		InferredSubClassAxiomGenerator isc = new InferredSubClassAxiomGenerator();
		Set<OWLSubClassOfAxiom> subAxs = isc.createAxioms(this.targetOwlOntology.getOWLOntologyManager(), reasoner);
		targetOwlManager.addAxioms(this.targetOwlOntology, subAxs);
		
		Set<OWLEntity> things = targetOwlOntology.getEntitiesInSignature(IRI.create("http://www.w3.org/2002/07/owl#Thing"));
		OWLClass thing = null;
		for (OWLEntity t : things) {
			thing = (OWLClass) t;
		}
		Set<OWLSubClassOfAxiom> rootsEdges = targetOwlOntology.getSubClassAxiomsForSuperClass(thing);
		for (OWLSubClassOfAxiom rootEdge : rootsEdges) {
			if (!rootEdge.getSubClass().isAnonymous()) {
				OWLClass subClass = (OWLClass) rootEdge.getSubClass();
				String rootID = subClass.getIRI().toString();
				if (rootID.toLowerCase().contains("obo")) {
					Set<OWLAnnotation> annotationsRoot = subClass.getAnnotations(targetOwlOntology);
					for (OWLAnnotation annRoot : annotationsRoot) {
						if (annRoot.isDeprecatedIRIAnnotation()) {
						System.out.println("Deprecated annotation with value " + annRoot.getValue().toString());
						if (annRoot.getValue().toString().contains("true")) {
							System.out.println("Removing edge from owl:Thing for obsolete in OBO " + rootID);
							RemoveAxiom remove = new RemoveAxiom(targetOwlOntology,rootEdge);
							targetOwlManager.applyChange(remove);
						}}
					}
				}
			}
		}
		return true;
	}
	
	private boolean classHasRestrictions(OWLClass subClass,OWLOntology ont) {
		if (!subClass.toString().toLowerCase().contains("/obo/"))
			return false;
		Set<OWLClassAxiom> classAxioms = ont.getAxioms(subClass);
		for (OWLClassAxiom axiom : classAxioms) {
			if (axiom instanceof OWLSubClassOfAxiom) {
				OWLSubClassOfAxiom sc = (OWLSubClassOfAxiom) axiom;
				OWLClassExpression ce = sc.getSuperClass();
				if (ce instanceof OWLObjectSomeValuesFrom) {
					System.out.println("classHasRestrictions IN OBO --> " + subClass.toString());
					return true;
				}
			}
		}

		return false;
	}

	public boolean parse() throws Exception {
		try {
			if (internalParse()) {
				parserInvocation.saveErrors();
				return true;
			}
			parserInvocation.saveErrors();
		} catch (Exception e) {
			log.log(Level.SEVERE, e.getMessage(),e);
			StringWriter trace = new StringWriter();
			e.printStackTrace(new PrintWriter(trace));
			parserInvocation.getParserLog().addError(ParserError.UNKNOWN, 
					"Error " + e.getMessage() +
					"\nTrace:\n" + trace.toString());
		}
		parserInvocation.saveErrors();
		return false;
	}
	private boolean internalParse() {
		findLocalOntologies();
		this.localMaster = findMasterFile();		
		if (this.localMaster == null) {
			String message = "Error cannot find " + this.parserInvocation.getMasterFileName() + " in input folder.";
			parserInvocation.getParserLog().addError(ParserError.MASTER_FILE_MISSING,message);
			log.info(message);
			return false;
		}
		if (buildOWLOntology()) {
			if (serializeOntology()) {
				return true;
			} else {
				return false;
			}
		} else {
			//abort error in parsing
			return false;
		}
	}
	
	private boolean serializeOntology() {
		log.info("Serializing ontology in RDF ...");
		TurtleOntologyFormat turtle = new TurtleOntologyFormat();
		RDFXMLOntologyFormat rdfxml = new RDFXMLOntologyFormat();

		File output = new File(parserInvocation.getOutputRepositoryFolder()+
				File.separator+"owlapi.xrdf");
		IRI newPath = IRI.create("file:"+output.getAbsolutePath());
		try {
			this.targetOwlManager.saveOntology(this.targetOwlOntology, rdfxml, newPath);
		} catch (OWLOntologyStorageException e) {
			log.log(Level.ALL, e.getMessage(),e);
			StringWriter trace = new StringWriter();
			e.printStackTrace(new PrintWriter(trace));
			parserInvocation.getParserLog().addError(ParserError.OWL_STORAGE_EXCEPTION, 
					"Error buildOWLOntology" + e.getMessage() +
					"\n" + trace.toString());
			return false;
		}
		log.info("Serialization done!");
		return true;
	}

	private OWLOntology findMasterFile() {

 	  OWLOntologyLoaderConfiguration conf = new OWLOntologyLoaderConfiguration();
 	  conf = conf.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
 	 LogMissingImports missingHandler = new LogMissingImports(parserInvocation.getParserLog());
 	 this.sourceOwlManager.addMissingImportListener(missingHandler);

		if (this.parserInvocation.getInputRepositoryFolder() == null) {
	       try {
	    	   return this.sourceOwlManager.loadOntologyFromOntologyDocument(
	    			   new FileDocumentSource( new File(this.parserInvocation.getMasterFileName())), conf);
			} catch (OWLOntologyCreationException e) {
				log.log(Level.SEVERE, e.getMessage(),e);
				StringWriter trace = new StringWriter();
				e.printStackTrace(new PrintWriter(trace));
				parserInvocation.getParserLog().addError(ParserError.OWL_PARSE_EXCEPTION, 
						"Error parsing" + this.parserInvocation.getMasterFileName() +
						"\n" + e.getMessage() +
						"\n" + trace.toString());
				log.info(e.getMessage());
				return null;
			}
		}
		
		//repo input for zip files
		File master = new File(new File(parserInvocation.getInputRepositoryFolder()), this.parserInvocation.getMasterFileName());
		log.info("---> master.getAbsolutePath(): " + master.getAbsolutePath());

		OntologyBean selectedBean = null;
		for (OntologyBean b : this.ontologies) {
			log.info("---> " + b.getFile().getAbsolutePath() + " --> " + master.getAbsolutePath().equals(b.getFile().getAbsolutePath()) );
			if (b.getFile().getAbsolutePath().equals(master.getAbsolutePath())) {
				selectedBean = b;
			}
		}
		if (selectedBean == null) {
			for (OntologyBean b : this.ontologies) {
				log.info("---> " + b.getFile().getAbsolutePath() + " --> " + master.getAbsolutePath().equals(b.getFile().getAbsolutePath()) );
				if (b.getFile().getName().equals(this.parserInvocation.getMasterFileName())) {
					selectedBean = b;
				}
			}
		}
		
		log.info("Selected master file " + selectedBean.getFile().getAbsolutePath());
				
		if (selectedBean != null) {
	       try {
	    	   return this.sourceOwlManager.loadOntologyFromOntologyDocument(
	    			   new FileDocumentSource(selectedBean.getFile()), conf);
			} catch (OWLOntologyCreationException e) {
				log.log(Level.SEVERE, e.getMessage(),e);
				StringWriter trace = new StringWriter();
				e.printStackTrace(new PrintWriter(trace));
				parserInvocation.getParserLog().addError(ParserError.OWL_PARSE_EXCEPTION, 
						"Error parsing" + selectedBean.getFile().getAbsolutePath() +
						"\n" + e.getMessage() +
						"\n" + trace.toString());
				log.info(e.getMessage());
				return null;
			}
		}
		return null;
	}

	public Set<OWLOntology> getParsedOntologies() {
		return this.sourceOwlManager.getOntologies();
	}
	
	private static Pattern SEPARATOR_PATTERN = Pattern.compile("([^#_|_]+)(#_|_)(.+)");
	private String toOBOId(IRI iri) {
	       String value = iri.toString();
	       String localPart = "";
	       if (value.startsWith(OBOPrefix.OBO.getPrefix())) {
	           localPart = value.substring(OBOPrefix.OBO.getPrefix().length());
	       }
	       else if (value.startsWith(OBOPrefix.OBO_IN_OWL.getPrefix())) {
	           localPart = value.substring(OBOPrefix.OBO_IN_OWL.getPrefix().length());

	       }
	       else if (value.startsWith(OBOPrefix.IAO.getPrefix())) {
	           localPart = value.substring(OBOPrefix.IAO.getPrefix().length());
	       }
	       else {
	           String fragment = iri.getFragment();
	           if (fragment != null) {
	               localPart = fragment;
	           }
	           else {
	               localPart = value;
	           }
	       }
	       Matcher matcher = SEPARATOR_PATTERN.matcher(localPart);
	       if (matcher.matches()) {
	           StringBuilder sb = new StringBuilder();
	           sb.append(matcher.group(1));
	           sb.append(":");
	           sb.append(matcher.group(3));
	           return sb.toString();
	       }
	       else {
	           return value;
	       }
	   }
}
