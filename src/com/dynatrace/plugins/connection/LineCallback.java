package com.dynatrace.plugins.connection;
/**
 * Called for each read line from a persistent connection
 *
 * @author michael.kumar
 *         Date: 15.06.2010
 */
public interface LineCallback {
    void lineRead(String line);
}
