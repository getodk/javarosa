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
		xBufferSize = this.getWidth()/10;
		yBufferSize = this.getHeight()/10;
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

		g.setColor(ViewUtils.BLACK);
		g.drawRect(0, yBufferSize, getWidth()-xBufferSize, fontHeight);
		g.drawString((String)choices.firstElement(), 5, yBufferSize, g.TOP | g.LEFT);
		
		// draw box
		g.setColor(ViewUtils.LIGHT_GREY);
		g.fillRect(dbX0, dbY0, dbWidth, dbHeight);
		g.setColor(ViewUtils.BLACK);
		g.drawRect(dbX0, dbY0, dbWidth, dbHeight);
		
		// draw "V"
		g.drawLine(dbX0p25, dbY0p25, dbX0p5, dbY0p75);
		g.drawLine(dbX0p5, dbY0p75, dbX0p75, dbY0p25);
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
		this.setHeight(fontHeight * 2);
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
		refresh();
	}
	
}
