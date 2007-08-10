package org.dimagi.view.widget;

import java.util.Vector;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import org.dimagi.utils.ViewUtils;
import org.dimagi.view.Widget;

/**
 * A widget for accepting enumerated type choices in list of either
 * radiobuttons or checkboxes.
 *  
 * @author ctsims
 * @date Aug-09-2007
 */
public class ChoiceList extends Widget {
	public static final int SINGLE = 1;

	public static final int MULTI = 2;

	private Vector _choices = new Vector();

	private Vector _selectedChoices = new Vector();

	private int _choiceType = SINGLE;

	/**
	 * Creates a new ChoiceList widget
	 */
	public ChoiceList() {
	}

	/**
	 * @param choiceType The new Type of choices to be displayed.
	 */
	public void setChoiceType(int choiceType) {
		_choiceType = choiceType;
		refresh();
	}

	/**
	 * @param choice The new choice to be added to the list.
	 */
	public void addChoice(String choice) {
		_choices.addElement(choice);
	}

	/**
	 * Sets the height of the widget to fit all choices.
	 */
	public void sizeWidget() {
		int fontHeight = Font.getDefaultFont().getHeight();

		int totalHeight = (_choices.size() + 1) * fontHeight;

		this.setHeight(totalHeight);
	}

	/**
	 * Inherits a pointer event, identifies whether a choice was clicked, and deals
	 * with the selection appropriately. 
	 */
	public void pointerPressed(int x, int y) {
		int textHeight = Font.getDefaultFont().getHeight();
		int yBuffer = (getHeight() - textHeight * _choices.size())
				/ (_choices.size() + 1);

		boolean itemChanged = false;

		// For right now, we're just going to go through each item and see if
		// the click applies to it
		// TODO: Make this a O(1) Operation.
		for (int i = 0; i < _choices.size(); ++i) {
			// int yButton = (yBuffer + textHeight)*(i+1);
			int yButton = (yBuffer) * (i + 1) + textHeight * i;
			if (ViewUtils.checkPointInRectangle(x, y, 0, yButton, this
					.getWidth(), textHeight)) {
				buttonPressed(i);
				itemChanged = true;
			}
		}

		//TODO: Make buttonPressed return whether or not there was a change
		if (itemChanged) {
			refresh();
		}
	}

	/**
	 * Deals with a choice being selected
	 * @param index the choice selected.
	 */
	private void buttonPressed(int index) {
		switch (_choiceType) {
		case (SINGLE):
			_selectedChoices.removeAllElements();
			_selectedChoices.addElement(new Integer(index));
			fireWidgetComplete();
			break;
		case (MULTI):
			Integer bigIndex = new Integer(index);
			if(_selectedChoices.contains(bigIndex)) {
				_selectedChoices.removeElement(bigIndex);
			}
			else {
				_selectedChoices.addElement(bigIndex);
			}
			break;
		}
	}

	protected void drawInternal(Graphics g) {
		int textHeight = Font.getDefaultFont().getHeight();
		int xBuffer = Font.getDefaultFont().getHeight();
		int yBuffer = (getHeight() - textHeight * _choices.size())
				/ (_choices.size() + 1);

		int buttonWidth = ViewUtils.findAcceptableBoxSize(textHeight);
		int buttonHeight = ViewUtils.findAcceptableBoxSize(textHeight);

		g.setColor(ViewUtils.BLACK);

		for (int i = 0; i < _choices.size(); ++i) {
			String choice = (String) _choices.elementAt(i);

			int xButton = xBuffer;
			// int yButton = (yBuffer + textHeight)*(i+1);
			int yButton = (yBuffer) * (i + 1) + textHeight * i;

			int scaleDenom = 2;
			int scaleNumer = 1;
			int xGutter = (buttonWidth * (scaleDenom - scaleNumer))
					/ (2 * scaleDenom);
			int yGutter = (buttonHeight * (scaleDenom - scaleNumer))
					/ (2 * scaleDenom);

			g.drawString(choice, xBuffer + buttonWidth * 2, yButton,
					Graphics.TOP | Graphics.LEFT);

			switch (_choiceType) {
			case SINGLE:
				// Draw the Container
				g.drawArc(xButton, yButton, buttonWidth, buttonHeight, 0, 360);
				// Draw the selection
				if (_selectedChoices.contains(new Integer(i))) {

					g.fillArc(xButton + xGutter, yButton + yGutter, buttonWidth
							* scaleNumer / scaleDenom, buttonHeight
							* scaleNumer / scaleDenom, 0, 360);
				}
				break;
			case MULTI:
				g.drawRect(xButton, yButton, buttonWidth, buttonHeight);
				if (_selectedChoices.contains(new Integer(i))) {
					g.fillRect(xButton + xGutter, yButton + yGutter,
							buttonWidth * scaleNumer / scaleDenom, buttonHeight
									* scaleNumer / scaleDenom);
				}
				break;
			}

			/*
			 * g.setColor(ViewUtils.WHITE); g.fillRect(0, 0, 400,
			 * g.getFont().getHeight()*2); g.setColor(ViewUtils.BLACK);
			 * g.drawString("Gutter - X: "+ xGutter + " Y: " + yGutter, 0, 0,
			 * Graphics.TOP|Graphics.LEFT); g.drawString("Button - X: "+
			 * buttonWidth + " Y: " + buttonHeight, 0, g.getFont().getHeight(),
			 * Graphics.TOP|Graphics.LEFT);
			 */
		}
	}
}
