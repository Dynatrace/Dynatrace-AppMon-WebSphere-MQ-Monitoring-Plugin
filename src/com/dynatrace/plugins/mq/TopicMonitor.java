package com.dynatrace.plugins.mq;

import java.util.Collection;
import java.util.regex.Pattern;

import com.dynatrace.diagnostics.pdk.MonitorEnvironment;
import com.dynatrace.diagnostics.pdk.MonitorMeasure;
import com.dynatrace.diagnostics.pdk.Status;
import com.dynatrace.diagnostics.pdk.Status.StatusCode;

public class TopicMonitor extends MQSCMonitor {
	private static final String COMMAND_TOPIC = "DISPLAY TPSTATUS(\'%s\') TYPE(TOPIC) ALL\nEND\n";
	
	private static final String METRIC_GROUP_TOPIC = "WebSphere MQ Topic";
	
	private static final String METRIC_TOPIC_PUBLISHER_COUNT = "Publisher Count"; 
	private static final String METRIC_TOPIC_SUBSCRIBER_COUNT = "Subscriber Count";
	
	private static final Pattern publisherCountPattern = Pattern.compile("PUBCOUNT\\((\\d*)\\)");
	private static final Pattern subscriberCountPattern = Pattern.compile("SUBCOUNT\\((\\d*)\\)");
	
	private String topicName;
	private int position;
	
	public TopicMonitor(String name, int position) {
		topicName = name;
		this.position = position;
	}
	
	@Override
	protected String getMQSCCommandPattern() {
		return COMMAND_TOPIC;
	}
	
	public String getName() {
		return topicName;
	}
	
	@Override
	public Status executeMonitor(MonitorEnvironment env, String commandOutput) {
		Status status = null;

		String publisherCountStr = getPublisherCount(commandOutput);
		if(publisherCountStr != null) {
			try {
				int publisherCountInt = Integer.parseInt(publisherCountStr);
				log.info("Publisher count="+publisherCountInt);
				
				Collection<MonitorMeasure> publisherCountMeasures =  env.getMonitorMeasures(METRIC_GROUP_TOPIC, METRIC_TOPIC_PUBLISHER_COUNT + position);
				for(MonitorMeasure mm : publisherCountMeasures) {
					mm.setValue(publisherCountInt);
				}
			} catch(Exception e) {
				String message = "Error while converting the number of publishers from String to int: " + e.getMessage(); 
				log.severe(message);
				status = new Status(Status.StatusCode.PartialSuccess, message);
			}
		}
		
		String subscriberCountStr = getSubscriberCount(commandOutput);
		if(subscriberCountStr != null) {
			try {
				int subscriberCountInt = Integer.parseInt(subscriberCountStr);
				log.info("Publisher count="+subscriberCountInt);
				
				Collection<MonitorMeasure> subscriberCountMeasures =  env.getMonitorMeasures(METRIC_GROUP_TOPIC, METRIC_TOPIC_SUBSCRIBER_COUNT + position);
				for(MonitorMeasure mm : subscriberCountMeasures) {
					mm.setValue(subscriberCountInt);
				}
			} catch(Exception e) {
				String message = "Error while converting the number of subscribers from String to int: " + e.getMessage(); 
				log.severe(message);
				status = new Status(Status.StatusCode.PartialSuccess, message);
			}
		}
		
		if(status == null) {
			status = new Status(StatusCode.Success);
		}
		return status; 
	}
	
	private String getPublisherCount(String commandOutput) {
		return getStringFromCommandOutput(commandOutput, publisherCountPattern);	
	}
	
	private String getSubscriberCount(String commandOutput) {
		return getStringFromCommandOutput(commandOutput, subscriberCountPattern);
	}
}
