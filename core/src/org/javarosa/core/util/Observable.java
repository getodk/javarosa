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

package org.javarosa.core.util;

import java.util.Vector;

/**
 * Observable object interface
 * 
 * @author <a href="mailto:m.nuessler@gmail.com">Matthias Nuessler</a>
 */
public abstract class Observable {

	/**
	 * 
	 */
	private boolean changed = false;

	/**
	 * 
	 */
	private Vector observers = new Vector();

	/**
	 * 
	 */
	public void setChanged() {
		changed = true;
	}

	/**
	 * 
	 */
	public void clearChanged() {
		changed = false;
	}

	/**
	 * @return
	 */
	public boolean hasChanged() {
		return changed;
	}

	/**
	 * @param observer
	 */
	public void addObserver(Observer observer) {
		observers.addElement(observer);
	}

	/**
	 * @param observer
	 */
	public void deleteObserver(Observer observer) {
		observers.removeElement(observer);
	}

	/**
	 * 
	 */
	public void deleteObservers() {
		observers.removeAllElements();
	}

	/**
	 * @param arg
	 */
	public void notifyObservers(Object arg) {
		if (changed) {
			for (int i = 0; i < observers.size(); i++) {
				((Observer) observers.elementAt(i)).update(this, arg);
			}
			clearChanged();
		}
	}

}
