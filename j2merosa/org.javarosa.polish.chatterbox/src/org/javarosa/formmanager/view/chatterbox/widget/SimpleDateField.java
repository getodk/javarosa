package org.javarosa.formmanager.view.chatterbox.widget;

import java.util.Calendar;
import java.util.Date;
import org.javarosa.core.model.utils.DateUtils;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;

import de.enough.polish.ui.Item;
import de.enough.polish.ui.CustomItem;
import de.enough.polish.ui.Style;

/**
 * 
 * @author Ndubisi Onuora
 */

public class SimpleDateField extends CustomItem 
{
	private Calendar value;
	private Calendar selection;
	
	private final int MIN_MONTH = Calendar.JANUARY; //(Assuming it equals 0)
	private final int MAX_MONTH = Calendar.DECEMBER; //(Assuming it equals 11)
	
	private final int MIN_DAY = 1;
	private final int MAX_DAY = 31;
	
	private final int MIN_YEAR = 1970;
	private final int MAX_YEAR = 2069;
	
	private int prevSelectionMode;
	private int selectionMode; //The mode{YEAR, MONTH, DAY} the user is currently on
	private final int YEAR = 0;
	private final int MONTH = 1;
	private final int DAY = 2;
	/*
	 * No selection prevents user from changing other fields 
	 * when a key is accidentally pressed.
	 */
	private final int NONE = 3;
	
	/*
	 * Current value of the month, day, or year
	 */
	private int currMonth;
	private int currDay;
	private int currYear;
	
	/*Whether the user prefers the American or European date format
	 * (MM/DD/YYYY) American
	 * (DD/MM/YYYY) European
	 */
	private int dateFormat; 
	private final int AMERICAN = 0;
	private final int EUROPEAN = 1;

	/*
	 * The field the user is currently on
	 */
	private int fieldMode;
	private final int FIRST_FIELD = 0;
	private final int SECOND_FIELD = 1;
	private final int THIRD_FIELD = 2;
	private final int FOURTH_FIELD = 3;
	
	//2-digit fields 
	private int firstField;
	private int secondField;
	
	//4-digit fields for year
	private int thirdField;
	private int fourthField;
	
	Graphics gObj;
	int width = -1;
	int height = -1;
	
	//Date Rect Position
	int dateRectX = -1;
	int dateRectY = -1;
	
	private int repeatCount;
	private final String NO_DATE_STR = "No Date"; 
	
	public SimpleDateField(String label)
	{
		super("", null);
		selection = Calendar.getInstance();
		dateFormat = AMERICAN;
		prevSelectionMode = -1;
		selectionMode = MONTH; //Default is American format	
		
		value = null;
		
		currMonth = selection.get(Calendar.MONTH);
		currDay = selection.get(Calendar.DAY_OF_MONTH);
		currYear = selection.get(Calendar.YEAR);
				
		fieldMode = FIRST_FIELD;
		
		gObj = null;
	}
	
	public int getMinContentHeight() 
	{ 
		return 10; 
	}
	
	public int getMinContentWidth()
	{ 
		return 10; 
	}
	
	public int getPrefContentHeight(int h)
	{ 
		Font f = Font.getDefaultFont(); 
		return 8 * f.getHeight() + 6; 
	}
	
	public int getPrefContentWidth(int w)
	{ 
		return 99999; 
	}
	
	public boolean handleKeyPressed(int keyCode, int gameAction)
	{
		System.err.println("KeyCode =" + keyCode);
		System.err.println("GameAction =" + gameAction);
		boolean gameActionActivated = handleGameActionPressed(gameAction);
		
		boolean keyCodePressed = false;
		if(selectionMode != NONE)
			keyCodePressed = handleKeyCodePressed(keyCode);
		boolean fieldsChanged = keyCodePressed || gameActionActivated;
		if(fieldsChanged)
		{
			updateDate();
			repaint();
		}
		return fieldsChanged;
	}
	
