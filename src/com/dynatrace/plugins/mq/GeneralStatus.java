package com.dynatrace.plugins.mq;

public enum GeneralStatus {
	STOPPED (0),
	RUNNING (1); 

	final private int statusCode;
	private GeneralStatus(int i)
	{
		statusCode = i;
	}
	public int getStatusCode() {
		return statusCode;
	}
} 
