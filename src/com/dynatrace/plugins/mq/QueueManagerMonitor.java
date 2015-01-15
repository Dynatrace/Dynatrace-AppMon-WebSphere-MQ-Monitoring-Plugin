package com.dynatrace.plugins.mq;

//import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import com.dynatrace.diagnostics.pdk.Monitor;
import com.dynatrace.diagnostics.pdk.MonitorEnvironment;
import com.dynatrace.diagnostics.pdk.MonitorMeasure;
import com.dynatrace.diagnostics.pdk.Status;
import com.dynatrace.diagnostics.pdk.Status.StatusCode;
import com.dynatrace.plugins.connection.*;
public class QueueManagerMonitor  {
	private static final Logger log = Logger.getLogger(MQMonitorPlugin.class
			.getName());

	private MQSCMonitor[] mqMonitors = null;
	public static final String PARAM_OBJECT_NAME = "object.name";
	public static final String PARAM_OBJECT_TYPE = "object.type";
	private static final String OBJECT_TYPE_QUEUE = "Queue";
	private static final String OBJECT_TYPE_TOPIC = "Topic";
	private static final String OBJECT_TYPE_SUBSCRIPTION = "Subscription";
	private static String PARAM_OBJECT_COUNT = "object.count";
	private String objectType = null;

	private static final String METRIC_GROUP_QMANAGER = "WebSphere MQ Queue Manager";
	private static final String METRIC_QMANAGER_STATUS = "Status";
	private static final String METRIC_QMANAGER_STATUS_RUNNING = "Running State";
	private static final String METRIC_QMANAGER_STATUS_STOPPED = "Stopped";
	private static final Pattern statusPattern = Pattern
			.compile("STATUS\\(([A-Z,a-z\\s]*)\\)");

	private String queueMgrName = null;
	private String shellCommand = null;

	private QueueManagerStatus status;

