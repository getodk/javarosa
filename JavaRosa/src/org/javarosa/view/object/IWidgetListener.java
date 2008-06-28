package org.javarosa.view.object;

/**
 * The Widget interface is implemented to allow
 * the receipt of various Widget events.
 * 
 * @author ctsims
 * @date Aug-09-2007
 *
 */
public interface IWidgetListener {
	/**
	 * Called when a widget has completed accepting input.
	 */
	public void onWidgetComplete();
}
