/**
 * 
 */
package org.javarosa.formmanager.view.chatterbox;

import java.util.Vector;

import javax.microedition.lcdui.Graphics;

import de.enough.polish.ui.CustomItem;
import de.enough.polish.ui.Style;

/**
 * @author Clayton Sims
 *
 */
public class ColorBar extends CustomItem {
	
	private Vector starts = new Vector();
	private Vector stops = new Vector();
	
	private int height = 320;
	
	private class Tuple {
		public int t1;
		public int t2;
		
		public Tuple(int t1, int t2) {
			this.t1 = t1;
			this.t2 = t2;
		}
	}

	protected ColorBar(String label) {
		super(label);
	}
	protected ColorBar(String label, Style style) {
		super(label, style);
	}

	private final int width = 2;
	

	/* (non-Javadoc)
	 * @see de.enough.polish.ui.CustomItem#getMinContentWidth()
	 */
	protected int getMinContentWidth() {
		return width;
	}

	/* (non-Javadoc)
	 * @see de.enough.polish.ui.CustomItem#getPrefContentWidth(int)
	 */
	protected int getPrefContentWidth(int height) {
		// TODO Auto-generated method stub
		return width;
	}

	/* (non-Javadoc)
	 * @see de.enough.polish.ui.CustomItem#paint(javax.microedition.lcdui.Graphics, int, int)
	 */
	protected void paint(Graphics g, int w, int h) {
		
		System.out.println("Painting bar");
		
		g.setColor(255, 255, 0);
		g.fillRect(0,0,w,h);
		
		int level = 0;
		int curStart = 0;
		int curStop = 0;
		//Total unnacounted for starts/ends
		int total = starts.size() + stops.size();
		
		
		Vector snips = new Vector();
		
		while(total != 0) {
			int iterStart = start(curStart);
			int iterStop = stop(curStop);
			if(iterStart < iterStop && iterStart != -1) {
				
				//We have a new start to handle that is the start of a nested group
				//Or the special case of starting a new root snip
				
				// Special case, don't draw anything if we're starting a new
				// snip
				if (curStart == 0 || level == 0 ) {
					
					// It's possible we'll have to draw the background here.
					
				} else {
					// Set our color, and draw the Start->Start snip
					setColor(level, curStop, g);
					int height = start(curStart) - start(curStart - 1);
					g.fillRect(0, start(curStart - 1), w, height);				
				}
				
				// Increment our current level
				level += 1;
				curStart += 1;
				
			}
			if(iterStart > iterStop || iterStart == -1) {
				//We're going to stop either a nested group or the root level group
				
				//Set our color, and draw the last Start->Stop snip
				setColor(level, curStop, g);
				int height = stop(curStop) - start(curStart-1);
				g.fillRect(0,start(curStart-1),w,height);//g.fillRect(0,start(curStart-1),w,height);
				
				//Move down one level
				level -= 1;
				
				//increment to the next stopping point
				curStop += 1;
			} else {
				//We're going to start a new group at the same time we're stopping the last
				//group. Not clear yet whether this can happen.
			}
			
			
			//Right now this only works if the invariant on the bars is valid, that needs to get addressed.
			total = (starts.size() - curStart) + (stops.size() - curStop); 
		}
	}
	
	private void setColor(int currentLevel, int numSnips, Graphics g) {
		int lm3 = currentLevel % 3;
		int blue = ((lm3 == 1) ? 1 : 0) * (((currentLevel / 3)+1) * 60);
		int green = ((lm3 == 2) ? 1 : 0) * (((currentLevel / 3)+1) * 60);
		int red = ((lm3 == 0) ? 1 : 0) * (((currentLevel / 3)+1) * 60);
		
		g.setColor(red, green, blue);
	}

	protected int getMinContentHeight() {
		// TODO Auto-generated method stub
		return 0;
	}

	protected int getPrefContentHeight(int width) {
		// TODO Auto-generated method stub
		return height;
	}
	
	public void setHeight(int height) {
		this.height  = height;
	}
	public void clearSpans() {
		starts.removeAllElements();
		stops.removeAllElements();
	}
	
	public void addSpan(int start, int stop) {
		sortedEntry(start, starts);
		sortedEntry(stop,stops);
	}
	
	private void sortedEntry(int n, Vector v) {
		int i;
		for(i = 0 ; i < v.size() ; ++i) {
			int value = ((Integer)v.elementAt(i)).intValue();
			if(n <= value) {
				break;
			}
		}
		v.insertElementAt(new Integer(n),i);
	}
	
	private int start(int i) {
		if(starts.size() <= i) {
			return -1;
		}
		return ((Integer)starts.elementAt(i)).intValue();
	}
	
	private int stop(int i) {
		if(stops.size() <= i) {
			return -1;
		}
		return ((Integer)stops.elementAt(i)).intValue();
	}
}