	public boolean handleKeyRepeated(int keyCode, int gameAction)
	{
		//repeatCount = 1;
		handleKeyPressed(keyCode, gameAction);
		
		return false;
	}
	
	public boolean handleKeyReleased(int keyCode, int gameAction)
	{
		repeatCount = 0;
		return false;
	}
	
	public Date getValue()
	{
		if(selectionMode == NONE)
			return null;
		Date d = selection.getTime();		
		return d;
	}
	
	public void setValue(Date d)
	{
		selection.setTime(d);
	}
	
	private boolean handleGameActionPressed(int gameAction)
	{
		boolean fieldChanged = false;
		if(gameAction == Canvas.UP)
		{			
			if(selectionMode == MONTH)
			{				
				++currMonth;
				currMonth += repeatCount;				
			}
			else if(selectionMode == DAY)
			{					
				++currDay;
				currDay += repeatCount;				
			}
			else if(selectionMode == YEAR)
			{				
				++currYear;
				currYear += repeatCount;
			}
			else
				fieldChanged = false;
			
			fieldChanged = true;
		}
		else if(gameAction == Canvas.DOWN)
		{
			if(selectionMode == MONTH)
			{
				/*
				if(currMonth != Calendar.JANUARY)
					--currMonth;
				else
					currMonth = Calendar.DECEMBER;
				*/
				--currMonth;
				currMonth -= repeatCount;
			}
			else if(selectionMode == DAY)
			{
				/*
				 * Months with 30 days.
				 */
				/*
				boolean awkMonth = (currMonth == Calendar.APRIL || currMonth == Calendar.JUNE || currMonth == Calendar.SEPTEMBER || currMonth == Calendar.NOVEMBER);
				if(awkMonth)
				{
					int lessDays = MAX_DAY - 1;
					if(currDay == MIN_DAY)
					{
						currDay = lessDays;
					}
					else
						--currDay;
				}				
				else if(currMonth == Calendar.FEBRUARY)
				{
					int febDays = MAX_DAY - 3;
					if(isLeapYear())
						febDays = MAX_DAY - 2;
					if(currDay == MIN_DAY)
					{
						currDay = febDays;
					}
					else
						--currDay;
				}
				else if(currDay == MIN_DAY)
					currDay = MAX_DAY;
				else
					--currDay;
				*/
				--currDay;
				currDay -= repeatCount;
			}
			else if(selectionMode == YEAR)
			{
				/*
				if(currYear == MIN_YEAR)
					currYear = MAX_YEAR;
				else
				*/
				--currYear;
				currYear -= repeatCount;
			}
			else
				fieldChanged = false;
			
			fieldChanged = true;
		}
		else if(gameAction == Canvas.RIGHT)
		{
			//American dates
			if(selectionMode == MONTH && dateFormat == AMERICAN)
			{
				prevSelectionMode = MONTH;
				selectionMode = DAY;
			}
			else if(selectionMode == DAY && dateFormat == AMERICAN)
			{
				prevSelectionMode = DAY;
				selectionMode = YEAR;
			}
			else if(selectionMode == YEAR && dateFormat == AMERICAN)
			{
				prevSelectionMode = YEAR;
				selectionMode = NONE;
			}
			else if(selectionMode == NONE && dateFormat == AMERICAN)
			{
				prevSelectionMode = NONE;
				selectionMode = MONTH;
			}			

			//European dates
			if(selectionMode == DAY && dateFormat == EUROPEAN)
			{
				prevSelectionMode = DAY;
				selectionMode = MONTH;
			}
			else if(selectionMode == MONTH && dateFormat == EUROPEAN)
			{
				prevSelectionMode = MONTH;
				selectionMode = YEAR;
			}
			else if(selectionMode == YEAR && dateFormat == EUROPEAN)
			{
				prevSelectionMode = YEAR;
				selectionMode = NONE;
			}
			else if(selectionMode == NONE && dateFormat == EUROPEAN)
			{
				prevSelectionMode = NONE;
				selectionMode = DAY;
			}
			fieldMode = FIRST_FIELD; //Each time the user changes a field, the field mode will reset to the first field.
			fieldChanged = true;			
		}
		/*
		 * Field will always change when left or right button has been pressed.
		 */
		else if(gameAction == Canvas.LEFT)
		{
			//American dates
			if(selectionMode == MONTH && dateFormat == AMERICAN)
			{
				prevSelectionMode = MONTH;
				selectionMode = NONE;
			}
			else if(selectionMode == DAY && dateFormat == AMERICAN)
			{
				prevSelectionMode = DAY;
				selectionMode = MONTH;
			}
			else if(selectionMode == YEAR && dateFormat == AMERICAN)
			{
				prevSelectionMode = YEAR;
				selectionMode = DAY;
			}
			else if(selectionMode == NONE && dateFormat == AMERICAN)
			{
				prevSelectionMode = NONE;
				selectionMode = YEAR;
			}			

			//European dates
			if(selectionMode == DAY && dateFormat == EUROPEAN)
			{
				prevSelectionMode = DAY;
				selectionMode = NONE;
			}
			else if(selectionMode == MONTH && dateFormat == EUROPEAN)
			{
				prevSelectionMode = MONTH;
				selectionMode = DAY;
			}
			else if(selectionMode == YEAR && dateFormat == EUROPEAN)
			{
				prevSelectionMode = YEAR;
				selectionMode = MONTH;
			}
			else if(selectionMode == NONE && dateFormat == EUROPEAN)
			{
				prevSelectionMode = NONE;
				selectionMode = YEAR;
			}
			fieldMode = FIRST_FIELD; //Each time the user changes a field, the field mode will reset to the first field.
			fieldChanged = true;
		}		
		/*
		 * Essentially the "okay" for this widget.
		 * Makes sure date is valid before final submission
		 */
		else if(gameAction == Canvas.FIRE)
		{
			if(!validDate())
			{
				invalidDate();
			}
			fieldChanged = true;
		}
		fixDate();
		return fieldChanged;
	}
	/*
	 * Pressing a number when not in the NONE selectionMode will 
	 * alter the state of the field.
	 */
	private boolean handleKeyCodePressed(int keyCode)
	{
		/*
		 * For each key pressed into of the date fields, the application must wait for the subsequent digit.
		 * However in a month field, if the user presses "1" and presses the arrow key, the field automatically becomes "01"
		 */
		
		if(keyCode == Canvas.KEY_NUM0)
		{			
			switch(fieldMode)
			{
				case FIRST_FIELD:
					firstField = 0;
					break;				
				case SECOND_FIELD:
					secondField = 0;
					break;
				case THIRD_FIELD:
					thirdField = 0;
					break;
				case FOURTH_FIELD:
					fourthField = 0;
					break;
			}
		}
		else if(keyCode == Canvas.KEY_NUM1)
		{
			switch(fieldMode)
			{
				case FIRST_FIELD:
					firstField = 1;
					break;				
				case SECOND_FIELD:
					secondField = 1;
					break;
				case THIRD_FIELD:
					thirdField = 1;
					break;
				case FOURTH_FIELD:
					fourthField = 1;
					break;
			}
		}
		else if(keyCode == Canvas.KEY_NUM2)
		{
			switch(fieldMode)
			{
				case FIRST_FIELD:
					firstField = 2;
					break;				
				case SECOND_FIELD:
					secondField = 2;
					break;
				case THIRD_FIELD:
					thirdField = 2;
					break;
				case FOURTH_FIELD:
					fourthField = 2;
					break;
			}
		}
		else if(keyCode == Canvas.KEY_NUM3)
		{
			switch(fieldMode)
			{
				case FIRST_FIELD:
					firstField = 3;
					break;				
				case SECOND_FIELD:
					secondField = 3;
					break;
				case THIRD_FIELD:
					thirdField = 3;
					break;
				case FOURTH_FIELD:
					fourthField = 3;
					break;
			}
		}
		else if(keyCode == Canvas.KEY_NUM4)
		{			
			switch(fieldMode)
			{
				case FIRST_FIELD:
					firstField = 4;
					break;				
				case SECOND_FIELD:
					secondField = 4;
					break;
				case THIRD_FIELD:
					thirdField = 4;
					break;
				case FOURTH_FIELD:
					fourthField = 4;
					break;
			}
		}
		else if(keyCode == Canvas.KEY_NUM5)
		{
			switch(fieldMode)
			{
				case FIRST_FIELD:
					firstField = 5;
					break;				
				case SECOND_FIELD:
					secondField = 5;
					break;
				case THIRD_FIELD:
					thirdField = 5;
					break;
				case FOURTH_FIELD:
					fourthField = 5;
					break;
			}
		}
		else if(keyCode == Canvas.KEY_NUM6)
		{
			switch(fieldMode)
			{
				case FIRST_FIELD:
					firstField = 6;
					break;				
				case SECOND_FIELD:
					secondField = 6;
					break;
				case THIRD_FIELD:
					thirdField = 6;
					break;
				case FOURTH_FIELD:
					fourthField = 6;
					break;
			}
		}
		else if(keyCode == Canvas.KEY_NUM7)
		{
			switch(fieldMode)
			{
				case FIRST_FIELD:
					firstField = 7;
					break;				
				case SECOND_FIELD:
					secondField = 7;
					break;
				case THIRD_FIELD:
					thirdField = 7;
					break;
				case FOURTH_FIELD:
					fourthField = 7;
					break;
			}
		}
		else if(keyCode == Canvas.KEY_NUM8)
		{
			switch(fieldMode)
			{
				case FIRST_FIELD:
					firstField = 8;
					break;				
				case SECOND_FIELD:
					secondField = 8;
					break;
				case THIRD_FIELD:
					thirdField = 8;
					break;
				case FOURTH_FIELD:
					fourthField = 8;
					break;
			}
		}
		else if(keyCode == Canvas.KEY_NUM9)
		{
			switch(fieldMode)
			{
				case FIRST_FIELD:
					firstField = 9;
					break;				
				case SECOND_FIELD:
					secondField = 9;
					break;
				case THIRD_FIELD:
					thirdField = 9;
					break;
				case FOURTH_FIELD:
					fourthField = 9;
					break;
			}
		}
		
		else
			return false;
		/*
		 * Make the changes to the date fields that are respectively
		 * selected.
		 * However the fieldMode must be reset each time the selection has changed
		 */
		
		System.err.println("SelectionMode=" + selectionMode + "\nPrevSelectionMode=" + prevSelectionMode);
		if(selectionMode == MONTH)
		{
			if(prevSelectionMode == MONTH)
			{
				if(fieldMode == FIRST_FIELD)
				{
					String monthStr = firstField + "";
					System.err.println("Month=" + monthStr);
					currMonth = Integer.parseInt(monthStr);
					--currMonth; //Assuming the user does not start counting at 0 for Januray
				}
				else if(fieldMode == SECOND_FIELD)
				{
					String monthStr = firstField + "" + secondField + "";
					System.err.println("Month=" + monthStr);
					currMonth = Integer.parseInt(monthStr);
					--currMonth; //Assuming the user does not start counting at 0 for Januray
				}
				++fieldMode; //Move to next field
			}			
			else
			{
				String monthStr = firstField + "";
				System.err.println("Month=" + monthStr);
				currMonth = Integer.parseInt(monthStr);
				--currMonth;
				prevSelectionMode = MONTH;
				++fieldMode;
			}
		}
		else if(selectionMode == DAY)
		{			
			if(prevSelectionMode == DAY)
			{
				if(fieldMode == FIRST_FIELD)
				{
					String dayStr = firstField + "";
					System.err.println("Day=" + dayStr);
					currDay = Integer.parseInt(dayStr);
				}
				else if(fieldMode == SECOND_FIELD)
				{
					String dayStr = firstField + "" + secondField + "";
					System.err.println("Day=" + dayStr);
					currDay = Integer.parseInt(dayStr);					
				}
				++fieldMode; //Move to next field
			}			
			else
			{
				String dayStr = firstField + "";
				System.err.println("Day=" + dayStr);
				currDay = Integer.parseInt(dayStr);
				prevSelectionMode = DAY;
				++fieldMode;
			}
		}
		else if(selectionMode == YEAR)
		{
			if(prevSelectionMode == YEAR)
			{
				if(fieldMode == FIRST_FIELD)
				{
					String yearStr = firstField + "";
					System.err.println("Year=" + yearStr);
					currYear = Integer.parseInt(yearStr);
				}
				else if(fieldMode == SECOND_FIELD)
				{
					String yearStr = firstField + "" + secondField + "";
					System.err.println("Year=" + yearStr);
					currYear = Integer.parseInt(yearStr);					
				}
				else if(fieldMode == THIRD_FIELD)
				{
					String yearStr = firstField + "" + secondField + "" + thirdField + "";
					System.err.println("Year=" + yearStr);
					currYear = Integer.parseInt(yearStr);					
				}
				else if(fieldMode == FOURTH_FIELD)
				{
					String yearStr = firstField + "" + secondField + "" + thirdField + "" + fourthField + "";
					System.err.println("Year=" + yearStr);
					currYear = Integer.parseInt(yearStr);				
				}
				++fieldMode; //Move to next field
			}
			else
			{
				String yearStr = firstField + "";
				System.err.println("Year=" + yearStr);
				currYear = Integer.parseInt(yearStr);
				prevSelectionMode = YEAR;
				++fieldMode;
			}
		}
		wrapFieldMode();
		return true;
	}
	
