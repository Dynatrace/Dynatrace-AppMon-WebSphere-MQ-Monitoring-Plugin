package com.dynatrace.plugins.mq;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Pattern;

import com.dynatrace.diagnostics.pdk.MonitorEnvironment;
import com.dynatrace.diagnostics.pdk.MonitorMeasure;
import com.dynatrace.diagnostics.pdk.Status;
import com.dynatrace.diagnostics.pdk.Status.StatusCode;

public class SubscriptionMonitor extends MQSCMonitor {
	private static final String COMMAND_SUBSCRIPTION = "DISPLAY SBSTATUS(\'%s\') ALL\nEND\n";

	private static final Pattern messageCountPattern = Pattern.compile("NUMMSGS\\((\\d*)\\)");
	private static final Pattern lastRestorationTimePattern = Pattern.compile("RESMTIME\\(([\\d\\:\\s]*)\\)");
	private static final Pattern lastRestorationDatePattern = Pattern.compile("RESMDATE\\(([\\d\\-\\s]*)\\)");
	private static final Pattern lastMessageTimePattern = Pattern.compile("LMSGTIME\\(([\\d\\:\\s]*)\\)");
	private static final Pattern lastMessageDatePattern = Pattern.compile("LMSGDATE\\(([\\d\\-\\s]*)\\)");

	private static final String METRIC_GROUP_SUBSCRIPTION = "WebSphere MQ Subscription";

	private static final String METRIC_SUBSCRIPTION_MESSAGE_COUNT = "Message Count";
	private static final String METRIC_SUBSCRIPTION_LAST_RESTORATION_DURATION = "Duration Since Last Restoration";
	private static final String METRIC_SUBSCRIPTION_LAST_MESSAGE_DURATION = "Duration Since Last Message";
	private String subscriptionName;
	private int position;
	
	public SubscriptionMonitor(String name, int position) {
		subscriptionName = name;
		this.position = position;
	}
	
	@Override
	protected String getMQSCCommandPattern() {
		return COMMAND_SUBSCRIPTION;
	}

	public String getName() {
		return subscriptionName;
	}
	
	@Override
	public Status executeMonitor(MonitorEnvironment env, String commandOutput) {
		Status status = null;

		String messageCountStr = getMessageCount(commandOutput);

		if (messageCountStr != null) {
			try {
				int messageCountInt = Integer.parseInt(messageCountStr);
//				log.info("Message count=" + messageCountInt);

				Collection<MonitorMeasure> messageCountMeasures = env.getMonitorMeasures(METRIC_GROUP_SUBSCRIPTION,
						METRIC_SUBSCRIPTION_MESSAGE_COUNT + position);
				for (MonitorMeasure mm : messageCountMeasures) {
					mm.setValue(messageCountInt);
				}
			} catch (Exception e) {
				String message = "Error while converting the message count from String to int: "
						+ e.getMessage();
				log.severe(message);
				status = new Status(Status.StatusCode.PartialSuccess, message);
			}
		}

		Calendar now = Calendar.getInstance();

		Date lastRestorationDate = getLastRestorationDate(commandOutput);
//		log.info("Last restoration date=" + lastRestorationDate);

		if (lastRestorationDate != null) {
			long lastRestorationDuration = getDurationInSeconds(now, lastRestorationDate);
			Collection<MonitorMeasure> lastRestorationMeasures = env.getMonitorMeasures(METRIC_GROUP_SUBSCRIPTION,
					METRIC_SUBSCRIPTION_LAST_RESTORATION_DURATION + position);
			for (MonitorMeasure mm : lastRestorationMeasures) {
				mm.setValue(lastRestorationDuration);
			}
		}

		Date lastMessageDate = getLastMessageDate(commandOutput);
//		log.info("Last message date=" + lastMessageDate);

		if (lastMessageDate != null) {
			long lastMessageDuration = getDurationInSeconds(now, lastMessageDate);
			Collection<MonitorMeasure> lastGetMeasures = env.getMonitorMeasures(METRIC_GROUP_SUBSCRIPTION,
					METRIC_SUBSCRIPTION_LAST_MESSAGE_DURATION + position);
			for (MonitorMeasure mm : lastGetMeasures) {
				mm.setValue(lastMessageDuration);
			}
		}

		if (status == null) {
			status = new Status(StatusCode.Success);
		}
		return status;
	}

	private String getMessageCount(String commandOutput) {
		return getStringFromCommandOutput(commandOutput, messageCountPattern);
	}

	private Date getLastRestorationDate(String commandOutput) {
		return getDateFromCommandOutput(commandOutput, lastRestorationDatePattern, lastRestorationTimePattern);
	}

	private Date getLastMessageDate(String commandOutput) {
		return getDateFromCommandOutput(commandOutput, lastMessageDatePattern, lastMessageTimePattern);
	}
}
