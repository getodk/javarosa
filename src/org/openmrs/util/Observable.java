package org.openmrs.util;

import java.util.Vector;

/**
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
