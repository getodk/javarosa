/**
 * 
 */
package org.javarosa.log.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import org.javarosa.core.log.StreamLogSerializer;
import org.javarosa.core.log.LogEntry;

/**
 * @author ctsims
 *
 */
public class LogWriter extends StreamLogSerializer {
	OutputStreamWriter writer;
	
	public LogWriter(OutputStream stream) {
		try {
			writer = new OutputStreamWriter(stream, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			writer = new OutputStreamWriter(stream);
		}
	}
	
	private String logToString(LogEntry log) {
		return "[" + log.getType() + "] " +log.getTime().toString() + ": " +  log.getMessage()+ "\n"; 
	}
	
	protected void serializeLog(LogEntry entry) throws IOException {
		writer.write(logToString(entry));
	}

}
