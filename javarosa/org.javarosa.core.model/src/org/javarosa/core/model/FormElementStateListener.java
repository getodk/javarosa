package org.javarosa.core.model;

import org.javarosa.core.model.instance.TreeElement;

/**
 * @author Drew Roos?
 *
 */
public interface FormElementStateListener {
	static final int CHANGE_INIT = 0x00;
	static final int CHANGE_DATA = 0x01;
	static final int CHANGE_LOCALE = 0x02;
	static final int CHANGE_ENABLED = 0x04;
	static final int CHANGE_RELEVANT = 0x08;
	static final int CHANGE_REQUIRED = 0x10;
//	static final int CHANGE_LOCKED = 0x20;
	static final int CHANGE_OTHER = 0x40;

	void formElementStateChanged (IFormElement question, int changeFlags);
	
	void formElementStateChanged (TreeElement question, int changeFlags);
}