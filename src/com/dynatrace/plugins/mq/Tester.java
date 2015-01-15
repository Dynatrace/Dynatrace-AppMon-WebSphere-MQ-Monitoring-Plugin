package com.dynatrace.plugins.mq;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;

import com.dynatrace.diagnostics.pdk.Status;
import com.dynatrace.diagnostics.pdk.Status.StatusCode;

public class Tester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String mqBinPath = "C:\\dev\\WebSphereMQ\\bin\\";
		String queueMgrName = "QM_AHELAILI";

		// Executing the MQSC command
		StringBuilder shellCommandSB = new StringBuilder(mqBinPath);
		shellCommandSB.append("runmqsc ");
		shellCommandSB.append(queueMgrName);
		String shellCommand = shellCommandSB.toString();
		String mqscCommand = "DISPLAY QSTATUS(\'postcard\') TYPE(QUEUE) ALL\nEND\n";
		Process process;

		BufferedInputStream is;

		BufferedOutputStream os;

		try {
			
			process = Runtime.getRuntime().exec(shellCommand);
			InputStream inputStream = process.getInputStream();

			is = new BufferedInputStream(inputStream);
			os = new BufferedOutputStream(process.getOutputStream(), 256);
			
			os.write(mqscCommand.getBytes());
			os.flush();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			readUntilEnd(is, baos);
			String commandOutput = baos.toString();
			System.out.println(commandOutput);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	private static boolean readUntilEnd(BufferedInputStream is, OutputStream baos) throws IOException {
		boolean emptyline = true;

		for (int i = is.read();; i = is.read()) {
			if (i == -1) {
				return false;
			}
			if (i == ':' && emptyline) {
				break; // started successfully
			} else if (i == '\n') {
				emptyline = true;
			} else {
				baos.write(i);
				if (i != ' ')
					emptyline = false;
			}

		}
		return true;
	}

}
