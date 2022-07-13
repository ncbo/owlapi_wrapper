package org.stanford.ncbo.owlapi.wrapper;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class ParserInvocation {

	private boolean useReasoner = true;

	private int invocationId = 0;

	private ParserLog parserLog = new ParserLog();

	private String inputRepositoryFolder = null;

	private String outputRepositoryFolder = null;

	private String masterFileName = null;

	private String oboVersion;

	public ParserInvocation(String inputRepositoryFolder,
							String outputRepositoryFolder, String masterFileName,
							Boolean useReasoner) {
		super();
		this.inputRepositoryFolder = inputRepositoryFolder;
		this.outputRepositoryFolder = outputRepositoryFolder;
		this.masterFileName = masterFileName;
		this.useReasoner = useReasoner;
	}

	public String getInputRepositoryFolder() {
		return inputRepositoryFolder;
	}

	public void setInputRepositoryFolder(String inputRepositoryFolder) {
		this.inputRepositoryFolder = inputRepositoryFolder;
	}

	public String getOutputRepositoryFolder() {
		return outputRepositoryFolder;
	}

	public void setOutputRepositoryFolder(String outputRepositoryFolder) {
		this.outputRepositoryFolder = outputRepositoryFolder;
	}

	public String getMasterFileName() {
		return masterFileName;
	}

	public void setMasterFileName(String masterFileName) {
		this.masterFileName = masterFileName;
	}

	public String getOBOVersion() {
		return oboVersion;
	}

	public void setOBOVersion(String oboVersion) {
		this.oboVersion = oboVersion;
	}

	public int getInvocationId() {
		return invocationId;
	}

	public void setInvocationId(int invocationId) {
		this.invocationId = invocationId;
	}

	public boolean isUseReasoner() {
		return useReasoner;
	}

	public void setUseReasoner(boolean useReasoner) {
		this.useReasoner = useReasoner;
	}

	public ParserLog getParserLog() {
		return this.parserLog;
	}

	@Override
	public String toString() {
		return "ParserInvocation [inputRepositoryFolder="
				+ inputRepositoryFolder + ", outputRepositoryFolder="
				+ outputRepositoryFolder + ", masterFileName=" + masterFileName
				+ ", invocationId=" + invocationId + ", parserLog=" + parserLog
				+ ", userReasoner= " + useReasoner
				+ "]";
	}
	
	public boolean valid() {
		parserLog.flush();
		if (inputRepositoryFolder != null) {
			File inputFolder = new File(this.inputRepositoryFolder);
			if (!inputFolder.exists()) {
				parserLog.addError(ParserError.INPUT_REPO_MISSING);
				return false;
			} else if (!inputFolder.isDirectory()) {
				parserLog.addError(ParserError.INPUT_REPO_NOT_A_FOLDER);
				return false;
			}
		} else {
			if (this.masterFileName == null) {
				parserLog.addError(ParserError.MASTER_FILE_MISSING);
				return false;
			}
			File masterFile = new File(this.masterFileName);
			if (!masterFile.exists()) {
				parserLog.addError(ParserError.MASTER_FILE_MISSING);
				return false;
			} else if (masterFile.isDirectory()) {
				parserLog.addError(ParserError.MASTER_FILE_IS_FOLDER);
				return false;
			}
		}
		
		File outputFolder = new File(this.outputRepositoryFolder);
		if (!outputFolder.exists()) {
			try {
				FileUtils.forceMkdir(outputFolder);
			} catch (IOException e) {
				parserLog.addError(ParserError.OUPUT_REPO_CANNOT_BE_CREATED, "Folder " + outputFolder.getAbsolutePath() + "cannot be created");
			} 
		}
		else if (!outputFolder.isDirectory())
			parserLog.addError(ParserError.OUPUT_REPO_NOT_A_FOLDER);

		return parserLog.logErrors.size() == 0;
	}
	
	public void saveErrors() throws Exception {
		this.parserLog.writeTo(new File(outputRepositoryFolder + File.separator + "errors.log"));
	}

}
