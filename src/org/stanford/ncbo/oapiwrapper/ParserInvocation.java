package org.stanford.ncbo.oapiwrapper;

public class ParserInvocation {
	
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
	public String getMasterFilePath() {
		return masterFilePath;
	}
	public void setMasterFilePath(String masterFilePath) {
		this.masterFilePath = masterFilePath;
	}
	public boolean isUniqueFile() {
		return uniqueFile;
	}
	public void setUniqueFile(boolean uniqueFile) {
		this.uniqueFile = uniqueFile;
	}
	
	public ParserInvocation(String inputRepositoryFolder,
			String outputRepositoryFolder, String masterFilePath,
			boolean uniqueFile) {
		super();
		this.inputRepositoryFolder = inputRepositoryFolder;
		this.outputRepositoryFolder = outputRepositoryFolder;
		this.masterFilePath = masterFilePath;
		this.uniqueFile = uniqueFile;
	}

	private String inputRepositoryFolder = null;
	private String outputRepositoryFolder = null;
	private String masterFilePath = null;
	private boolean uniqueFile = true;
	
	@Override
	public String toString() {
		return "ParseInvocation [inputRepositoryFolder="
				+ inputRepositoryFolder + ", outputRepositoryFolder="
				+ outputRepositoryFolder + ", masterFilePath=" + masterFilePath
				+ ", uniqueFile=" + uniqueFile + "]";
	}
	
}
