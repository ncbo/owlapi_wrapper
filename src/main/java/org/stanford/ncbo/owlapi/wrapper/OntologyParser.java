package org.stanford.ncbo.owlapi.wrapper;

import com.google.common.base.Optional;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.coode.owlapi.obo12.parser.OBO12DocumentFormat;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.OBODocumentFormat;
import org.semanticweb.owlapi.formats.PrefixDocumentFormat;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.util.AutoIRIMapper;
import org.semanticweb.owlapi.util.InferredSubClassAxiomGenerator;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stanford.ncbo.owlapi.wrapper.metrics.OntologyMetrics;
import uk.ac.manchester.cs.owl.owlapi.OWLLiteralImplString;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

public class OntologyParser {
	private final static Logger log = LoggerFactory.getLogger(OntologyParser.class.getName());
	public static final String USE_IMPORTS_IRI = "http://omv.ontoware.org/2005/05/ontology#useImports";
	public static final String VERSION_SUBJECT = "http://bioportal.bioontology.org/ontologies/versionSubject";

	protected ParserInvocation parserInvocation = null;
	private ParserLog parserLog = null;
	private List<OntologyBean> ontologies = new ArrayList<OntologyBean>();
	private OWLOntologyManager sourceOwlManager = null;
	private OWLOntologyManager targetOwlManager = null;
	private OWLOntology targetOwlOntology = null;

	public OntologyParser(ParserInvocation parserInvocation) throws OntologyParserException {
		super();
		log.info("executor ...");

		if (!parserInvocation.valid()) {
			throw new OntologyParserException(parserInvocation.getParserLog());
		}
		this.parserInvocation = parserInvocation;
		this.parserLog = this.parserInvocation.getParserLog();

		this.sourceOwlManager = OWLManager.createOWLOntologyManager();
		setLocalFileRepositaryMapping(this.sourceOwlManager, this.parserInvocation.getInputRepositoryFolder());

		this.targetOwlManager = OWLManager.createOWLOntologyManager();
	}


	public OWLOntology getTargetOwlOntology() {
		return targetOwlOntology;
	}


	public List<OntologyBean> getLocalOntologies() {
		return ontologies;
	}

	private void setLocalFileRepositaryMapping(OWLOntologyManager m,
			String folder) {
		if (this.parserInvocation.getInputRepositoryFolder() != null) {
			File rooDirectory = new File(folder);
			m.getIRIMappers().add(new AutoIRIMapper(rooDirectory, true));
		}
	}


