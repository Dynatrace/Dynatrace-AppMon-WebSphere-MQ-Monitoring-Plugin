package com.dynatrace.plugins.mq;

public enum QueueManagerStatus {
	ENDED_UNEXPECTEDLY (0),
	ENDED_PREEMPTIVELY (1), 
	ENDED_IMMEDIATELY (2),
	ENDED_NORMALLY (3),
	ENDING_PREEMPTIVELY (4), 
	ENDING_IMMEDIATELY (5), 
	QUIESCING (6),
	STARTING (7),
	RUNNING_ELSEWHERE (8),
	RUNNING (9),
    RUNNING_AS_STANDBY (10);
	
	final private int statusCode;
	private QueueManagerStatus(int i)
	{
		statusCode = i;
	}
	public int getStatusCode() {
		return statusCode;
	}
} 
