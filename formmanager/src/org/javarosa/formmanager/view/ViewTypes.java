package org.javarosa.formmanager.view;

/**
 * This static class just has a list of unique ids for all
 * available views.
 * @author Brian DeRenzi
 *
 */
public class ViewTypes {
	public static final int LOGIN_SCREEN = 1;
	public static final int FORM_LIST = 2;
	public static final int CONTROLLER = 3;

	/**
	 *  Prohibit anyone trying to instantiate this class.
	 */
	protected ViewTypes() {}
}
