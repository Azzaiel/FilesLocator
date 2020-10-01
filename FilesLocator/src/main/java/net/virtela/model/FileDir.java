package net.virtela.model;

import java.util.List;

public class FileDir implements Cloneable {

	private String fileName;
	private FileDir child;

	private List<FileDir> childList;

	public FileDir(String fileName) {
		super();
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public List<FileDir> getChildList() {
		return childList;
	}

	public void setChildList(List<FileDir> childList) {
		this.childList = childList;
	}

	public FileDir getChild() {
		return child;
	}

	public void setChild(FileDir child) {
		this.child = child;
	}

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

}
