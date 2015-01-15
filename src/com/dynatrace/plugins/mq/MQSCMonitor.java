package com.dynatrace.plugins.mq;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dynatrace.diagnostics.pdk.MonitorEnvironment;
import com.dynatrace.diagnostics.pdk.Status;
import com.dynatrace.diagnostics.pdk.Status.StatusCode;

import com.dynatrace.plugins.connection.*;
/**
 * @author alain.helaili
 * 
 *         Superclass of the MQ object monitors that leverage the MQSC shell
 *         command
 * 
 */

public abstract class MQSCMonitor {
	protected static final Logger log = Logger.getLogger(MQMonitorPlugin.class
			.getName());

	private String mqscCommand = null;
	private String queueManagerName;
	protected abstract String getMQSCCommandPattern();

//	@Override
	public Status setup(MonitorEnvironment env) throws Exception {
//		String objectName = env
//				.getConfigString(MQMonitorPlugin.PARAM_OBJECT_NAME);
//		mqscCommand = String.format(getMQSCCommandPattern(), objectName);
		mqscCommand = getMQSCCommandPattern();

//		return startProcess(env);
		return new Status(Status.StatusCode.Success);
	}

	public void setQueueManagerName(String name) {
		queueManagerName = name;
	}
	
	private String startProcess(MonitorEnvironment env, ConnectionMethod connMethod) {
		String mqBinPath = env
				.getConfigString(MQMonitorPlugin.PARAM_MQ_BIN_PATH);

		// Executing the MQSC command
		StringBuilder shellCommandSB = new StringBuilder(mqBinPath);
		if (!mqBinPath.endsWith("/") && !mqBinPath.endsWith("\\")) {
			shellCommandSB.append("/");
		}
		shellCommandSB.append("runmqsc ");
		shellCommandSB.append(queueManagerName);
		shellCommandSB.append(" << ENDOFLINE \n");
		shellCommandSB.append(mqscCommand + " ENDOFLINE");
		String shellCommand = shellCommandSB.toString();

		try {
			return connMethod.executeCommand(shellCommand, "");
		}
		catch (Exception e) {
			teardown(null);
			String message = "Could not start process with shell command : '"
					+ shellCommand;
			log.severe(message);
			Status status = new Status(
					StatusCode.ErrorTargetServiceExecutionFailed, message);
			status.setException(e);
			return null;
		}
//		log.info("Shell command : " + shellCommand);
//		log.info("MQSC command : " + mqscCommand);
		
//		if (log.isLoggable(Level.FINE)) {
//			log.fine("Shell command : " + shellCommand);
//			log.fine("MQSC command : " + mqscCommand);
//		}
		
//		try {
//			process = Runtime.getRuntime().exec(shellCommand);
//			InputStream inputStream = process.getInputStream();
			//InputStream inputStream = process.getErrorStream();

/*			os = new BufferedOutputStream(process.getOutputStream(), 256);
			
			os.write(mqscCommand.getBytes());
			os.flush();

			is = new BufferedInputStream(inputStream);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			readUntilEnd(baos, is);
			String commandOutput = baos.toString();
*/			
//			log.info("*** Command output ****");
//			log.info(commandOutput);
//			log.info("***********************");
/*			
			if (log.isLoggable(Level.FINE)) {
				log.fine("*** Command output ****");
				log.fine(commandOutput);
				log.fine("***********************");
			}
			
			return commandOutput;
			
		
		} catch (IOException e) {
			teardown(null);
			String message = "Could not start process with shell command : '"
					+ shellCommand;
			log.severe(message);
			Status status = new Status(
					StatusCode.ErrorTargetServiceExecutionFailed, message);
			status.setException(e);
			return null;
		} finally {
			if (process != null)
				process.destroy();
			process = null;
			is = null;
			os = null;
		}	
*/	}

//	@Override
	public void teardown(MonitorEnvironment arg0) {
		
	}

//	@Override
	public Status execute(MonitorEnvironment env, ConnectionMethod connMethod) {	
		String commandOutput = startProcess(env, connMethod);
		log.severe("CommandOutput=" + commandOutput);
		if (commandOutput == null) {
			return new Status(StatusCode.ErrorTargetService); 
		} else {		
			//Call the parsing method in the actual plugin
			return executeMonitor(env, commandOutput);
		}
	}

	public abstract Status executeMonitor(MonitorEnvironment env,
			String commandOutput);

	public abstract String getName();
	
	protected Date getDateFromCommandOutput(String commandOutput,
			Pattern datePattern, Pattern timePattern) {
		Matcher timeMatcher = timePattern.matcher(commandOutput);
		Matcher dateMatcher = datePattern.matcher(commandOutput);
		String timeStr = null;
		String dateStr = null;

		if (timeMatcher.find()) {
			// Group zero always stands for the entire expression
			timeStr = timeMatcher.group(1);
			// Sometimes the hour format is hh:mm:ss
			timeStr = timeStr.replace(":", ".");
		} else {
			return null;
		}

		if (dateMatcher.find()) {
			// Group zero always stands for the entire expression
			dateStr = dateMatcher.group(1);
		} else {
			return null;
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh.mm.ss");
		String longdateStr = (new StringBuilder(dateStr)).append(" ").append(
				timeStr).toString();
		try {
			return sdf.parse(longdateStr);
		} catch (ParseException e) {
			log.severe("Failed parsing date '" + longdateStr
					+ "' with message: " + e.getMessage());
			return null;
		}
	}

	protected String getStringFromCommandOutput(String commandOutput,
			Pattern pattern) {
		Matcher matcher = pattern.matcher(commandOutput);
		if (matcher.find()) {
			// Group zero always stands for the entire expression
			return matcher.group(1);
		} else {
			return null;
		}
	}

	protected long getDurationInSeconds(Calendar now, Date then) {
		Calendar thenCal = Calendar.getInstance();
		thenCal.setTime(then);
		return (now.getTimeInMillis() - thenCal.getTimeInMillis()) / 1000;
	}

}