	public Status execute(MonitorEnvironment env, ConnectionMethod connMethod) throws Exception {
		final String commandOutput;
		//final Process process;

		try {
			commandOutput = connMethod.executeCommand(shellCommand, "");
		} catch (IOException e) {
			String message = "Could not start process with shell command : '"
					+ shellCommand;
			log.severe(message);
			Status status = new Status(
					StatusCode.ErrorTargetServiceExecutionFailed, message);
			status.setException(e);
			return status;

		}
/*		try {
			byte[] byteBuffer = new byte[1024];
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int len = 0;

			while ((len = process.getInputStream().read(byteBuffer)) != -1) {
				baos.write(byteBuffer, 0, len);
			}

			commandOutput = baos.toString();
		} catch (IOException e) {
			log.severe(e.getMessage());
			return new Status(
					Status.StatusCode.ErrorTargetServiceExecutionFailed, e
							.getMessage());
		} finally {
			process.destroy();
		}
*/
		return executeMonitor(env, commandOutput, connMethod);
	}

//	@Override
	public Status setup(MonitorEnvironment env) throws Exception {
		objectType = env.getConfigString(PARAM_OBJECT_TYPE);
		String mqBinPath = env
				.getConfigString(MQMonitorPlugin.PARAM_MQ_BIN_PATH);

		StringBuilder shellCommandSB = new StringBuilder(mqBinPath);
		if (!mqBinPath.endsWith("/") && !mqBinPath.endsWith("\\")) {
			shellCommandSB.append("/");
		}
		if (env.getConfigBoolean("forceEnglish"))
			shellCommandSB.append("dspmq -n "); 
		else
			shellCommandSB.append("dspmq "); 
//		shellCommandSB.append(queueMgrName);
		shellCommand = shellCommandSB.toString();

		if (log.isLoggable(Level.FINE)) {
			log.fine("Shell command : " + shellCommand);
		}
		int objectCount = env.getConfigLong(PARAM_OBJECT_COUNT).intValue();
		mqMonitors = new MQSCMonitor[objectCount]; 
		if (objectType.equalsIgnoreCase(OBJECT_TYPE_QUEUE)) {
			for (int j=0; j<objectCount; j++) {
				String param_objectName = env.getConfigString(PARAM_OBJECT_NAME + (j + 1));
				mqMonitors[j] = new QueueMonitor(param_objectName, j+1);
				mqMonitors[j].setup(env);
			}
		} else if (objectType.equalsIgnoreCase(OBJECT_TYPE_TOPIC)) {
			for (int j=0; j<objectCount; j++) {
				String param_objectName = env.getConfigString(PARAM_OBJECT_NAME + (j + 1));
				mqMonitors[j] = new TopicMonitor(param_objectName,j);				
				mqMonitors[j].setup(env);
			}
		} else if (objectType.equalsIgnoreCase(OBJECT_TYPE_SUBSCRIPTION)) {
			for (int j=0; j<objectCount; j++) {
				String param_objectName = env.getConfigString(PARAM_OBJECT_NAME + (j + 1));
				mqMonitors[j] = new SubscriptionMonitor(param_objectName,j);				
				mqMonitors[j].setup(env);
			}
		} else {
			return new Status(Status.StatusCode.ErrorInternalConfigurationProblem, "Unknown MQ object type");
		}

		
		return new Status(Status.StatusCode.Success);
	}
	private Status executeMonitor(MonitorEnvironment env, String commandOutput, ConnectionMethod connMethod) {

		int numberOfQueueMgr = env.getConfigLong(MQMonitorPlugin.PARAM_QUEUEMANAGER_COUNT).intValue();
		
		for (int i=1; i<=numberOfQueueMgr; i++) {
			String paramName_QMGR = MQMonitorPlugin.PARAM_QUEUEMANAGER_NAME + i;
//			log.info("Param_QMGR=" + paramName_QMGR);
			queueMgrName = env.getConfigString(paramName_QMGR);			

			String qmStatus = getStatus(commandOutput, queueMgrName);
	
			if (qmStatus != null) {
	
				// Java enums can't have white space in their values.
				// Replacing them in the MQ status string with '_'.
				qmStatus = qmStatus.replace(' ', '_');
				int statusInt;
				try {
					status = QueueManagerStatus.valueOf(qmStatus.toUpperCase());
					statusInt = getStatus().ordinal();
				} catch (IllegalArgumentException e) {
					log.severe("Unknown Status Code for queue manager '"
							+ queueMgrName + "': " + qmStatus);
					return new Status(
							Status.StatusCode.ErrorTargetServiceExecutionFailed,
							"Unknown Status Code for queue manager '"
									+ queueMgrName + "': " + qmStatus);
	
				}
	
				String paramName_Status = METRIC_QMANAGER_STATUS + i;
				String paramName_Running = METRIC_QMANAGER_STATUS_RUNNING + i;
				String paramName_Stopped = METRIC_QMANAGER_STATUS_STOPPED + i;
				setValue(env, statusInt,paramName_Status);
				switch (getStatus())
				{
				case ENDED_IMMEDIATELY:
				case ENDED_NORMALLY:
				case ENDED_PREEMPTIVELY:
				case ENDED_UNEXPECTEDLY:
					setValue(env, 0,paramName_Status);
					setValue(env, 0,paramName_Running);
					break;
				case ENDING_IMMEDIATELY:
				case ENDING_PREEMPTIVELY:
				case QUIESCING:
				case RUNNING_ELSEWHERE:
				case STARTING:
					setValue(env, 1,paramName_Stopped);
					setValue(env, 0,paramName_Running);
					break;
				case RUNNING:
					setValue(env, 0,paramName_Stopped);
					setValue(env, 2,paramName_Running);
					break;
	            case RUNNING_AS_STANDBY:
					setValue(env, 0,paramName_Stopped);
					setValue(env, 1,paramName_Running);
					break;
				
				}

				if (qmStatus.equalsIgnoreCase("Running")) {
						for (int j=0; j<mqMonitors.length; j++) {
							MQSCMonitor theMonitor = mqMonitors[j];
							try {
								theMonitor.setQueueManagerName(queueMgrName);
								theMonitor.execute(env, connMethod);
							}
							catch (Exception e) {
								String message = "Could not retrieve queue manager '"
									+ theMonitor.getName() + "' status.\n" + commandOutput;
								log.severe(message);
								return new Status(
										Status.StatusCode.ErrorTargetServiceExecutionFailed,
										message);						
							}
						}
				}
			} else {
				String message = "Could not retrieve queue manager '"
						+ queueMgrName + "' status.\n" + commandOutput;
				log.severe(message);
				return new Status(
						Status.StatusCode.ErrorTargetServiceExecutionFailed,
						message);
			}
		}
		

		return new Status(StatusCode.Success);

	}

	private void setValue(MonitorEnvironment env, int statusInt, String measureName) {
//		log.info("About to set value of " + measureName + " to " + statusInt);
		Collection<MonitorMeasure> qmStatusMeasures = env
		.getMonitorMeasures(METRIC_GROUP_QMANAGER,
				measureName);
		for (MonitorMeasure mm : qmStatusMeasures) {
//			log.info("Setting value of " + mm.toString() + " to " + statusInt);
			mm.setValue(statusInt);
		}
	}

	private String getStatus(String commandOutput, String queueMgrName) {
//		log.info("queueMgrName = " + queueMgrName);
		String[] outputString = commandOutput.split("\n");
		
		for (int j=0; j<outputString.length; j++ ) {
//			log.info("For row " + j + " String = " + outputString[j]);
			if (outputString[j].contains(queueMgrName)) {
				Matcher matcher = statusPattern.matcher(outputString[j]);
				if (matcher.find()) {
					// Group zero always stands for the entire expression
					return matcher.group(1);
				} else {
					return null;
				}
			}
		}
		return null;
	}

//	@Override
	public void teardown(MonitorEnvironment arg0) throws Exception {
		for (int j=0; j<mqMonitors.length; j++ ) {
			mqMonitors[j].teardown(arg0);
		}
	}

	public QueueManagerStatus getStatus() {
		return status;
	}

}