	public String getOBODataVersion(String file) {
		String result = null;
		String line = null;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			while ((line = reader.readLine()) != null) {
				if (line.contains("data-version:")) {
					String[] version = line.split(" ");
					if (version.length > 1) {
						reader.close();
						return version[1];
					}
				}
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Determines the list of ontologies to parse, and sets the obo version if applicable.
	 */
	private void findLocalOntologies() {
		String oboVersion = null;
		String inputRepositoryFolder = parserInvocation.getInputRepositoryFolder();
		String masterFileName = parserInvocation.getMasterFileName();
		int invocationId = parserInvocation.getInvocationId();

		if (inputRepositoryFolder != null) {
			log.info(String.format("[%d] findLocalOntologies in %s", invocationId, inputRepositoryFolder));

			File repo = new File(inputRepositoryFolder);
			if (repo.isDirectory()) {
				@SuppressWarnings("unchecked")
				Iterator<File> files = FileUtils.iterateFiles(repo, new OntologySuffixFileFilter(), TrueFileFilter.INSTANCE);
				ontologies = new ArrayList<OntologyBean>();
				while (files.hasNext()) {
					File f = files.next();
					if (f.getAbsolutePath().toLowerCase().endsWith("obo")) {
						oboVersion = getOBODataVersion(f.getAbsolutePath());
					}
					ontologies.add(new OntologyBean(f));
					log.info(String.format("[%d] Found ontology: %s", invocationId, f.getName()));
				}
			}
		} else {
			if (masterFileName.toLowerCase().endsWith("obo")) {
				oboVersion = getOBODataVersion(masterFileName);
			}
			ontologies.add(new OntologyBean(new File(masterFileName)));
			log.info("Input repository folder is null. Unique file being parsed.");
		}
		if (oboVersion != null) {
			parserInvocation.setOBOVersion(oboVersion);
		}
	}

	private boolean isOBO(OWLOntology ontology) {
		boolean isOBO = false;
		OWLDocumentFormat format = sourceOwlManager.getOntologyFormat(ontology);
		if ((format instanceof OBODocumentFormat) || (format instanceof OBO12DocumentFormat)) {
			isOBO = true;
		}
		log.info("Ontology document format: {}", format.getClass().getName());
		return isOBO;
	}

  /**
   * Get ontology URI and imports. Add the ontology URI to the submission graph
   * (<http://bioportal.bioontology.org/ontologies/URI> owl:versionInfo
   * "ONTOLOGY_IRI"). And add all ontology imports to <ONTOLOGY_URI>
   * omv:useImports <import_URI>
   *
   * @param fact
   * @param sourceOnt
   */
  private void addOntologyIRIAndImports(OWLDataFactory fact, OWLOntology sourceOnt) {
    if (!sourceOnt.getOntologyID().isAnonymous()) {

      // Get ontology URI
      Optional<IRI> sub = sourceOnt.getOntologyID().getOntologyIRI();
      IRI ontologyIRI = sub.get();

      OWLAnnotationProperty prop = fact.getOWLAnnotationProperty(IRI.create(OWLRDFVocabulary.OWL_VERSION_INFO.toString()));
      OWLAnnotationAssertionAxiom annOntoURI = fact.getOWLAnnotationAssertionAxiom(prop, IRI.create("http://bioportal.bioontology.org/ontologies/URI"), fact.getOWLLiteral(ontologyIRI.toString()));
      this.targetOwlManager.addAxiom(targetOwlOntology, annOntoURI);

      // Get imports and add them as omv:useImports
      OWLAnnotationProperty useImportProp = fact.getOWLAnnotationProperty(IRI.create("http://omv.ontoware.org/2005/05/ontology#useImports"));
      for (OWLOntology imported : sourceOnt.getImports()) {
        if (!imported.getOntologyID().isAnonymous()) {
          log.info("useImports: " + imported.getOntologyID().getOntologyIRI().get().toString());
          OWLAnnotationAssertionAxiom useImportAxiom = fact.getOWLAnnotationAssertionAxiom(useImportProp, ontologyIRI, imported.getOntologyID().getOntologyIRI().get());
          this.targetOwlManager.addAxiom(targetOwlOntology, useImportAxiom);
        }
      }

      /* Done in addGroundMetadata now (Add ontology metadatas to <ONTOLOGY_URI> <metadata_property> <metadata_value>)
      for (OWLAnnotation ann : sourceOnt.getAnnotations()) {
        OWLAnnotationAssertionAxiom groundAnnotation = fact.getOWLAnnotationAssertionAxiom(ann.getProperty(), ontologyIRI, ann.getValue());
        this.targetOwlManager.addAxiom(targetOwlOntology, groundAnnotation);
      }
      */
    }
  }

	/**
	 * Get the source ontology IRI and add it to the target ontology
	 *
	 * @param sourceOnt
	 */
	private void addOntologyIRI(OWLOntology sourceOnt) {
		Optional<IRI> ontologyIRI = sourceOnt.getOntologyID().getOntologyIRI();
		Optional<IRI> versionIRI =  sourceOnt.getOntologyID().getVersionIRI();
		if (ontologyIRI.isPresent()) {
			OWLOntologyID newOntologyID = new OWLOntologyID(ontologyIRI, versionIRI);
			SetOntologyID setOntologyID = new SetOntologyID(targetOwlOntology, newOntologyID);
			this.targetOwlManager.applyChange(setOntologyID);
		}
	}

	/**
	 * Get ontology imports and them to <ONTOLOGY_URI>
	 * omv:useImports <import_URI>
	 *
	 * @param fact
	 * @param sourceOnt
	 */
	private void addOntologyImports(OWLDataFactory fact, OWLOntology sourceOnt){
		Optional<IRI> sub = sourceOnt.getOntologyID().getOntologyIRI();
		IRI ontologyIRI = sub.get();
		if(!sub.isPresent()){
            return;
        }
		// Get imports and add them as omv:useImports
		OWLAnnotationProperty useImportProp = fact.getOWLAnnotationProperty(IRI.create(USE_IMPORTS_IRI));
		for (OWLOntology imported : sourceOnt.getImports()) {
			if (!imported.getOntologyID().isAnonymous()) {
				log.info("useImports: " + imported.getOntologyID().getOntologyIRI().get());
				OWLAnnotationAssertionAxiom useImportAxiom = fact.getOWLAnnotationAssertionAxiom(useImportProp, ontologyIRI, imported.getOntologyID().getOntologyIRI().get());
				this.targetOwlManager.addAxiom(targetOwlOntology, useImportAxiom);
			}
		}
	}

	/**
	 * Copies ontology-level annotation axioms from the source ontology to the target ontology.
	 * <p>
	 * Checks for the owl#versionInfo property. If found, adds a BioPortal-specific "versionSubject"
	 * annotation axiom to the target ontology.
	 *
	 * @param documentIRI		the document IRI of the source ontology
	 * @param factory			the OWL data factory of the source ontology
	 * @param sourceOntology	the source ontology
	 */
	private void addGroundMetadata(IRI documentIRI, OWLDataFactory factory, OWLOntology sourceOntology) {
		OWLOntologyID ontologyID = sourceOntology.getOntologyID();
		boolean isFile = documentIRI.toString().startsWith("file:/");

		if (ontologyID.isAnonymous()) {
			return;
		}

		for (OWLAnnotation annotation : sourceOntology.getAnnotations()) {

			IRI subjectIRI = ontologyID.getOntologyIRI().get();
			OWLAnnotationProperty annotationProperty = annotation.getProperty();
			OWLAnnotationValue annotationValue = annotation.getValue();

			OWLAnnotationAssertionAxiom annotationAssertionAxiom = factory.getOWLAnnotationAssertionAxiom(annotationProperty, subjectIRI, annotationValue);
			targetOwlManager.addAxiom(targetOwlOntology, annotationAssertionAxiom);

			if (isFile && (annotationProperty.toString().contains("versionInfo"))) {
				IRI versionSubjectIRI = IRI.create(VERSION_SUBJECT);
				OWLAnnotationProperty versionAnnotationProperty = factory.getOWLAnnotationProperty(OWLRDFVocabulary.OWL_VERSION_INFO.getIRI());
				OWLAnnotationAssertionAxiom versionAnnotationAssertionAxiom = factory.getOWLAnnotationAssertionAxiom(versionAnnotationProperty, versionSubjectIRI, annotationValue);
				targetOwlManager.addAxiom(targetOwlOntology, versionAnnotationAssertionAxiom);
			}
		}
	}

	private boolean buildOWLOntology(OWLOntology masterOntology, boolean isOBO) {

		Set<OWLAxiom> allAxioms = new HashSet<OWLAxiom>();

		OWLDataFactory fact = sourceOwlManager.getOWLDataFactory();
		try {
			targetOwlOntology = targetOwlManager.createOntology();
			addOntologyIRI(masterOntology);
			addOntologyImports(fact, masterOntology);
		} catch (OWLOntologyCreationException e) {
			log.error(e.getMessage());
			parserLog.addError(ParserError.OWL_CREATE_ONTOLOGY_EXCEPTION, "Error buildOWLOntology" + e.getMessage());
			return false;
		}


		for (OWLOntology sourceOnt : sourceOwlManager.getOntologies()) {
			IRI documentIRI = sourceOwlManager.getOntologyDocumentIRI(sourceOnt);

        addGroundMetadata(documentIRI, fact, masterOntology);
        generateGroundTriplesForAxioms(allAxioms, fact, masterOntology);

			if (isOBO && !documentIRI.toString().startsWith("owlapi:ontology")) {
				generateSKOSInObo(allAxioms, fact, sourceOnt);
			}

			boolean isPrefixedOWL = sourceOwlManager.getOntologyFormat(sourceOnt).isPrefixOWLOntologyFormat();
			log.info("isPrefixOWLOntologyFormat: {}", isPrefixedOWL);
			if (isPrefixedOWL && !isOBO) {
				generateSKOSInOwl(allAxioms, fact, sourceOnt);
			}
		}

        targetOwlManager.addAxioms(targetOwlOntology, allAxioms);
        for (OWLAnnotation ann : targetOwlOntology.getAnnotations()) {
            AddOntologyAnnotation addAnn = new AddOntologyAnnotation(targetOwlOntology, ann);
            targetOwlManager.applyChange(addAnn);
        }

        if (isOBO) {
            String oboVersion = parserInvocation.getOBOVersion();
            if (oboVersion != null) {
                log.info("Adding version: {}", oboVersion);
                OWLAnnotationProperty prop = fact.getOWLAnnotationProperty(IRI.create(OWLRDFVocabulary.OWL_VERSION_INFO.toString()));
                IRI versionSubjectIRI = IRI.create(VERSION_SUBJECT);
                OWLAnnotationAssertionAxiom annVersion = fact.getOWLAnnotationAssertionAxiom(prop, versionSubjectIRI, fact.getOWLLiteral(oboVersion));
                targetOwlManager.addAxiom(targetOwlOntology, annVersion);
            }
        }

		addOntologyAnnotations(masterOntology);
        escapeXMLLiterals(targetOwlOntology);

        OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
        OWLReasoner reasoner = reasonerFactory.createReasoner(targetOwlOntology);
        InferredSubClassAxiomGenerator isc = new InferredSubClassAxiomGenerator();
        Set<OWLSubClassOfAxiom> subAxs = isc.createAxioms(targetOwlOntology.getOWLOntologyManager().getOWLDataFactory(), reasoner);
        targetOwlManager.addAxioms(targetOwlOntology, subAxs);
        deprecateBranch();

        log.info("isOBO: {}", isOBO);
        if (isOBO) {
            replicateHierarchyAsTreeview(fact);
        }
        return true;
    }

	private void addOntologyAnnotations(OWLOntology masterOntology){
		for (OWLAnnotation ann : masterOntology.getAnnotations()) {
			AddOntologyAnnotation addAnn = new AddOntologyAnnotation(targetOwlOntology, ann);
			targetOwlManager.applyChange(addAnn);
		}
	}
	private void replicateHierarchyAsTreeview(OWLDataFactory fact) {
		Set<OWLAxiom> treeViewAxs = new HashSet<OWLAxiom>();
		for (OWLAxiom axiom : targetOwlOntology.getAxioms()) {
			if (axiom instanceof OWLSubClassOfAxiom) {
				OWLSubClassOfAxiom scAxiom = (OWLSubClassOfAxiom) axiom;
				OWLAnnotationProperty prop = fact
						.getOWLAnnotationProperty(IRI
								.create("http://data.bioontology.org/metadata/treeView"));
				if (!scAxiom.getSubClass().isAnonymous()
						&& !scAxiom.getSuperClass().isAnonymous()) {
					OWLAxiom annAsse = fact.getOWLAnnotationAssertionAxiom(
							prop, scAxiom.getSubClass().asOWLClass().getIRI(),
							scAxiom.getSuperClass().asOWLClass().getIRI());
					treeViewAxs.add(annAsse);
				}
			}
		}
		targetOwlManager.addAxioms(targetOwlOntology, treeViewAxs);
	}

	/**
	 * Checks all root-level ontology classes for deprecation markers. If such a marker is found,
	 * i.e. owl:deprecated, the ontology class is removed from the list of roots.
	 * <p>
	 * Facilitates desired functionality in the <a href="http://bioportal.bioontology.org/">BioPortal</a>
	 * application for the display of ontology class trees without deprecated branches.
	 */
	private void deprecateBranch() {
		OWLClass thing = targetOwlManager.getOWLDataFactory().getOWLThing();

		Set<OWLSubClassOfAxiom> rootsEdges = targetOwlOntology.getSubClassAxiomsForSuperClass(thing);
		for (OWLSubClassOfAxiom rootEdge : rootsEdges) {
			if (!rootEdge.getSubClass().isAnonymous()) {
				OWLClass subClass = (OWLClass) rootEdge.getSubClass();
				String rootID = subClass.getIRI().toString();
				if (rootID.toLowerCase().contains("obo")) {
					Collection<OWLAnnotation> annotationsRoot = EntitySearcher.getAnnotations(subClass, targetOwlOntology);
					boolean hasLabel = false;

					for (OWLAnnotation annRoot : annotationsRoot) {
						hasLabel = hasLabel
								|| annRoot.getProperty().toString().equals("http://www.w3.org/2000/01/rdf-schema#label")
								|| annRoot.getProperty().toString().equals("rdfs:label");
						if (annRoot.isDeprecatedIRIAnnotation()) {
							if (annRoot.getValue().toString().contains("true")) {
								RemoveAxiom remove = new RemoveAxiom(targetOwlOntology, rootEdge);
								targetOwlManager.applyChange(remove);
							}
						}
					}

					Collection<OWLAnnotationAssertionAxiom> assRoot = EntitySearcher.getAnnotationAssertionAxioms(subClass, targetOwlOntology);
					for (OWLAnnotationAssertionAxiom annRoot : assRoot) {
						if (annRoot.getProperty().toString().contains("treeView")) {
							RemoveAxiom remove = new RemoveAxiom(targetOwlOntology, rootEdge);
							targetOwlManager.applyChange(remove);
						}
					}

					if (!hasLabel) {
						RemoveAxiom remove = new RemoveAxiom(targetOwlOntology, rootEdge);
						targetOwlManager.applyChange(remove);
					}
				}
			}
		}
	}

	private void generateSKOSInOwl(Set<OWLAxiom> allAxioms, OWLDataFactory fact, OWLOntology sourceOnt) {
		OWLDocumentFormat docFormat = this.sourceOwlManager.getOntologyFormat(sourceOnt);
		PrefixDocumentFormat prefixFormat = docFormat.asPrefixOWLOntologyFormat();

		Set<OWLClass> classes = sourceOnt.getClassesInSignature();
		for (OWLClass cls : classes) {
			if (!cls.isAnonymous()) {
				boolean notationFound = false;

				for (OWLAnnotation ann : EntitySearcher.getAnnotations(cls, targetOwlOntology)) {
					if (ann.getProperty().toString().contains("http://www.w3.org/2004/02/skos/core#notation")) {
						notationFound = true;
						break;
					}
				}

				if (notationFound) {
					continue;
				}

				for (OWLAnnotation ann : EntitySearcher.getAnnotations(cls, sourceOnt)) {
					if (ann.getProperty().toString().contains("http://www.geneontology.org/formats/oboInOwl#id")) {
						OWLAnnotationProperty prop = fact.getOWLAnnotationProperty(IRI.create("http://www.w3.org/2004/02/skos/core#notation"));
						OWLAxiom annAsse = fact.getOWLAnnotationAssertionAxiom(prop, cls.getIRI(), ann.getValue());
						allAxioms.add(annAsse);
						notationFound = true;
						break;
					}
				}

				if (notationFound) {
					continue;
				}

				String prefixIRI = prefixFormat.getPrefixIRI(cls.getIRI());
				if (prefixIRI != null) {
					if (prefixIRI.startsWith(":")) {
						prefixIRI = prefixIRI.substring(1);
					}

					if (prefixIRI.startsWith("obo:") && prefixIRI.contains("_")) {
						// OBO ontologies transformed into OWL before submitting to BioPortal
						prefixIRI = prefixIRI.substring(4);
						StringBuilder b = new StringBuilder(prefixIRI);
						int ind = prefixIRI.lastIndexOf("_");
						b.replace(ind, ind + 1, ":");
						prefixIRI = b.toString();
					}

					OWLAnnotationProperty prop = fact.getOWLAnnotationProperty(IRI.create("http://data.bioontology.org/metadata/prefixIRI"));
					OWLAxiom annAsse = fact.getOWLAnnotationAssertionAxiom(prop, cls.getIRI(), fact.getOWLLiteral(prefixIRI));
					allAxioms.add(annAsse);
				}
			}
		}
	}

	/**
	 * Generates a set of annotation axioms for the target ontology.
	 * <p>
	 * The purpose of the axioms is to add a skos:notation annotation property to every class in the ontology,
	 * where the value is set to the OBO term ID. The notation property is meant to act as a unique code for ontology
	 * classes, which facilitates class ID lookups across various ontology formats in the BioPortal application.
	 *
	 * @param allAxioms			the set of axioms for the target ontology
	 * @param factory			the OWL data factory of the source ontology
	 * @param sourceOntology	the source ontology
	 */
	private void generateSKOSInObo(Set<OWLAxiom> allAxioms, OWLDataFactory factory, OWLOntology sourceOntology) {
		IRI notationPropertyIRI = IRI.create("http://www.w3.org/2004/02/skos/core#notation");
		OWLAnnotationProperty property = factory.getOWLAnnotationProperty(notationPropertyIRI);

		Set<OWLClass> classes = sourceOntology.getClassesInSignature();
		for (OWLClass c : classes) {
			Optional<String> remainder = c.getIRI().getRemainder();
			if (remainder.isPresent()) {
				OWLAnnotationSubject subject = c.getIRI();

				String classID = remainder.get().replace("_", ":");
				OWLLiteralImplString value = new OWLLiteralImplString(classID);

				OWLAnnotationAssertionAxiom annotationAssertionAxiom = factory.getOWLAnnotationAssertionAxiom(property, subject, value);
				allAxioms.add(annotationAssertionAxiom);
			}
		}
	}

	private void escapeXMLLiterals(OWLOntology target) {
		OWLDataFactory td = targetOwlManager.getOWLDataFactory();
		Set<OWLClass> classes = target.getClassesInSignature();
		for (OWLClass cls : classes) {
			if (!cls.isAnonymous()) {
				for (OWLAnnotationAssertionAxiom ann : EntitySearcher.getAnnotationAssertionAxioms(cls, target)) {
					for (OWLDatatype t : ann.getValue().getDatatypesInSignature()) {
						if (t.toString().contains("XMLLiteral")) {
							System.out.println("stripping xml " + cls.getIRI().toString() +" "+ ann.getProperty().toString());
							String noXMLString = ann.getValue().asLiteral().get().getLiteral().replaceAll("\\<.*?\\>", "");
							System.out.println(ann.getValue().toString());
							System.out.println(noXMLString);
							OWLAnnotationAssertionAxiom annAsse = td.getOWLAnnotationAssertionAxiom(
									ann.getProperty(), cls.getIRI(), td.getOWLLiteral(noXMLString));
							targetOwlManager.addAxiom(target, annAsse);
							Set<OWLAnnotationAssertionAxiom> del = new HashSet<OWLAnnotationAssertionAxiom>();
							del.add(ann);
							targetOwlManager.removeAxioms(target,del);
						}
					}
				}
			}
		}
	}

	private void generateGroundTriplesForAxioms(Set<OWLAxiom> allAxioms, OWLDataFactory fact, OWLOntology sourceOnt) {

		for (OWLAxiom axiom : sourceOnt.getAxioms()) {
			allAxioms.add(axiom);

			if (axiom instanceof OWLSubClassOfAxiom) {
				OWLSubClassOfAxiom sc = (OWLSubClassOfAxiom) axiom;
				OWLClassExpression ce = sc.getSuperClass();
				try {
					sc.getSubClass().asOWLClass().getIRI();
				} catch (OWLRuntimeException exc) {
					continue;
				}

				if (ce instanceof OWLObjectSomeValuesFrom) {
					OWLObjectSomeValuesFrom some = (OWLObjectSomeValuesFrom) ce;

					if (!some.getProperty().isAnonymous() && !some.getFiller().isAnonymous()) {
						String propSome = some.getProperty().asOWLObjectProperty().getIRI().toString().toLowerCase();

						if (propSome.contains("obo")) {

							if (propSome.endsWith("part_of")
									|| propSome.endsWith("bfo_0000050")
									|| propSome.endsWith("contains")
									|| propSome.endsWith("ro_0001019")
									|| propSome.endsWith("develops_from")
									|| propSome.endsWith("ro_0002202")) {

								OWLAnnotationProperty prop = null;
								if (propSome.endsWith("contains") || propSome.endsWith("ro_0001019")) {
									prop = fact.getOWLAnnotationProperty(IRI.create("http://data.bioontology.org/metadata/obo/contains"));
									OWLAxiom annAsse = fact.getOWLAnnotationAssertionAxiom(prop, some.getFiller().asOWLClass().getIRI(), sc.getSubClass().asOWLClass().getIRI());
									allAxioms.add(annAsse);

									prop = fact.getOWLAnnotationProperty(IRI.create("http://data.bioontology.org/metadata/treeView"));
									annAsse = fact.getOWLAnnotationAssertionAxiom(prop, some.getFiller().asOWLClass().getIRI(), sc.getSubClass().asOWLClass().getIRI());
									allAxioms.add(annAsse);
								} else {
									if (propSome.endsWith("part_of") || propSome.endsWith("bfo_0000050"))
										prop = fact.getOWLAnnotationProperty(IRI.create("http://data.bioontology.org/metadata/obo/part_of"));
									else {
										prop = fact.getOWLAnnotationProperty(IRI.create("http://data.bioontology.org/metadata/obo/develops_from"));
									}

									OWLAxiom annAsse = fact.getOWLAnnotationAssertionAxiom(prop, sc.getSubClass().asOWLClass().getIRI(), some.getFiller().asOWLClass().getIRI());
									allAxioms.add(annAsse);

									prop = fact.getOWLAnnotationProperty(IRI.create("http://data.bioontology.org/metadata/treeView"));
									annAsse = fact.getOWLAnnotationAssertionAxiom(prop, sc.getSubClass().asOWLClass().getIRI(), some.getFiller().asOWLClass().getIRI());
									allAxioms.add(annAsse);
								}
							} else {
								if (!some.getFiller().isAnonymous() && !sc.getSubClass().isAnonymous()) {
									OWLAnnotationProperty prop = fact.getOWLAnnotationProperty(some.getProperty().asOWLObjectProperty().getIRI());
									OWLAxiom annAsse = fact.getOWLAnnotationAssertionAxiom(prop, sc.getSubClass().asOWLClass().getIRI(), some.getFiller().asOWLClass().getIRI());
									allAxioms.add(annAsse);
								}
							}
						}
					}
				}
			}
		}
	}

	/*
	 * Parses one or more ontology files.
	 */
	public boolean parse() throws Exception {
		boolean result = false;

		try {
			result = internalParse();
		} catch (Exception e) {
			log.error(e.getMessage());
			parserLog.addError(ParserError.UNKNOWN, "Error " + e.getMessage());
		}

		if (parserLog.getErrors().size() > 0) {
			parserInvocation.saveErrors();
		}

		return result;
	}

	private boolean internalParse() {
		findLocalOntologies();

		OWLOntology ontology = findMasterFile();

		if (ontology == null) {
			String msg = String.format("Can't process %s in input folder! Allowed file extensions are the following ones : %s.", parserInvocation.getMasterFileName(), Arrays.toString(OntologySuffixFileFilter.acceptedFileExtensions));
			parserLog.addError(ParserError.MASTER_FILE_MISSING, msg);
			log.info(msg);
			return false;
		}

		OntologyMetrics metrics = new OntologyMetrics(ontology, parserInvocation);
		metrics.generate();

		boolean isOBO = isOBO(ontology);

		if (!buildOWLOntology(ontology, isOBO)) return false;

		if (!serializeOntology()) return false;

		return true;
	}

	private boolean serializeOntology() {
		log.info("Serializing ontology in RDF ...");
		File output = new File(parserInvocation.getOutputRepositoryFolder()
				+ File.separator + "owlapi.xrdf");
		IRI newPath = IRI.create("file:" + output.getAbsolutePath());
		try {
			this.targetOwlManager.saveOntology(this.targetOwlOntology, new RDFXMLDocumentFormat(),newPath);
		} catch (OWLOntologyStorageException e) {
			log.error(e.getMessage());
			parserLog.addError(ParserError.OWL_STORAGE_EXCEPTION, "Error buildOWLOntology" + e.getMessage());
			if (output.exists()) {
				output.renameTo(new File(parserInvocation.getOutputRepositoryFolder() + File.separator + "owlapi.xrdf.incomplete"));
			}
			return false;
		}
		log.info("Serialization done!");
		return true;
	}

	private OWLOntology findMasterFile() {
		LogMissingImports missingHandler = new LogMissingImports(parserLog);
		sourceOwlManager.addMissingImportListener(missingHandler);

		if (parserInvocation.getInputRepositoryFolder() == null) {
			try {
				File file = new File(parserInvocation.getMasterFileName());
				FileDocumentSource fileDocumentSource = new FileDocumentSource(file);
				OWLOntology ontology = sourceOwlManager.loadOntologyFromOntologyDocument(fileDocumentSource);
				return ontology;
			} catch (OWLOntologyCreationException e) {
				log.error(e.getMessage());
				parserLog.addError(ParserError.OWL_PARSE_EXCEPTION, e.getMessage());
				return null;
			}
		}

		// repo input for zip files
		File master = new File(new File(parserInvocation.getInputRepositoryFolder()), parserInvocation.getMasterFileName());
		log.info("master.getAbsolutePath(): {}", master.getAbsolutePath());

		OntologyBean selectedBean = null;
		for (OntologyBean b : ontologies) {
			if (b.getFile().getAbsolutePath().equals(master.getAbsolutePath())) {
				selectedBean = b;
			}
		}
		if (selectedBean == null) {
			for (OntologyBean b : ontologies) {
				if (b.getFile().getName().equals(parserInvocation.getMasterFileName())) {
					selectedBean = b;
				}
			}
		}

		if (selectedBean != null) {
			log.info("Selected master file: {}", selectedBean.getFile().getAbsolutePath());
			try {
				FileDocumentSource documentSource = new FileDocumentSource(selectedBean.getFile());
				OWLOntology ontology = sourceOwlManager.loadOntologyFromOntologyDocument(documentSource);
				return ontology;
			} catch (OWLOntologyCreationException e) {
				log.error(e.getMessage());
				parserLog.addError(ParserError.OWL_PARSE_EXCEPTION, e.getMessage());
				return null;
			}
		}
		return null;
	}

	public Set<OWLOntology> getParsedOntologies() {
		return this.sourceOwlManager.getOntologies();
	}

}
