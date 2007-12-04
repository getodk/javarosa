package org.dimagi.utils;


/**
 * A utility class containing various methods and constants helpful for the DiMEC 
 * view classes.
 * 
 * @author ctsims
 * @date Aug-07-2007
 *
 */
public class ViewUtils {
	
	public static int BLACK = 0x00000000;
	
	public static int WHITE = 0x00FFFFFF;
	
	public static int GREY = 0x00666666;
	
	public static int LIGHT_GREY = 0x00999999;
	
	public static int DARK_GREY = 0x00BBBBBB;
	
	public static int PINK_GREY = 0x00776666;
	
	public static int TRANSPARENT = 0xFF000000;
	
	/**
	 * Identifies whether the point given falls within the rectangle defined by
	 * the given X,Y, width, and height.
	 * 
	 * @param pointX
	 * @param pointY
	 * @param rectX
	 * @param rectY
	 * @param rectW
	 * @param rectH
	 * @return true if the point falls within the given rectangle, false otherwise
	 */
	public static boolean checkPointInRectangle(int pointX, int pointY, int rectX, 
			int rectY, int rectW, int rectH) {
		if(pointX >= rectX && 
		   pointX <= (rectX + rectW) &&
		   pointY >= rectY &&
		   pointY <= (rectY + rectH)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Returns a size as close as possible to the inputted size that will
	 * accomodate a buffer of equal size on either side of a space half of
	 * the size of the input. 
	 * 
	 * This method is intended to be used to size checkboxes/radiobuttons to
	 * allow for a marker in the middle of a square or circle that is centered.
	 * 
	 * @param input the size to be modified
	 * @return If impossible, the input, otherwise an integer size that allows for 
	 * equal buffers while being as close to the input as possible.
	 */
	public static int findAcceptableBoxSize(int input) {
		int retNum = input;
		int augment = -1;
		while(!sizeAcceptable(retNum)) {
			retNum = input + augment;
			if(retNum < 0) {
				retNum = 0;
			}
			if(augment < 0 ) {
				augment += 2*(-augment);
			}
			else {
				augment = (augment - 2*augment) -1;
			}
		}
		if(retNum == 0 ) {
			return input;
		}
		else {
			return retNum;
		}
	}
	
	/**
	 * Checks whether the input can be divided by 2 twice, and have an even result
	 * 
	 * @param input the integer to be checked
	 * @return True if input/4 is even. False otherwise.
	 */
	private static boolean sizeAcceptable(int input) {
		int remains  = input % 2;
		int doubled = (input/2) % 2;
		int quad = (input/4) % 2;
		if(remains == 0 && doubled == 0 && quad == 0) {
			return true;
		}
		else return false;
	}

}
