package org.dimagi.utils;

public class ViewUtils {
	
	public static int BLACK = 0x00000000;
	
	public static int WHITE = 0x00FFFFFF;
	
	public static int GREY = 0x00666666;
	
	public static int LIGHT_GREY = 0x00999999;
	
	public static int DARK_GREY = 0x00BBBBBB;
	
	public static int PINK_GREY = 0x00776666;
	
	public static int TRANSPARENT = 0xFF000000;
	
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
