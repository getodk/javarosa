/**
 * 
 */
package org.javarosa.patient.entry.activity.util;

import javax.microedition.lcdui.Command;

import de.enough.polish.ui.Container;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.Style;

/**
 * @author Clayton Sims
 * @date Jan 21, 2009 
 *
 */
public class ClickableContainer extends Container {
	
	IClickEventListener listener;
	
	boolean armed = false;
	
	int id = -1;

	public ClickableContainer(boolean focusFirstElement) {
		super(focusFirstElement);
	}
	public ClickableContainer(boolean focusFirstElement, Style style) {
		super(focusFirstElement, style);
	}
	
	public void setClickEventListener(IClickEventListener listener) {
		this.listener = listener;
	}
	
	public void disarm() {
		this.armed = false;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	//TODO: Verify that this is the correct variable
	//#if polish.hasPointerEvents
	
	/* (non-Javadoc)
	 * @see de.enough.polish.ui.Container#handlePointerPressed(int, int)
	 */
	protected boolean handlePointerPressed(int arg0, int arg1) {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see de.enough.polish.ui.Container#handlePointerReleased(int, int)
	 */
	protected boolean handlePointerReleased(int arg0, int arg1) {
		return true;
	}
	//#endif
}
