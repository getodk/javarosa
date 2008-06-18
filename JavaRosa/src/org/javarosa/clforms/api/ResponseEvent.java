package org.javarosa.clforms.api;

public class ResponseEvent {

	public static final int NEXT = 1;
	public static final int PREVIOUS = 2;
	public static final int GOTO = 3;
	public static final int SAVE = 4;
	public static final int SAVE_AND_RELOAD = 5;
	public static final int EXIT = 6; // TODO is this the same as Cancel?
	
	// more possibles
	
	public static final int SAVE_AND_EXIT = 7;
	public static final int LIST = 8;
	

	private int parameter;
	private int type;

	public ResponseEvent(int type, int param) {
		this.type = type;
		this.parameter = param;
	}

	public int getParameter() {
		return parameter;
	}

	public void setParameter(int parameter) {
		this.parameter = parameter;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

}
