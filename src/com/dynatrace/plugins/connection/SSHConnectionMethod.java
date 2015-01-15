package com.dynatrace.plugins.connection;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.trilead.ssh2.Connection;
import com.trilead.ssh2.Session;
import com.trilead.ssh2.StreamGobbler;

public class SSHConnectionMethod extends ConnectionMethod {
	private static final Logger log = Logger.getLogger(SSHConnectionMethod.class.getName());

	private Connection conn;
	private Session session;

	private String host, user, pass;
	private int port;

	private String keyFile;

    @Override
    public Object openSession(String env) throws Exception {
        return conn.openSession();
    }

    @Override
    public void closeSession(Object session) throws Exception {
        this.session.close();
        if (session instanceof Session) {
            ((Session)session).close();
        }
    }

    @Override
	public void executePersistentCommand(String cmd, String env, LineCallback callback, Object session) throws Exception {
    	Session s = ((Session)session);
        s.execCommand(((env.isEmpty()) ? "" : (env + " ")) + "LANG=C " + cmd);
        readPersistentInputStream(new StreamGobbler(s.getStdout()), callback);
    }


    @Override
	public String executeStringCommand(String cmd, String env) throws Exception {
		String output = "";

		try {
			session = conn.openSession();
			session.execCommand(((env.isEmpty()) ? "" : (env + " "))
				+ "LANG=C " + cmd);
			output = readInputStream(new StreamGobbler(session.getStdout()));
		} finally {
			session.close();
		}
		if (output.equals(""))
			throw new Exception("Command \"" + cmd + "\" did not produce any output");

		return output;
	}

	@Override
	public void reconnectIfNecessary() throws IOException {
		try {
			Session testSession = conn.openSession();
			testSession.close();
		} catch (Exception e) {
			log.info("Connection seems to be down, trying to reconnect..");
			connect();
		}
	}

	private void connect() throws IOException {
		boolean isAuthenticated;
		try {
			if (conn != null) {
				conn.close();
			}
			conn = new Connection(host, port);
			conn.connect();

			if (keyFile != null) {
				if(log.isLoggable(Level.INFO)){
					log.info("SSH Publickey authentication");
				}
				boolean available = conn.isAuthMethodAvailable(user, "publickey");
				if (!available) {
					throw new IOException("Authentication-Method publickey not available");
				}
				File pemFile = new File(keyFile);
				isAuthenticated = conn.authenticateWithPublicKey(user, pemFile, pass);
			} else {
				boolean available = conn.isAuthMethodAvailable(user, "password");
				if (!available) {
					throw new IOException("Authentication-Method password is not available");
				}
				isAuthenticated = conn.authenticateWithPassword(user, pass);
			}
			if (!isAuthenticated) {
				throw new IOException("Authentication failed.");
			}
		} catch (IOException ex) {
			if (conn != null) {
				conn.close();
			}
			throw ex;
		}
	}

	@Override
	public void setup(String host, String user, String pass, int port) throws Exception {
		setup(host, user, pass, port, null);
	}

	public void setup(String host, String user, String pass, int port, String keyFile) throws Exception {
		this.host = host;
		this.user = user;
		this.pass = pass;
		this.port = port;
		this.keyFile = keyFile;
		connect();
	}

	@Override
	public void teardown() {
		conn.close();
	}
}
