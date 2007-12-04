package org.dimagi.utils;

import java.util.Vector;

import javax.microedition.lcdui.Font;

/**
 * A Utility class of various static string methods
 * 
 * @author ctsims
 * @date Aug-08-2007
 */
public class StringUtils {

	//TODO: Set this up to use String[] instead of Vectors for return
	
	/**
	 * Splits a string into a set of smaller strings, displaying the maximum number of letters
	 * possible per line.
	 * 
	 * @param theString The string to be split up
	 * @param totalWidth The width permitted per line
	 * @param theFont The font that will be used to display the string
	 * @return A Vector containing the original string split into an array of strings all of which
	 * are less than totalWidth when displayed with theFont.
	 */
	public static Vector splitStringByLetters(String theString, int totalWidth, Font theFont) {
		int numLines = 1;
		
		String currentString = "";
		
		Vector splitStrings = new Vector();
		
		for(int i = 0 ; i < theString.length() ; ++i) {
			//We need a line break
			if(theFont.stringWidth(currentString+theString.charAt(i)) > totalWidth) {
				splitStrings.addElement(currentString);
				currentString = ""+theString.charAt(i); 
				numLines++;
			}
			else {
				currentString += theString.charAt(i);
			}
		}
		splitStrings.addElement(currentString);
		
		return splitStrings;
	}
	
	/**
	 * Splits a string into a set of smaller strings, displaying the maximum number of full words
	 * possible per line.
	 * 
	 * @param theString The string to be split up
	 * @param totalWidth The width permitted per line
	 * @param theFont The font that will be used to display the string
	 * @return A Vector containing the original string split into an array of strings all of which
	 * are less than totalWidth when displayed with theFont.
	 */
	public static Vector splitStringByWords(String theString, int totalWidth, Font theFont) {
		
		String currentString = "";
		
		Vector splitStrings = new Vector();
		
		String[] words = de.enough.polish.util.TextUtil.split(theString,' ');
		
		for(int i = 0 ; i < words.length ; ++i) {
			//We need a line break
			if(theFont.stringWidth(currentString+words[i]) > totalWidth) {
				splitStrings.addElement(currentString);
				currentString = words[i] + " "; 
			}
			else {
				currentString += words[i] + " ";
			}
		}
		splitStrings.addElement(currentString);
		
		return splitStrings;
	}
}
