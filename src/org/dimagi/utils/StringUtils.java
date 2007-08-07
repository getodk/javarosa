package org.dimagi.utils;

import java.util.Vector;

import javax.microedition.lcdui.Font;

import de.enough.polish.util.StringTokenizer;

public class StringUtils {

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
	
	public static Vector splitStringByWords(String theString, int totalWidth, Font theFont) {
		int numLines = 1;
		
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
