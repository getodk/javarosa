/**
 * 
 */
package org.javarosa.log.view;

import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;

import org.javarosa.core.api.IView;

/**
 * @author Clayton Sims
 * @date Apr 13, 2009 
 *
 */
public class LogViewer extends Form implements IView {

	StringItem logs;
	
	public LogViewer() {
		super("Incident Log");
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IView#getScreenObject()
	 */
	public Object getScreenObject() {
		return this;
	}
	
	public void loadLogs(String data) {
		logs = new StringItem("", data);
		this.append(logs);
	}
}
