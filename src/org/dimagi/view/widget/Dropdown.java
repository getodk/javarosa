package org.dimagi.view.widget;

import java.util.Vector;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import org.dimagi.utils.ViewUtils;
import org.dimagi.view.Widget;

public class Dropdown extends Widget {

	private Vector choices = new Vector();
	private String selectedChoice = "Selected";
	
	// drawing variables	
	private int fontHeight;
	private int xBufferSize;
	private int yBufferSize;
	private boolean listExpanded = true;
	
	// drawing variable for dropdown button (db)
	private int dbWidth;
	private int dbHeight;
	private int dbX0;
	private int dbY0;
	private int dbX0p25;
	private int dbY0p25;
	private int dbX0p5;
	private int dbY0p5;
	private int dbX0p75;
	private int dbY0p75;
	
	/**
	 * Creates a dropdown widget
	 */
	public Dropdown() {
	}
	
	public void addChoice(String choice) {
		choices.addElement(choice);
	}
	
	public void drawActiveWidget(Graphics g) {
		
		// offset
		int offset = 5;
		
		// buffers
		xBufferSize = this.getWidth()/10;
		yBufferSize = this.getHeight()/80;
		
		// upper box 
		int upperBoxWidth = getWidth()-xBufferSize;
		int upperBoxHeight = fontHeight;
		int upperBoxX0 = 0;
		int upperBoxY0 = yBufferSize;
		
		// dropdown button
		dbWidth = xBufferSize;
		dbHeight = fontHeight;
		dbX0 = getWidth()-2*xBufferSize;
		dbY0 = yBufferSize;
		dbX0p25 = dbX0 + dbWidth * 1/4;
		dbY0p25 = dbY0 + dbHeight * 1/4;
		dbX0p5 = dbX0 + dbWidth * 1/2;
		dbY0p5 = dbY0 + dbHeight * 1/2;
		dbX0p75 = dbX0 + dbWidth * 3/4;
		dbY0p75 = dbY0 + dbHeight * 3/4;

		// lower box
		int lowerBoxWidth = upperBoxWidth;
		int lowerBoxHeight = fontHeight*choices.size() + yBufferSize;
		int lowerBoxX0 = upperBoxX0;
		int lowerBoxY0 = yBufferSize + upperBoxHeight;
		
		// draw upper box
		g.setColor(ViewUtils.BLACK);
		g.drawRect(upperBoxX0, upperBoxY0, upperBoxWidth, upperBoxHeight);
		g.drawString((String)choices.firstElement(), upperBoxX0+offset, upperBoxY0, g.TOP | g.LEFT);
		
		// draw dropdown button 
		g.setColor(ViewUtils.LIGHT_GREY);
		g.fillRect(dbX0, dbY0, dbWidth, dbHeight);
		g.setColor(ViewUtils.BLACK);
		g.drawRect(dbX0, dbY0, dbWidth, dbHeight);
		
		// draw "V" inside dropdown button
		g.drawLine(dbX0p25, dbY0p25, dbX0p5, dbY0p75);
		g.drawLine(dbX0p5, dbY0p75, dbX0p75, dbY0p25);
	
		if (listExpanded) {
			// draw lower box
			g.drawRect(lowerBoxX0, lowerBoxY0, lowerBoxWidth, lowerBoxHeight);
			int y = lowerBoxY0 + yBufferSize;
			for (int i=1; i < choices.size(); i++) {
				g.drawString((String)choices.elementAt(i), lowerBoxX0+offset, y, g.TOP | g.LEFT);
				y += fontHeight + yBufferSize;
			}
			refresh();
		}
	}

	public void drawInactiveWidget(Graphics g) {
		xBufferSize = this.getWidth()/10;
		yBufferSize = this.getHeight()/10;
		g.setColor(ViewUtils.BLACK);
		g.drawString(selectedChoice, getWidth()-20, yBufferSize, g.TOP | g.RIGHT);
	}
	
	protected void drawInternal(Graphics g) {
		if ( this.isActiveWidget() ) {
			drawActiveWidget(g);
		} else {
			drawInactiveWidget(g);
		}
	}

	public void sizeWidget() {
		fontHeight = Font.getDefaultFont().getHeight();
		this.setHeight(fontHeight * (choices.size()+1));
	}

	public void sizeWidget(int width, int height) {
		this.setWidth(width);
		this.setHeight(height);
	}
	
	public void pointerPressed(int x, int y) {
		if (ViewUtils.checkPointInRectangle(x, y, dbX0, dbY0, dbWidth, dbHeight)) {
			buttonPressed();
		}
	}
	
	private void buttonPressed() {
		choices.setElementAt("Pressed", 0);
		listExpanded = true;
		refresh();
	}
	
}
