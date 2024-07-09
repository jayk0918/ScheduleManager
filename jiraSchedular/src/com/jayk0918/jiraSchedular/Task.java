package com.jayk0918.jiraSchedular;

import java.io.Serializable;

public class Task implements Serializable {
    private static final long serialVersionUID = 1L;

    private String crNumber;
    private String title;
    private String reflectionDate;
    private String progress;
    private String remarks;
    
    public Task() {}
    
	public Task(String crNumber, String title, String reflectionDate, String progress, String remarks) {
		super();
		this.crNumber = crNumber;
		this.title = title;
		this.reflectionDate = reflectionDate;
		this.progress = progress;
		this.remarks = remarks;
	}
	
	public String getCrNumber() {
		return crNumber;
	}
	public void setCrNumber(String crNumber) {
		this.crNumber = crNumber;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getReflectionDate() {
		return reflectionDate;
	}
	public void setReflectionDate(String reflectionDate) {
		this.reflectionDate = reflectionDate;
	}
	public String getProgress() {
		return progress;
	}
	public void setProgress(String progress) {
		this.progress = progress;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
    
	

}