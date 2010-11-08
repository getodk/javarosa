package org.javarosa.demo.applogic;

import org.javarosa.core.services.locale.Localization;
import org.javarosa.demo.util.ProgressScreenFormDownload;
import org.javarosa.formmanager.api.GetFormHttpState;
import org.javarosa.formmanager.view.ProgressScreen;

public class JRGetFormHTTPState extends GetFormHttpState {

	private String formUrl;
	
	public JRGetFormHTTPState(String formUrl){
		this.formUrl = formUrl;
	}
	
	public String getURL() {
		return this.formUrl;
	}

	protected ProgressScreen initProgressScreen() {
		return new ProgressScreenFormDownload(Localization.get("jrdemo.downloading"),Localization.get("jrdemo.fetching"), this);
	}
	
	public void done() {
		new JRDemoFormListState().start();
	}

}
