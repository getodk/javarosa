/**
 * 
 */
package org.javarosa.j2me.log;

import org.kxml2.kdom.Element;

/**
 * @author ctsims
 *
 */
public interface XmlStatusProvider {
	public Element getStatusReport() throws StatusReportException;
}
