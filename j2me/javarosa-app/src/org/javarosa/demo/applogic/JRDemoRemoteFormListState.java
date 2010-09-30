package org.javarosa.demo.applogic;

import org.javarosa.core.api.State;
import org.javarosa.demo.activity.remoteformlist.JRDemoRemoteFormListController;
import org.javarosa.demo.activity.remoteformlist.JRDemoRemoteFormListTransitions;

public class JRDemoRemoteFormListState implements JRDemoRemoteFormListTransitions, State {

	private String formList = null;
	
	public JRDemoRemoteFormListState()
	{
		
	}

	public JRDemoRemoteFormListState(String formList)
	{
		this.formList= formList;
	}

	public void start() {
		JRDemoRemoteFormListController ctrl = new JRDemoRemoteFormListController(this.formList);
	
		ctrl.setTransitions(this);
		ctrl.start();	
	}

	public void back() {
		new JRDemoFormListState().start();
	}

	public void formDownload(String formUrl) {
		new JRGetFormHTTPState(formUrl).start();
	}

}
