package org.javarosa.view;

import javax.microedition.lcdui.Graphics;

import org.javarosa.utils.ViewUtils;

/**
 * The DiMEC implementation of a simple progress bar.
 * 
 * Displays a rectangular gague with progress/capacity filled in one color,
 * and the remainder filled in another.
 * 
 * @author ctsims
 * @date Aug-08-2007
 *
 */
public class DProgressBar extends Component {

	int _progress;
	int _capacity;
	
	/**
	 * Creates a new progress bar with the given capacity.
	 * 
	 * @param capacity The capactity of the Progress Bar.
	 */
	public DProgressBar(int capacity) {
		_capacity = capacity;
	}
	
	/**
	 * @param progress The Amount of Progress 
	 */
	public void setProgress(int progress) {
		_progress = progress;
		refresh();
	}
	
	/**
	 * @param capacity The new Capacity of the bar
	 */
	public void setCapacity(int capacity) {
		_capacity = capacity;
		refresh();
	}
	
	/**
	 * @return The current capcity of the bar.
	 */
	public int getCapacity() {
		return _capacity;
	}

	protected void drawInternal(Graphics g) {
		//Draw the Background
		g.setColor(ViewUtils.LIGHT_GREY);
		g.fillRect(0, 0, _width, _height);
		
		//Draw the amount filled
		int filledWidth = (_width*_progress)/_capacity;
		
		g.setColor(ViewUtils.PINK_GREY);
		g.fillRect(0,0,filledWidth,_height);
		
		//Finish by drawing the border
		g.setColor(ViewUtils.BLACK);
		g.drawRect(0, 0, _width, _height);
	}
	
	public void refresh() {
		
	}

}
