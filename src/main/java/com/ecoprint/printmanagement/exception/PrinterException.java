package com.ecoprint.printmanagement.exception;

public class PrinterException extends Exception{
	

    public PrinterException(String message) {
        super(message);
    }
    
    public PrinterException(String message, Throwable cause) {
        super(message, cause);
    }


}
