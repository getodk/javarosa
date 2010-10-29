/**
 * 
 */
package org.javarosa.j2me.log;

import java.io.IOException;

import org.xmlpull.v1.XmlSerializer;

/**
 * @author ctsims
 *
 */
public interface XmlStatusProvider {
	public void getStatusReport(XmlSerializer o, String namespace) throws StatusReportException, IOException;
}
