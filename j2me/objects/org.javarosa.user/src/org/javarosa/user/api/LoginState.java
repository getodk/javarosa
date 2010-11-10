package org.javarosa.user.api;

import org.javarosa.core.api.State;
import org.javarosa.user.api.transitions.LoginTransitions;

/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

public abstract class LoginState implements LoginTransitions, State {
	
	public void start () {
		LoginController controller = getController();
		controller.setTransitions(this);
		controller.start();
	}
	
	protected LoginController getController () {
		return new LoginController();
	}
	
}
