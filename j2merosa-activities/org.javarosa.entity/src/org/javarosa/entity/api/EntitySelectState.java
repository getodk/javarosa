package org.javarosa.entity.api;

import org.javarosa.core.api.State;
import org.javarosa.core.services.storage.Persistable;
import org.javarosa.entity.api.transitions.EntitySelectTransitions;

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

public abstract class EntitySelectState <E extends Persistable> implements EntitySelectTransitions, State {
	
	public void start () {
		EntitySelectController<E> controller = getController();
		controller.setTransitions(this);
		controller.start();
	}
	
	protected abstract EntitySelectController<E> getController ();
	
}