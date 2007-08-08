package org.dimagi.chatscreen;

import java.util.Vector;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import org.dimagi.entity.Question;
import org.dimagi.utils.StringUtils;
import org.dimagi.utils.ViewUtils;
import org.dimagi.view.Component;
import org.dimagi.view.ISizeChangeListener;

public class Frame extends Component {
	
	private Question _question;
	
	private boolean _small;
	
	private String _text = "";
	
	private int _backColor;
	
	private int _labelWidth;
	
	private int _xBufferSize;
	
	private int _yBufferSize;
	
	public Frame(Question theQuestion) {
		_question = theQuestion;
		setText();
	}
	
	public void setDrawingModeSmall(boolean small) {
		_small = small;
		setText();
		if(_small) {
			_backColor = ViewUtils.LIGHT_GREY;
			_labelWidth = this.getWidth() - _xBufferSize;
		}
		else {
			_backColor = ViewUtils.WHITE;
			_labelWidth = this.getWidth()/2 - _xBufferSize;
		}
	}
	
	private void setText() {
		if(_small) {
			_text = _question.getShortText();
		}
		else {
			_text = _question.getLongText();
		}
	}
	
	public void sizeFrame() {
		Font theFont = Font.getDefaultFont();
		_xBufferSize = this.getWidth()/10;
		_yBufferSize = _xBufferSize/2;
		
		int labelWidth = this.getWidth()/2 - _xBufferSize;
		
		Vector splitStrings = StringUtils.splitStringByWords(_text, labelWidth, theFont);
		
		int numLines = splitStrings.size();
		
		this.setHeight((theFont.getHeight() * numLines) + _yBufferSize);
	}
	
	/**
	 * Draws the frame object onto the graphics canvas starting at the height given, and 
	 * returns the total amount of height used
	 * @param g 
	 * @param startingHeight 
	 * @return
	 */
	public void draw(Graphics g) {
		Font theFont = g.getFont();
		
		int labelWidth = g.getClipWidth()/2 - _xBufferSize;
		
		Vector splitStrings;
		
		splitStrings = StringUtils.splitStringByWords(_text, labelWidth, theFont);
		
		int numLines = splitStrings.size();
		
		int totalHeight = (theFont.getHeight() * numLines) + _yBufferSize;
		
		g.setColor(_backColor);
		g.fillRect(0, _y-totalHeight, g.getClipWidth(), totalHeight);
		
		g.setColor(ViewUtils.BLACK);
		g.drawRect(0, _y-totalHeight, g.getClipWidth(), totalHeight);
		
		for(int i = 0; i < splitStrings.size(); ++i) {
			String stringPiece = (String)splitStrings.elementAt(i);
			g.drawString(stringPiece,_xBufferSize/2 ,
					_y - theFont.getHeight()*(splitStrings.size()-i) - _yBufferSize/2,
					Graphics.TOP|Graphics.LEFT);
		}
	}
}