	/*
	 * Wraps the field mode according to the date field
	 * and the current field mode.
	 */
	private void wrapFieldMode()
	{
		if( (selectionMode == MONTH) || (selectionMode == DAY) )
		{
			if(fieldMode > SECOND_FIELD)
				fieldMode = FIRST_FIELD;
		}
		else if(selectionMode == YEAR)
		{
			if(fieldMode > FOURTH_FIELD)
				fieldMode = FIRST_FIELD;
		}
	}
	
	private void updateDate()
	{		
		selection.set(Calendar.MONTH, currMonth);
		selection.set(Calendar.DAY_OF_MONTH, currDay);
		selection.set(Calendar.YEAR, currYear);
	}
	
	/*
	 * Resets all fields to 0
	 */
	private void resetFields()
	{
		firstField = 0;
		secondField = 0; 
		thirdField = 0; 
		fourthField = 0;
	}
	
	private boolean isLeapYear()
	{
		return( ((currYear%4 == 0) && (currYear%100 != 0)) || (currYear%400 == 0) );
	}
	
	protected void paint(Graphics g, int w, int h)
	{
		if(gObj == null)
		{
			gObj = g;
		}
		else if(width < 0)
		{
			width = w;
		}
		else if(height < 0)
		{
			height = h;
		}
		//g.drawString("I am a " + "SimpleDateField. Hear me ROAR!", w/2, h/2, 0);
		//g.drawString("MM/DD/YYYY", w/3 + 5, h/3 + 30, Graphics.TOP|Graphics.RIGHT);
		dateRectX = w/3;
		dateRectY = h/3;
		if(!validDate())
		{
			invalidDate();		
		}
		drawDateBox(dateRectX - 50, dateRectY + 2);
		int adjMonth = currMonth + 1;
		g.drawString("MM/DD/YYYY", dateRectX + 15, dateRectY - 25, Graphics.TOP|Graphics.RIGHT);
		
		//Fit the format for drawing dates		
		String adjMonthStr = (new Integer(adjMonth)).toString();
		if(adjMonth < 10 && adjMonth > 0)
			adjMonthStr = "0" + adjMonthStr;
		
		String dayStr = (new Integer(currDay)).toString();
		if(currDay < 10 && currDay > 0)
			dayStr = "0" + dayStr;
			
		g.drawString(adjMonthStr + "/" + dayStr + "/" + currYear, dateRectX, dateRectY, Graphics.TOP|Graphics.RIGHT);
		g.drawString(NO_DATE_STR, dateRectX + (Font.getDefaultFont()).stringWidth(NO_DATE_STR) + 10 , dateRectY, Graphics.TOP|Graphics.RIGHT);
		
	}
	
