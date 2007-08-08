package org.dimagi.chatscreen;

import java.util.Vector;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import org.dimagi.entity.Question;
import org.dimagi.utils.ViewUtils;
import org.dimagi.view.Component;
import org.dimagi.view.NavBar;

import de.enough.polish.util.VectorIterator;

public class ChatScreenCanvas extends Canvas {
	
	Component canvasComponent;
	
	Vector frameSet = new Vector();
	NavBar theNavBar = new NavBar();
	
	public ChatScreenCanvas() {
		setupComponents();
	}
	
	private void setupComponents() {
		
		canvasComponent = new Component();
		
		int width = this.getWidth();
		int height = this.getHeight();
		
		canvasComponent.setWidth(width);
		canvasComponent.setHeight(height);
		
		int frameCanvasHeight = height - (height/11);
		
		theNavBar.setBackgroundColor(ViewUtils.DARK_GREY);
		
		theNavBar.setX(0);
		
		theNavBar.setY(frameCanvasHeight);
		
		theNavBar.setWidth(width);
		
		theNavBar.setHeight(height/11);
		
		canvasComponent.add(theNavBar);
		canvasComponent.setBackgroundColor(ViewUtils.GREY);
	}

	public void addQuestion(Question theQuestion) {
		Frame newFrame = new Frame(theQuestion);
		newFrame.setWidth(this.getWidth());
		frameSet.insertElementAt(newFrame, 0);
		
		canvasComponent.add(newFrame);
		
		setupFrames();
		this.repaint();
	}
	
	private void setupFrames() {
		VectorIterator iter = new VectorIterator(frameSet);
		
		int frameCanvasHeight = canvasComponent.getHeight() - (canvasComponent.getHeight()/11);
		
		int frameStart = frameCanvasHeight;
		
		while(iter.hasNext()) {
			Frame aFrame = (Frame)iter.next();
			if(aFrame == frameSet.firstElement()) {
				aFrame.setDrawingModeSmall(false);
			}
			else {
				aFrame.setDrawingModeSmall(true);
			}
			aFrame.setY(frameStart);
			aFrame.sizeFrame();
			frameStart = frameStart - aFrame.getHeight();
		}
	}
	
	protected void paint(Graphics g) {
		canvasComponent.draw(g);
	}
}