package org.javarosa.demo.applogic;

import org.javarosa.demo.activity.savedformlist.JRDemoSavedFormListController;
import org.javarosa.demo.activity.savedformlist.JRDemoSavedFormListTransitions;

public class JRDemoSavedFormListState implements JRDemoSavedFormListTransitions{
	public void start() {
		JRDemoSavedFormListController ctrl = new JRDemoSavedFormListController();
		ctrl.setTransitions(this);
		ctrl.start();
	}

	public void back() {
		new JRDemoFormListState().start();
	}

	public void savedFormSelected(int intValue) {
		// TODO show saved form
	}
}
