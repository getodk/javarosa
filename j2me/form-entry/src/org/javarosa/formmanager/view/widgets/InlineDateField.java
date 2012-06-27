/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.formmanager.view.widgets;

import java.util.Calendar;
import java.util.Date;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.services.Logger;

import de.enough.polish.ui.CustomItem;

/**
 * Date field that fits into the chatterbox display.
 */
public class InlineDateField extends CustomItem {

	// value is the current value of the field, and defaults to no selection (null).
	private Calendar value; 
	
	// sel is the current selection of the field, and defaults to today's date.
	private Calendar sel;
	
	// Indicates if the month control is currently selected.
	private int selectionMode;
	private int repeatCount;
	private final int DAYS = 1;
	private final int NO_DATE = 2;
	private final int MONTH = 3;
	private final int YEAR = 4;
	
	//private Command clearCmd;
	
	private final String[] monthNames = {"January", "February",
		"March", "April", "May", "June", "July",
		"August", "September", "October", "November",
		"December"};
	private final String[] dayNames = {"Sunday", "Monday", "Tuesday", 
		"Wednesday", "Thursday", "Friday", "Saturday"};
	
	public InlineDateField(String label) {
		super("", null);
		
		// Initialize value to null.
		value = null;
		// Initialize selection to current date.
		sel = Calendar.getInstance();
		selectionMode = DAYS;
		repeatCount = 0;
	}
	
	public int getMinContentHeight() { return 10; }
	public int getMinContentWidth() { return 10; }
	public int getPrefContentHeight(int h) { Font f = Font.getDefaultFont(); return 8 * f.getHeight() + 6; }
	public int getPrefContentWidth(int w) { return 99999; /* force the max */ }

  // Handle key presses.
	public boolean handleKeyPressed(int keyCode, int gameAction) {
		try {
		
			int curDay = sel.get(Calendar.DAY_OF_MONTH);
			int daysInMonth = DateUtils.daysInMonth(sel.get(Calendar.MONTH), sel.get(Calendar.YEAR));
			boolean changed = false;
			
			// Handle four arrows.
			if (gameAction == Canvas.RIGHT) {
				if (selectionMode == YEAR) {
					changeMonth(sel, 12);
					changed = true;
				} else if (selectionMode == MONTH) {
					changeMonth(sel, 1);
					changed = true;
				} else if (selectionMode == DAYS) {
					if (curDay < daysInMonth) {
						curDay++;
						changed = true;
					} else {
						selectionMode = NO_DATE;
						changed = true;
					}
				} 
			} else if (gameAction == Canvas.LEFT) {
				if (selectionMode == YEAR) {
					changeMonth(sel, -12);
					changed = true;
				} else if (selectionMode == MONTH) {
					changeMonth(sel, -1);
					changed = true;
				} else if (selectionMode == DAYS && curDay > 1) {
					curDay--;
					changed = true;
				} else if (selectionMode == NO_DATE) {
					curDay = daysInMonth;
					selectionMode = DAYS;
					changed = true;
				}
			} else if (gameAction == Canvas.UP) {
				if (selectionMode == MONTH) {
					selectionMode = YEAR;
					changed = true;
				} else if (selectionMode == DAYS) {
					if (curDay > 7)
						curDay -= 7;
					else 
						selectionMode = MONTH;
					changed = true;
				} else if (selectionMode == NO_DATE) {
					selectionMode = DAYS;
					curDay = 28;
					changed = true;
				}
			} else if (gameAction == Canvas.DOWN) {
				if (selectionMode == YEAR) {
					selectionMode = MONTH;
					changed = true;
				} else if (selectionMode == MONTH) {
					selectionMode = DAYS;
					changed = true;
				} else if (selectionMode == DAYS) {
					if (curDay <= daysInMonth - 7) {
						curDay += 7;
						changed = true;
					} else {
						selectionMode = NO_DATE;
						changed = true;
					}
				}
			} else if (gameAction == Canvas.FIRE) {
				if (selectionMode == NO_DATE) {
					value = null;
					changed = true;
				} else if (selectionMode == DAYS) {
					// If value is null, create a new instance.
					if (value == null) value = Calendar.getInstance();
					
					// Set the value to be the same as the selection.
					value.setTime(sel.getTime());
					changed = true;
				}
			}
	
			// Check if changed flag has been raised and repaint if it has.
			if (changed) {
				sel.set(Calendar.DAY_OF_MONTH, curDay);
				repaint();
				return true;
			} else
				return false;
			
		} catch (Exception e) {
			Logger.die("gui-keydown", e);
			return false;
		}
	}
	
	public boolean handleKeyRepeated(int keyCode, int gameAction) {
		
		if ((selectionMode == YEAR || selectionMode == MONTH)
			&& (gameAction == Canvas.LEFT || gameAction == Canvas.RIGHT)) {

			try {
			
				int repeatIncrement;
				int diff;
	
				// Increment repeat count.
				repeatCount++;
			
				// Determine number of years/months to increment/decrement by.
				if (repeatCount % 2 == 0) repeatIncrement = 0;
				else if (repeatCount <= 10) repeatIncrement = 1;
				else repeatIncrement = 5;
			
				diff = (gameAction == Canvas.LEFT ? -1 : 1) * (selectionMode == YEAR ? 12 : 1) * repeatIncrement;
					
				changeMonth(sel, diff);
				
			} catch (Exception e) {
				Logger.die("gui-keyrep", e);
			}

			return true;
		} else
			return false;
	}

