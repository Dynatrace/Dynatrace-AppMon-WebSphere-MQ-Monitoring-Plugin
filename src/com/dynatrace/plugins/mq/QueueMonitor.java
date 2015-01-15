package com.dynatrace.plugins.mq;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dynatrace.diagnostics.pdk.MonitorEnvironment;
import com.dynatrace.diagnostics.pdk.MonitorMeasure;
import com.dynatrace.diagnostics.pdk.Status;
import com.dynatrace.diagnostics.pdk.Status.StatusCode;

public class QueueMonitor extends MQSCMonitor {
	private static final String COMMAND_QUEUE = "DISPLAY QSTATUS(\'%s\') TYPE(QUEUE) ALL\nEND\n";

	private static final Pattern currentDepthPattern = Pattern.compile("CURDEPTH\\((\\d*)\\)");
	private static final Pattern lastPutTimePattern = Pattern.compile("LPUTTIME\\(([\\d\\.\\s]*)\\)");
	private static final Pattern lastPutDatePattern = Pattern.compile("LPUTDATE\\(([\\d\\-\\s]*)\\)");
	private static final Pattern lastGetTimePattern = Pattern.compile("LGETTIME\\(([\\d\\.\\s]*)\\)");
	private static final Pattern lastGetDatePattern = Pattern.compile("LGETDATE\\(([\\d\\-\\s]*)\\)");
	private static final Pattern oldestMessageAgeDatePattern = Pattern.compile("MSGAGE\\((\\d*)\\)");
	private static final Pattern inputHandleCountPattern = Pattern.compile("IPPROCS\\((\\d*)\\)");
	private static final Pattern outputHandleCountPattern = Pattern.compile("OPPROCS\\((\\d*)\\)");
	private static final Pattern monqPattern = Pattern.compile("MONQ\\(([A-Z,a-z\\s]*)\\)");
	
	private static final String METRIC_GROUP_QUEUE = "WebSphere MQ Queue";

	private static final String METRIC_QUEUE_CURRENT_DEPTH = "Queue Depth";
	private static final String METRIC_QUEUE_LAST_GET_DURATION = "Duration Since Last Read";
	private static final String METRIC_QUEUE_LAST_PUT_DURATION = "Duration Since Last Insert";
	private static final String METRIC_QUEUE_OLDEST_MESSAGE_AGE = "Oldest Message Age";
	private static final String METRIC_QUEUE_INPUT_HANDLE_COUNT = "Input Handle Count";
	private static final String METRIC_QUEUE_OUTPUT_HANDLE_COUNT = "Output Handle Count";
	private String queueName;
	private int position;
	
	public QueueMonitor(String name, int position) {
		queueName = name;
		this.position = position;
	}
	
	public String getName() {
		return queueName;
	}
	@Override
	protected String getMQSCCommandPattern() {
		return String.format(COMMAND_QUEUE, queueName);
//		return COMMAND_QUEUE;
	}

