package org.dimagi.view;

import javax.microedition.lcdui.Graphics;

import org.dimagi.utils.ViewUtils;

public class DProgressBar extends Component {

	int _progress;
	int _capacity;
	
	public DProgressBar(int capacity) {
		_capacity = capacity;
	}
	
	public void setProgress(int progress) {
		_progress = progress;
		refresh();
	}
	
	public void setCapacity(int capacity) {
		_capacity = capacity;
		refresh();
	}

	public void drawInternal(Graphics g) {
		//Draw the Background
		g.setColor(ViewUtils.LIGHT_GREY);
		g.fillRect(0, 0, _width, _height);
		
		//Draw the amount filled
		int filledWidth = (_width*_progress)/_capacity;
		
		g.setColor(ViewUtils.PINK_GREY);
		g.fillRect(0,0,filledWidth,_height);
		
		//Finish by drawing the border
		g.setColor(ViewUtils.BLACK);
		g.drawRect(0, 0, _width, _height);
	}
	
	public void refresh() {
		
	}

}
