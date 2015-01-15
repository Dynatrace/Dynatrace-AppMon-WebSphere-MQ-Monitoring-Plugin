package com.dynatrace.plugins.connection;

public class LocalConnectionMethod extends ConnectionMethod {
	//private static final Logger log = Logger.getLogger(LocalConnectionMethod.class.getName());

    @Override
    public Object openSession(String env) throws Exception {
        return null;
    }

    @Override
    public void closeSession(Object session) throws Exception {
        //nothing to do
    }

    @Override
    public void executePersistentCommand(String cmd, String env, LineCallback callback, Object session) throws Exception {
        Process child = Runtime.getRuntime().exec(
                cmd,
                (env.equals("")) ? new String[] { "LANG=C" }
                        : new String[] { "LANG=C", env });
        readPersistentInputStream(child.getInputStream(), callback);
    }

    @Override
	public String executeStringCommand(String command, String env) throws Exception {
		Process child = Runtime.getRuntime().exec(
				command,
				(env.equals("")) ? new String[] { "LANG=C" }
						: new String[] { "LANG=C", env });
		child.waitFor();
		String output = readInputStream(child.getInputStream());
		readInputStream(child.getErrorStream());
		return output;
	}
}
