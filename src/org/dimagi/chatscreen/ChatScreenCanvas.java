package org.dimagi.chatscreen;

import java.util.Vector;
import de.enough.polish.util.VectorIterator;
import java.util.Stack;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;

import org.dimagi.entity.Question;

public class ChatScreenCanvas extends Canvas {
	
	Vector frameSet = new Vector();

	static int BLACK = 0x00000000;
	
	static int WHITE = 0x00FFFFFF;
	static int GREY = 0x00666666;
	
	public void addQuestion(Question theQuestion) {
		frameSet.insertElementAt(new Frame(theQuestion), 0);
		this.repaint();
	}
	
	protected void paint(Graphics g) {
		g.setColor(GREY);
		g.fillRect(0,0,g.getClipWidth(), g.getClipHeight());
		
		g.setColor(BLACK);
		int width = g.getClipWidth();
		int height = g.getClipHeight();
		
		VectorIterator iter = new VectorIterator(frameSet);
		
		int frameStart = height;
		
		while(iter.hasNext()) {
			Frame aFrame = (Frame)iter.next();
			if(aFrame == frameSet.firstElement()) {
				aFrame.setDrawingModeSmall(false);
			}
			else {
				aFrame.setDrawingModeSmall(true);
			}
			aFrame.setPosition(frameStart);
			frameStart = frameStart - aFrame.drawFrameOntoGraphics(g);
		}	
	}
}