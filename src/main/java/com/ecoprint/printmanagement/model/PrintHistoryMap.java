package com.ecoprint.printmanagement.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

public class PrintHistoryMap {

	private PrintJobDTO job;
	
	private List<PrintHistoryDTO> history;

	public PrintHistoryMap(PrintJobDTO jobDTO, List<PrintHistoryDTO> jobHistory) {
		this.job = jobDTO;
		this.history = new ArrayList();
		this.history.addAll(jobHistory);
	}
	
}
