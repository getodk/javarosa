package org.dimagi.chatscreen;

import java.util.Vector;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import org.dimagi.entity.Question;
import org.dimagi.utils.StringUtils;
import org.dimagi.utils.ViewUtils;
import org.dimagi.view.Component;
import org.dimagi.view.Widget;
import org.dimagi.view.widget.ChoiceList;

public class Frame extends Component {
	
	private Question _question;
	
	private boolean _small;
	
	private String _text = "";
	
	private int _labelWidth;
	
	private int _xBufferSize;
	
	private int _yBufferSize;
	
	private Widget _theWidget;
	
	public Frame(Question theQuestion) {
		_question = theQuestion;
		setText();
		setupWidget();
	}
	
	public void setDrawingModeSmall(boolean small) {
		_small = small;
		setText();
		if(_small) {
			setBackgroundColor(ViewUtils.LIGHT_GREY);
			_theWidget.setVisible(false);
		}
		else {
			setBackgroundColor(ViewUtils.WHITE);
			_theWidget.setVisible(true);
		}
		sizeFrame();
	}
	
	private void setText() {
		if(_small) {
			_text = _question.getShortText();
		}
		else {
			_text = _question.getLongText();
		}
	}
	
	private void setupWidget() {
		switch(_question.getWidgetType()){
		case(Constants.SINGLE_CHOICE):
			ChoiceList newWidget = new ChoiceList();
			newWidget.setChoiceType(ChoiceList.SINGLE);
			
			for(int i =0 ; i < _question.getInternalArray().length ; ++i) {
				newWidget.addChoice(_question.getInternalArray()[i]);
			}
			
			_theWidget = newWidget;
			break;
		case(Constants.MULTIPLE_CHOICE):
			ChoiceList aWidget = new ChoiceList();
			aWidget.setChoiceType(ChoiceList.MULTI);
			
			for(int i =0 ; i < _question.getInternalArray().length ; ++i) {
				aWidget.addChoice(_question.getInternalArray()[i]);
			}
			
			_theWidget = aWidget;
			break;
		}
		this.add(_theWidget);
	}
	
	public void sizeFrame() {
		Font theFont = Font.getDefaultFont();
		_xBufferSize = this.getWidth()/10;
		_yBufferSize = _xBufferSize/2;
		
		if(_small) {
			_labelWidth = this.getWidth() - _xBufferSize;
		}
		else {
			_labelWidth = this.getWidth()/3 - _xBufferSize;
		}
		
		Vector splitStrings = StringUtils.splitStringByWords(_text, _labelWidth, theFont);
		
		int numLines = splitStrings.size();
		
		int labelHeight = (theFont.getHeight() * numLines) + _yBufferSize;
		
		_theWidget.setWidth(this.getWidth() - _labelWidth - _xBufferSize);
		
		_theWidget.sizeWidget();
		
		_theWidget.setX(getWidth() - _theWidget.getWidth());
		_theWidget.setY(0);
		
		if(_theWidget.getHeight() < labelHeight || _small) {
			this.setHeight(labelHeight);	
			_theWidget.setHeight(labelHeight);
		}
		else {
			this.setHeight(_theWidget.getHeight());
		}
	}
	
	/**
	 * Draws the frame object onto the graphics canvas starting at the height given, and 
	 * returns the total amount of height used
	 * @param g 
	 * @param startingHeight 
	 * @return
	 */
	public void drawInternal(Graphics g) {
		
		Font theFont = g.getFont();
		
		Vector splitStrings;
		
		splitStrings = StringUtils.splitStringByWords(_text, _labelWidth, theFont);
		
		g.setColor(ViewUtils.BLACK);
		g.drawRect(0, 0, this.getWidth(), this.getHeight());
		
		for(int i = 0; i < splitStrings.size(); ++i) {
			String stringPiece = (String)splitStrings.elementAt(i);
			g.drawString(stringPiece,_xBufferSize/2 ,
					_yBufferSize/2 + theFont.getHeight()*(i),
					Graphics.TOP|Graphics.LEFT);
		}
	}
}