	private void drawDateBox(int xPos, int yPos)
	{
		int prevColor = gObj.getColor();
		Font font = Font.getDefaultFont();
		int boxWidth = font.stringWidth("30") + 5;
		int boxHeight = font.getHeight() - 2;
		
		String monthStr = (new Integer(currMonth+1)).toString() + " ";
		String dayStr = (new Integer(currDay)).toString() + " ";
		String yearStr = (new Integer(currYear)).toString();
				
		//Clear the previous rectangle on the date field
		if(prevSelectionMode != -1)
		{
			gObj.setColor(0x00FFFFFF);
			if(prevSelectionMode == MONTH)
			{
				boxWidth = font.stringWidth(monthStr);
				gObj.fillRect(xPos, yPos, boxWidth, boxHeight);
			}
			else if(prevSelectionMode == DAY)
			{
				boxWidth = font.stringWidth(dayStr);
				gObj.fillRect(xPos + font.stringWidth(monthStr), yPos, boxWidth, boxHeight);
			}
			else if(prevSelectionMode == YEAR)
			{
				boxWidth = font.stringWidth(yearStr);
				gObj.fillRect(xPos + font.stringWidth(monthStr) + font.stringWidth(dayStr), yPos, boxWidth, boxHeight);
			}
			else if(prevSelectionMode == NONE)
			{								
				boxWidth = font.stringWidth(NO_DATE_STR) + 5;
				gObj.fillRect(xPos + font.stringWidth(monthStr) + font.stringWidth(dayStr) + font.stringWidth(yearStr) + font.stringWidth("  "), yPos, boxWidth, boxHeight);
			}
		}
		gObj.setColor(0x0000FF0A);
		if(selectionMode == MONTH)
		{
			boxWidth = font.stringWidth(monthStr);
			gObj.fillRect(xPos, yPos, boxWidth, boxHeight);
		}
		else if(selectionMode == DAY)
		{
			boxWidth = font.stringWidth(dayStr);
			gObj.fillRect(xPos + font.stringWidth(monthStr), yPos, boxWidth, boxHeight);
		}
		else if(selectionMode == YEAR)
		{
			boxWidth = font.stringWidth(yearStr);
			gObj.fillRect(xPos + font.stringWidth(monthStr) + font.stringWidth(dayStr), yPos, boxWidth, boxHeight);
		}
		else if(selectionMode == NONE)
		{
			boxWidth = font.stringWidth(NO_DATE_STR);
			gObj.fillRect(xPos + font.stringWidth(monthStr) + font.stringWidth(dayStr) + font.stringWidth(yearStr) + font.stringWidth("  ") , yPos, boxWidth, boxHeight);
		}
		gObj.setColor(prevColor);
	}
	
