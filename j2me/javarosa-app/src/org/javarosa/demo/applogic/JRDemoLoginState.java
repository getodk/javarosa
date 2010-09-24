package org.javarosa.demo.applogic;

import org.javarosa.demo.util.JRDemoUtil;
import org.javarosa.user.api.LoginState;
import org.javarosa.user.model.User;

public class JRDemoLoginState extends LoginState {

	public void start() {
		JRDemoContext._().setUser(null);
		super.start();
	}

	public void exit() {
		JRDemoUtil.exit();
	}

	public void loggedIn(User u) {
		JRDemoContext._().setUser(u);
		new JRDemoFormListState().start();
	}

}
