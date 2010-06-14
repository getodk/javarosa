/**
 * 
 */
package org.javarosa.j2me.log;

/**
 * @author ctsims
 *
 */
public class StatusReportException extends Exception {
	private Exception parent;
	private String reportName;
	private String message;
	
	public StatusReportException(Exception parent, String reportName, String message) {
		this.parent = parent;
		this.reportName = reportName;
		this.message = message;
	}

	public Exception getParent() {
		return parent;
	}

	public String getReportName() {
		return reportName;
	}

	public String getMessage() {
		return message;
	}
}
