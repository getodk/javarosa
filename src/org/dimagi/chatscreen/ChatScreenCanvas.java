package org.dimagi.chatscreen;

import java.util.Vector;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;

import org.dimagi.entity.Question;
import org.dimagi.utils.ViewUtils;
import org.dimagi.view.Component;
import org.dimagi.view.IRefreshListener;
import org.dimagi.view.NavBar;

import de.enough.polish.util.VectorIterator;

public class ChatScreenCanvas extends DForm {

	Vector frameSet = new Vector();
	NavBar theNavBar = new NavBar();

	boolean a = false;

	public ChatScreenCanvas() {
		setupComponents();

		Question first = new Question("What is the name of the child?",
				"Child Name", Constants.SINGLE_CHOICE);
		first
				.setInternalArray(new String[] { "Terry", "Michael",
						"Samanatha" });
		addQuestion(first);
	}

	protected void keyPressed(int arg0) {
		if (!a) {
			Question second = new Question(
					"Is the child having any trouble breathing?",
					"Trouble Breathing", Constants.SINGLE_CHOICE);
			second.setInternalArray(new String[] { "Yes", "No" });
			addQuestion(second);
			a=true;
		} else {
			Question third = new Question("How is the child's hearing?",
					"Hearing", Constants.MULTIPLE_CHOICE);
			third.setInternalArray(new String[] { "Good", "Bad",
					"Getting Worse", "Abysmal" });
			addQuestion(third);
		}
	}

	private void setupComponents() {

		int width = this.getWidth();
		int height = this.getHeight();
		
		System.out.println(width + "," + height);

		int frameCanvasHeight = height - (height / 11);

		theNavBar.setBackgroundColor(ViewUtils.DARK_GREY);

		theNavBar.setX(0);

		theNavBar.setY(frameCanvasHeight);

		theNavBar.setWidth(width);

		theNavBar.setHeight(height / 11);

		this.getContentComponent().add(theNavBar);
		getContentComponent().setBackgroundColor(ViewUtils.GREY);
	}

	public void addQuestion(Question theQuestion) {
		Frame newFrame = new Frame(theQuestion);
		newFrame.setWidth(this.getWidth());
		frameSet.insertElementAt(newFrame, 0);

		getContentComponent().add(newFrame);

		setupFrames();
		this.repaint();
	}

	private void setupFrames() {
		VectorIterator iter = new VectorIterator(frameSet);

		int frameCanvasHeight = getContentComponent().getHeight()
				- (getContentComponent().getHeight() / 11);

		int frameStart = frameCanvasHeight;

		while (iter.hasNext()) {
			Frame aFrame = (Frame) iter.next();
			if (aFrame == frameSet.firstElement()) {
				aFrame.setDrawingModeSmall(false);
			} else {
				aFrame.setDrawingModeSmall(true);
			}
			frameStart -= aFrame.getHeight();
			aFrame.setY(frameStart);
		}
	}
}