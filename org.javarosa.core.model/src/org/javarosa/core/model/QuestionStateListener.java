package org.javarosa.core.model;

public interface QuestionStateListener {
	//Question: Is there a reason we're
	//using individual bytes here? Do we
	//want to be able to set multiple flags
	//at once?
	static final int CHANGE_INIT = 0x00;
	static final int CHANGE_DATA = 0x01;
	static final int CHANGE_LOCALE = 0x02;
	static final int CHANGE_ENABLED = 0x04;
	static final int CHANGE_VISIBLE = 0x08;
	static final int CHANGE_OTHER = 0x10;

	void questionStateChanged (QuestionDef question, int changeFlags);
}