	//needs no exception handling
	public boolean handleKeyReleased(int keyCode, int gameAction) {
		if (gameAction == Canvas.LEFT || gameAction == Canvas.RIGHT) {
			repeatCount = 0;
			return true;
		} else
			return false;
	}
	
	
	// Changes the month/year of the given Calendar by diff months.
	private void changeMonth(Calendar cal, int diff) {
		int n = cal.get(Calendar.YEAR) * 12 + cal.get(Calendar.MONTH) + diff;
		cal.set(Calendar.YEAR, n / 12);
		cal.set(Calendar.MONTH, n % 12);
	}
	
	public Date getValue() { return value == null ? null : value.getTime(); }
	
	public void setValue(Date d) { 
		if (d == null) {
			value = null;
			sel = Calendar.getInstance();
		} else {
			if (value == null) value = Calendar.getInstance();
			value.setTime(d);
			sel.setTime(d);
		}
		repaint();
	}
	
	// Draw everything.
	public void paint(Graphics g, int width, int height) {
		int anchor = Graphics.HCENTER | Graphics.TOP;
		Font font = Font.getDefaultFont();

		// Determine spacings.
		int hSpacing = width / 7;
		int vSpacing = 13;
		int boxWidth = font.stringWidth("22") + 4;
		int boxHeight = font.getHeight() - 2;

		// Set the year.
		String y = String.valueOf(sel.get(Calendar.YEAR));

		// If year is selected, draw outline around it.
		if (selectionMode == YEAR) {
			int strWidth = font.stringWidth(y);
			g.drawRect(width / 2 - strWidth / 2 - 4, 1, strWidth + 6, boxHeight + 2);
		}
		
		// Draw the year.
		g.drawString("<  " + y + "  >", width / 2, 1, anchor);
        
		// Set the ypos for the month.
		int monthY = vSpacing + 2;
		
		// Set the month.
		String m = monthNames[sel.get(Calendar.MONTH)];

		// If month is selected, draw outline around it.
		if (selectionMode == MONTH) {
			int strWidth = font.stringWidth(m);
			g.drawRect(width / 2 - strWidth / 2 - 4, monthY, strWidth + 6, boxHeight + 2);
		}
		
		// Draw the month.
		g.drawString("<  " + m + "  >", width / 2, monthY, anchor);
		
		// Draw the day name abbreviations.
		int dayAbbrY = monthY + vSpacing + 1;
		for (int c = 0; c < 7; c++)
			g.drawString(dayNames[c].substring(0, 1), (int)((0.5 + c) * hSpacing), dayAbbrY, anchor);
			
		// Set up dayCounter.
		int daysInMonth = DateUtils.daysInMonth(sel.get(Calendar.MONTH), sel.get(Calendar.YEAR));
		int dayCounter = -(getDayOfWeekOfFirstDayOfMonth() - 1);
		
		// Draw numbers.
		for (int r = 0; r < 6; r++)
			for (int c = 0; c < 7; c++)
			{
				// Draw number.
				if (dayCounter >= 0 && dayCounter < daysInMonth) {
					
					int boxX = (int)((0.5 + c) * hSpacing - boxWidth / 2);
					int boxY = dayAbbrY + (1 + r) * vSpacing;
					
					// If this is the currently selected day, draw an outline around it.
					if (selectionMode == DAYS && dayCounter == sel.get(Calendar.DAY_OF_MONTH) - 1)
						g.drawRect(boxX, boxY, boxWidth, boxHeight);
						
					// If this is the chosen date, draw a background box and switch color to white.
					if (value != null && dayCounter == value.get(Calendar.DAY_OF_MONTH) - 1
						&& sel.get(Calendar.MONTH) == value.get(Calendar.MONTH)
						&& sel.get(Calendar.YEAR) == value.get(Calendar.YEAR)) {
						g.fillRect(boxX + 1, boxY + 1, boxWidth - 1, boxHeight - 1);
						g.setColor(255, 255, 255);
					}
					
					// Draw the number.
					g.drawString(String.valueOf(dayCounter + 1), boxX + boxWidth / 2 + 1, boxY - 1, anchor);
					
					// Ensure color is back to black.
					g.setColor(0, 0, 0);
				}
				
				// Increment day counter.
				dayCounter++;
			}
		
		// Set the no date string and height.
		String noDate = "No Date";
		int noDateY = dayAbbrY + 6 * vSpacing;
		int strWidth = font.stringWidth(noDate);

		// If 'no date' is the current selection, draw an outline.
		if (selectionMode == NO_DATE) {
			g.drawRect(width / 2 - strWidth / 2 - 4, noDateY, strWidth + 6, boxHeight + 2);
		}

		// If the value is 'no date', draw a box and set color.
		if (value == null) {
			g.fillRect(width / 2 - strWidth / 2 - 3, noDateY + 1, strWidth + 5, boxHeight + 1);
			g.setColor(255, 255, 255);
		}

		// Draw the 'no date' label.
		g.drawString(noDate, width / 2, noDateY, anchor);

		// Ensure color is back to black.
		g.setColor(0, 0, 0);
	}
	
	private int getDayOfWeekOfFirstDayOfMonth() {
		Calendar first = Calendar.getInstance();
		first.setTime(sel.getTime());
		first.set(Calendar.DAY_OF_MONTH, 1);
		return first.get(Calendar.DAY_OF_WEEK);
	}
}