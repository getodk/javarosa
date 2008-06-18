package org.javarosa.chatscreen;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import org.javarosa.clforms.api.Prompt;
import org.javarosa.utils.StringUtils;
import org.javarosa.utils.ViewUtils;
import org.javarosa.view.Component;
import org.javarosa.view.Widget;
import org.javarosa.view.widget.ChoiceList;
import org.javarosa.view.widget.Dropdown;
import org.javarosa.view.widget.Textbox;


/**
 * The Frame component is the basic element of the Chat Screen
 * interface. It displays a Question in the optimal fashion
 * based on its text and widget type.
 * 
 * A Frame can be considered the UI equivilant of a Question object.
 * 
 * @author ctsims
 * @date Aug-08-2007
 *
 */
public class Frame extends Component {
	
	private Prompt _prompt;
	
	private boolean _isActiveFrame = true;
	
	private String _text = "";
	
	private int _labelWidth;
	
	private int _xBufferSize;
	
	private int _yBufferSize;
	
	private Widget _theWidget;
	
	/**
	 * Creates a new Frame for the given Question.
	 * @param theQuestion The question that this frame will represent.	
	 */
	public Frame(Prompt thePrompt) {
		_prompt = thePrompt;
		setText();
		setupWidget();
	}
	
	/**
	 * Sets the drawing mode of the frame to either large or small
	 * @param small True if the frame should be drawn in its small form, false otherwise.
	 */
	public void setActiveFrame(boolean isActiveFrame) {
		_isActiveFrame = isActiveFrame;
		setText();
		if(isActiveFrame) {
			setBackgroundColor(ViewUtils.WHITE);
			_theWidget.setActiveWidget(true);
		} else {
			setBackgroundColor(ViewUtils.LIGHT_GREY);
			_theWidget.setActiveWidget(false);
		}
		sizeFrame();
	}
	
	/**
	 * Sets the text of the widget to the proper field of the Question
	 */
	private void setText() {
		if(_isActiveFrame) {
			_text = _prompt.getLongText();
		}
		else {
			_text = _prompt.getShortText();
		}
	}
	
	/**
	 * Chooses the Frame's widget, based on what input type the 
	 * question requires.
	 *
	 */
	private void setupWidget() {
		Enumeration itr;
		switch(_prompt.getFormControlType()) {
		case ( org.javarosa.clforms.api.Constants.SELECT1 ):
			ChoiceList newWidget = new ChoiceList();
			newWidget.setChoiceType(ChoiceList.SINGLE);
			itr = _prompt.getSelectMap().keys();
            while (itr.hasMoreElements()) {
            	String label = (String) itr.nextElement();
            	newWidget.addChoice(label);
            }
			_theWidget = newWidget;
			break;
		case( org.javarosa.clforms.api.Constants.SELECT ):
			ChoiceList aWidget = new ChoiceList();
			aWidget.setChoiceType(ChoiceList.MULTI);
			itr = _prompt.getSelectMap().keys();
            while (itr.hasMoreElements()) {
            	String label = (String) itr.nextElement();
            	aWidget.addChoice(label);
            }
						
			_theWidget = aWidget;
			break;
		case ( org.javarosa.clforms.api.Constants.TEXTBOX ):
			_theWidget = new Textbox();
			break;
		case (Constants.DROPDOWN):
			Dropdown dropdownWidget = new Dropdown();
			itr = _prompt.getSelectMap().keys();
			while (itr.hasMoreElements()) {
				String label = (String) itr.nextElement();
				dropdownWidget.addChoice(label);
			}
			_theWidget = dropdownWidget;
			break;
		}
		_theWidget.setLabelPosition(_prompt.getLabelPosition());
		this.add(_theWidget);
	}
	
	/**
	 * Lays out the internal elements of the form to optimal sizes. Also sets the height 
	 * of the frame to the correct size, allowing both the widget and the Label to be 
	 * seen in full.
	 */
	public void sizeFrame() {
		Font theFont = Font.getDefaultFont();
		_xBufferSize = this.getWidth()/10;
		_yBufferSize = _xBufferSize/2;
		
		if(!_isActiveFrame) {
			_labelWidth = this.getWidth() - _xBufferSize;
		}
		else {
			if ( _theWidget.getLabelPosition() == Constants.LABEL_TOP ) {
				_labelWidth = this.getWidth();
			} else {
				_labelWidth = this.getWidth()/3 - _xBufferSize;
			}
		}
		
		Vector splitStrings = StringUtils.splitStringByWords(_text, _labelWidth, theFont);
		
		int numLines = splitStrings.size();
		
		int labelHeight = (theFont.getHeight() * numLines) + _yBufferSize;

		_theWidget.sizeWidget();
			
		if ( _theWidget.getLabelPosition() == Constants.LABEL_TOP ) {
			_theWidget.setWidth(this.getWidth() - _xBufferSize);
			_theWidget.setX(_xBufferSize/2);
			_theWidget.setY(labelHeight);
		} else {
			_theWidget.setWidth(this.getWidth() - _labelWidth - _xBufferSize); 
			_theWidget.setX(getWidth() - _theWidget.getWidth()); 
	        _theWidget.setY(0); 
	    }
			
		if(_theWidget.getHeight() < labelHeight || !_isActiveFrame) {
			this.setHeight(labelHeight + _yBufferSize);	
			_theWidget.setHeight(labelHeight + _yBufferSize);
		}
		else {
			if ( _theWidget.getLabelPosition() == Constants.LABEL_TOP)
				this.setHeight(_theWidget.getHeight() + labelHeight + _yBufferSize);
			else
				this.setHeight(_theWidget.getHeight()); 
		}
	}
	
	/**
	 * Manually splits the string that will be displayed, and draws it to the
	 * proper place in the frame
	 * @param g the graphic canvas
	 */
	public void drawInternal(Graphics g) {
		
		Vector splitStrings;
		
		// draw border
		g.setColor(ViewUtils.BLACK);
		g.drawRect(0, 0, this.getWidth(), this.getHeight());
		
		// draw string
		Font theFont = g.getFont();
		splitStrings = StringUtils.splitStringByWords(_text, _labelWidth, theFont);
		for(int i = 0; i < splitStrings.size(); ++i) {
			String stringPiece = (String)splitStrings.elementAt(i);
			g.drawString(stringPiece,
						_xBufferSize/2,
 	                	_yBufferSize/2 + theFont.getHeight()*(i),
 	                	Graphics.TOP|Graphics.LEFT);
		}
	}
	
}