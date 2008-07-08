package org.javarosa.formmanager.utility;

import org.javarosa.formmanager.model.temp.*;

public interface QuestionStateListener {
	static final int CHANGE_INIT = 0x00;
	static final int CHANGE_DATA = 0x01;
	static final int CHANGE_LOCALE = 0x02;
	static final int CHANGE_RELEVANCY = 0x04;
	static final int CHANGE_ENABLED = 0x08;
	static final int CHANGE_VISIBLE = 0x10;
	static final int CHANGE_OTHER = 0x20;

	void questionStateChanged (Prompt question, int changeFlags);
}