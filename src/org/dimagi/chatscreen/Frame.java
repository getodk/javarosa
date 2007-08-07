package org.dimagi.chatscreen;

import java.util.Vector;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import org.dimagi.entity.Question;
import org.dimagi.utils.StringUtils;

public class Frame{
	
	private Question _question;
	
	private boolean _small;
	private int _y;
	
	public Frame(Question theQuestion) {
		_question = theQuestion;
	}
	
	public void setDrawingModeSmall(boolean small) {
		_small = small;
	}
	
	public void setPosition(int y) {
		_y = y;
	}
	
	/**
	 * Draws the frame object onto the graphics canvas starting at the height given, and 
	 * returns the total amount of height used
	 * @param g 
	 * @param startingHeight 
	 * @return
	 */
	public int drawFrameOntoGraphics(Graphics g) {
		int xBufferSize = g.getClipWidth()/10;
		int yBufferSize = xBufferSize/2;
		Font theFont = g.getFont();
		
		int labelWidth = g.getClipWidth()/2 - xBufferSize;
		
		Vector splitStrings;
		
		String text = "";
		int backColor = 0x00FFFFFF;
		
		if(_small) {
			text = _question.getShortText();
			backColor = 0x00999999;
			labelWidth = g.getClipWidth() - xBufferSize;
		}
		else {
			text = _question.getLongText();
			backColor = 0x00FFFFFF;
			labelWidth = g.getClipWidth()/2 - xBufferSize;
		}
		
		splitStrings = StringUtils.splitStringByWords(text, labelWidth, theFont);
		
		int numLines = splitStrings.size();
		
		int totalHeight = (theFont.getHeight() * numLines) + yBufferSize;
		
		g.setColor(backColor);
		g.fillRect(0, _y-totalHeight, g.getClipWidth(), totalHeight);
		g.setColor(0x00000000);
		g.drawRect(0, _y-totalHeight, g.getClipWidth(), totalHeight);
		
		for(int i = 0; i < splitStrings.size(); ++i) {
			String stringPiece = (String)splitStrings.elementAt(i);
			g.drawString(stringPiece,xBufferSize/2 ,
					_y - theFont.getHeight()*(splitStrings.size()-i) - yBufferSize/2,
					Graphics.TOP|Graphics.LEFT);
		}
		
		return totalHeight;
	}
}
