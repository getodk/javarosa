package org.dimagi.view;

import javax.microedition.lcdui.Graphics;

import org.dimagi.utils.ViewUtils;

public class DButton extends Component {
	
	private String _text;
	
	public DButton(String text) {
		_text = text;
	}
	

	public void drawInternal(Graphics g) {
		//Finish by drawing the border	
		g.setColor(ViewUtils.BLACK);
		int buffer = g.getFont().getHeight()/3;
		
		int textY = this.getHeight()/2 - g.getFont().getHeight()/2; 
		
		g.drawString(_text,this.getWidth()/2,textY,Graphics.TOP |Graphics.HCENTER);
		
		//Finish by drawing the border
		g.setColor(ViewUtils.BLACK);
		g.drawRect(0, 0, _width, _height);
	}
}
