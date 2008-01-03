package org.celllife.clforms.api;

public class Constants {

	//return types
	public static final int RETURN_INTEGER = 1;
	public static final int RETURN_STRING = 2;
	public static final int RETURN_DATE = 3;
	public static final int RETURN_SELECT1 = 4;
	public static final int RETURN_SELECT_MULTI = 5;
	public static final int RETURN_BOOLEAN = 6;
	
	//form control types
	public static final int INPUT = 1;
	public static final int SELECT1 = 2;
	public static final int SELECT = 3;
	public static final int TEXTAREA = 4;
	public static final int SECRET = 5;
	public static final int OUTPUT = 6;
	public static final int UPLOAD = 4;
	public static final int RANGE = 5;
	public static final int SUBMIT = 6;
	
	// Dimagi form control types
	public static final int TEXTBOX = 10;
	
	// does the file connection API (JSR 75) exist?
	public static boolean FILE_CONNECTION = false;
	public static int MODE;

}
