package org.javarosa.core.api;

/**
 * IDisplay is a platform specific Screen object that
 * is capable of displaying different IViews
 * 
 * @author Clayton Sims
 *
 */
public interface IDisplay {
	public void setView(IView view);
	
	/**
	 * @return A link to the underyling platform specific display object.
	 * NOTE: This should rarely be used but is necessary for older platforms
	 * like j2me.
	 */
	public Object getDisplayObject();
}
