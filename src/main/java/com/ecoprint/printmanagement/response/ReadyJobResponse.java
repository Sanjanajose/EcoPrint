package com.ecoprint.printmanagement.response;

public class ReadyJobResponse {
	
    private Long id;
    private String fileName;
    private Integer estimatedWaitTime;
    private String userName;
    
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public Integer getEstimatedWaitTime() {
		return estimatedWaitTime;
	}
	public void setEstimatedWaitTime(Integer estimatedWaitTime) {
		this.estimatedWaitTime = estimatedWaitTime;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}

    

}
