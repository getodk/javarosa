package org.javarosa.demo.applogic;

import org.javarosa.demo.util.GetFormHttpStateGet;
import org.javarosa.formmanager.api.GetFormHttpState;

public class JRGetFormHTTPState extends GetFormHttpStateGet {

	private String formUrl;
	
	public JRGetFormHTTPState(String formUrl){
		this.formUrl = formUrl;
	}
	
	public String getURL() {
		return this.formUrl;
	}

	public void done() {
		new JRDemoFormListState().start();
	}

}