	@Override
	public Status executeMonitor(MonitorEnvironment env, String commandOutput) {
		Status status = null;

		if(getMONQStatus(commandOutput) == null) {
			String message = "MONQ is not initialized properly. Please run 'ALTER QMGR MONQ(LOW)'";
			log.severe(message);
			status = new Status(Status.StatusCode.ErrorTargetService, message);
		}
		
		log.severe("CommandOutput=\n" + commandOutput);
		String currentDepthStr = getCurrentDepth(commandOutput);

		if (currentDepthStr != null) {
			try {
				int currentDepthInt = Integer.parseInt(currentDepthStr);
				if (log.isLoggable(Level.FINE))
					log.fine("Current depth=" + currentDepthInt);

				Collection<MonitorMeasure> currentDepthMeasures = env.getMonitorMeasures(METRIC_GROUP_QUEUE,
						METRIC_QUEUE_CURRENT_DEPTH + position);
				for (MonitorMeasure mm : currentDepthMeasures) {
					mm.setValue(currentDepthInt);
				}
			} catch (Exception e) {
				String message = "Error while converting the current depth from String to int: "
						+ e.getMessage();
				log.severe(message);
				status = new Status(Status.StatusCode.PartialSuccess, message);
			}
		}

		String oldestMessageAgeStr = getOldestMessageAge(commandOutput);
		if (oldestMessageAgeStr != null) {
			try {
				int oldestMessageAgeInt = Integer.parseInt(oldestMessageAgeStr);
//				log.info("Oldest message age=" + oldestMessageAgeInt);

				Collection<MonitorMeasure> oldestMessageAgeMeasures = env.getMonitorMeasures(
						METRIC_GROUP_QUEUE, METRIC_QUEUE_OLDEST_MESSAGE_AGE + position);
				for (MonitorMeasure mm : oldestMessageAgeMeasures) {
					mm.setValue(oldestMessageAgeInt);
				}
			} catch (Exception e) {
				String message = "Error while converting the oldest message age from String to int: "
						+ e.getMessage();
				log.severe(message);
				status = new Status(Status.StatusCode.PartialSuccess, message);
			}
		}

		Calendar now = Calendar.getInstance();

		Date lastPutDate = getLastPutDate(commandOutput);
//		log.info("Last PUT date=" + lastPutDate);

		if (lastPutDate != null) {
			long lastPutDuration = getDurationInSeconds(now, lastPutDate);
			Collection<MonitorMeasure> lastPutMeasures = env.getMonitorMeasures(METRIC_GROUP_QUEUE,
					METRIC_QUEUE_LAST_PUT_DURATION + position);
			for (MonitorMeasure mm : lastPutMeasures) {
				mm.setValue(lastPutDuration);
			}
		}

		Date lastGetDate = getLastGetDate(commandOutput);
//		log.info("Last GET date=" + lastGetDate);

		if (lastGetDate != null) {
			long lastGetDuration = getDurationInSeconds(now, lastGetDate);
			Collection<MonitorMeasure> lastGetMeasures = env.getMonitorMeasures(METRIC_GROUP_QUEUE,
					METRIC_QUEUE_LAST_GET_DURATION + position);
			for (MonitorMeasure mm : lastGetMeasures) {
				mm.setValue(lastGetDuration);
			}
		}
		
		String inputHandleCountStr = getInputHandleCount(commandOutput);
		if(inputHandleCountStr != null) {
			try {
				int inputHandleCountInt = Integer.parseInt(inputHandleCountStr);
				if(log.isLoggable(Level.FINE)) {
					log.fine("Input handle count=" + inputHandleCountInt);
				}

				Collection<MonitorMeasure> currentDepthMeasures = env.getMonitorMeasures(METRIC_GROUP_QUEUE,
						METRIC_QUEUE_INPUT_HANDLE_COUNT + position);
				for (MonitorMeasure mm : currentDepthMeasures) {
					mm.setValue(inputHandleCountInt);
				}
			} catch (Exception e) {
				String message = "Error while converting input handle count from String to int: "
						+ e.getMessage();
				log.severe(message);
				status = new Status(Status.StatusCode.PartialSuccess, message);
			}
		}
		
		String outputHandleCountStr = getOutputHandleCount(commandOutput);
		if(outputHandleCountStr != null) {
			try {
				int outputHandleCountInt = Integer.parseInt(outputHandleCountStr);
				if(log.isLoggable(Level.FINE)) {
					log.fine("Output handle count=" + outputHandleCountInt);
				}

				Collection<MonitorMeasure> currentDepthMeasures = env.getMonitorMeasures(METRIC_GROUP_QUEUE,
						METRIC_QUEUE_OUTPUT_HANDLE_COUNT + position);
				for (MonitorMeasure mm : currentDepthMeasures) {
					mm.setValue(outputHandleCountInt);
				}
			} catch (Exception e) {
				String message = "Error while converting output handle count from String to int: "
						+ e.getMessage();
				log.severe(message);
				status = new Status(Status.StatusCode.PartialSuccess, message);
			}
		}
		
		
		if(status == null) {
			status = new Status(StatusCode.Success);
		}
		return status; 
	}
	
	private String getMONQStatus(String commandOutput) {
		Matcher matcher = monqPattern.matcher(commandOutput);
		if (matcher.find()) {
			// Group zero always stands for the entire expression
			return matcher.group(1);
		} else {
			return null;
		}
	}

	private String getCurrentDepth(String commandOutput) {
		return getStringFromCommandOutput(commandOutput, currentDepthPattern);
	}

	private String getOldestMessageAge(String commandOutput) {
		return getStringFromCommandOutput(commandOutput, oldestMessageAgeDatePattern);
	}

	private Date getLastPutDate(String commandOutput) {
		return getDateFromCommandOutput(commandOutput, lastPutDatePattern, lastPutTimePattern);
	}

	private Date getLastGetDate(String commandOutput) {
		return getDateFromCommandOutput(commandOutput, lastGetDatePattern, lastGetTimePattern);
	}
	private String getInputHandleCount(String commandOutput) {
		return getStringFromCommandOutput(commandOutput, inputHandleCountPattern);
	}
	
	private String getOutputHandleCount(String commandOutput) {
		return getStringFromCommandOutput(commandOutput, outputHandleCountPattern);
	}

}
