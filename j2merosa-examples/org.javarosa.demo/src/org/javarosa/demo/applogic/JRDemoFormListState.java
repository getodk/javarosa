package org.javarosa.demo.applogic;

import org.javarosa.core.api.State;
import org.javarosa.demo.activity.formlist.JRDemoFormListController;
import org.javarosa.demo.activity.formlist.JRDemoFormListTransitions;
import org.javarosa.services.properties.api.PropertyUpdateState;
import org.javarosa.user.api.AddUserState;
import org.javarosa.user.model.User;

public class JRDemoFormListState implements JRDemoFormListTransitions, State {

	public void start() {
		JRDemoFormListController ctrl = new JRDemoFormListController();
		ctrl.setTransitions(this);
		ctrl.start();	
	}

	public void formSelected(int formID) {
		new JRDemoFormEntryState(formID).start();
	}

	public void viewSaved() {
		//new JRDemoSavedFormListState().start();
	}

	public void back() {
		new JRDemoPatientSelectState().start();
	}
	
	public void settings() {
		new PropertyUpdateState () {
			public void done () {
				new JRDemoFormListState().start();
			}
		}.start();
	}

	public void addUser() {
		new AddUserState () {
			public void cancel() {
				new JRDemoFormListState().start();
			}

			public void userAdded(User newUser) {
				new JRDemoFormListState().start();
			}
		}.start();
	}
}