	/*
	 * Since the Calendar class starts January at 0, 1 must be subtracted from
	 * the current month to ensure consistency.
	 */
	private boolean validDate()
	{
		boolean valid = false;
		boolean awkMonth = (currMonth == Calendar.APRIL || currMonth == Calendar.JUNE || currMonth == Calendar.SEPTEMBER || currMonth == Calendar.NOVEMBER);
		boolean boundsMonth = ( currMonth > Calendar.DECEMBER ) || ( currMonth < Calendar.JANUARY );
		
		int awkDays = MAX_DAY - 1;
		int febDays = MAX_DAY - 3;
		if(isLeapYear())
			 febDays = MAX_DAY - 2;
		boolean boundsDay = (currDay > MAX_DAY || currDay < MIN_DAY || (awkMonth && (currDay > awkDays)) || ( currMonth == Calendar.FEBRUARY && currDay > febDays) );
		boolean boundsYear = (currYear > MAX_YEAR || currYear < MIN_YEAR);
		//Check for the obvious out of bounds errors
		if(!boundsMonth && !boundsDay && !boundsYear)
			valid = true;		
		return valid;
	}
	
	/*
	 * Message to paint to screen in the case of an invalid date.
	 */
	private void invalidDate()
	{
		//Only alert the user of an invalid date when he or she switches between date fields
		if(prevSelectionMode != selectionMode)
		{
			String badDate = "Invalid Date!";
			gObj.setColor(0x00FF0000);
			gObj.drawString(badDate, width/4, height/3 - 10, Graphics.TOP|Graphics.LEFT);
			System.err.println("Invalid Date!");
			fixDate();
		}				
	}
	/*
	 * Fixes the date when the user goes under or over
	 */
	private void fixDate()
	{
		System.err.println("Date=" + currMonth + "/" + currDay + "/" + currYear);
		//if(selectionMode == MONTH)
		//{
			if(currMonth > Calendar.DECEMBER)
			{	
				int diff = currMonth - Calendar.DECEMBER;
				if(diff == 1)
					currMonth = Calendar.JANUARY;
				else
					currMonth = Calendar.DECEMBER;
			}
			else if(currMonth < Calendar.JANUARY)
			{
				int diff = currMonth - Calendar.JANUARY;
				if(diff == -1)
					currMonth = Calendar.DECEMBER;
				else					
					currMonth = Calendar.JANUARY;
			}
		//}
		//else if(selectionMode == DAY)
		//{
			/*
			 * Months with 30 days.
			 */
			boolean awkMonth = (currMonth == Calendar.APRIL || currMonth == Calendar.JUNE || currMonth == Calendar.SEPTEMBER || currMonth == Calendar.NOVEMBER); 
			if(awkMonth)
			{
				int lessDays = MAX_DAY - 1;
				if(currDay > lessDays)
				{
					int diff = currDay - lessDays;
					if(diff == 1)
						currDay = MIN_DAY;
					else
						currDay = lessDays;
				}
				else if(currDay < MIN_DAY)
				{
					int diff = currDay - MIN_DAY;
					if(diff == -1)
						currDay = lessDays;
					else
						currDay = MIN_DAY;
				}
			}
			else if(currMonth == Calendar.FEBRUARY)
			{
				int febDays = MAX_DAY - 3;
				if(isLeapYear())
					febDays = MAX_DAY - 2;
				if(currDay > febDays)
				{
					int diff = currDay - febDays;
					if(diff == 1)
						currDay = MIN_DAY;
					else
						currDay = febDays;
				}
				else if(currDay < MIN_DAY)
				{
					int diff = currDay - MIN_DAY;
					if(diff == -1)
						currDay = febDays;
					else
						currDay = MIN_DAY;
				}
			}
			//Months with 31 days
			else if(currDay > MAX_DAY)
			{
				int diff = currDay - MAX_DAY;
				if(diff == 1)
					currDay = MIN_DAY;
				else
					currDay = MAX_DAY;
			}
			else if(currDay < MIN_DAY)
			{
				int diff = currDay - MIN_DAY;
				if(diff == -1)
					currDay = MAX_DAY;
				else
					currDay = MIN_DAY;
			}				
		//}
		//else if(selectionMode == YEAR)
		//{
			if(currYear > MAX_YEAR)
			{
				int diff = currYear - MAX_YEAR;
				if(diff == 1)
					currYear = MIN_YEAR;
				else
					currYear = MAX_YEAR;
			}				
			else if(currYear < MIN_YEAR)
			{
				int diff = currYear - MIN_YEAR;
				if(diff == -1)
					currYear = MAX_YEAR;
				else
					currYear = MIN_YEAR;
			}
		//}
	}
}
