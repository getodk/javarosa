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

/**
 * The base container for the Chat Screen interface.
 * 
 * The ChatScreenForm is responsible for containing and laying out frames,
 * as well as the control logic used for navigation of protocols.
 * 
 * It is the primary interface between the Protocol object and the View.
 * 
 * @author ctsims
 *
 */
public class ChatScreenForm extends DForm {

	//TODO: Add (...) objects to the top and bottom of the display to signal
	//that there are frames above or below the current view.
	
	Vector frameSet = new Vector();
	NavBar theNavBar = new NavBar();

	boolean a = false;

	/**
	 * Creates a new ChatScreen Form
	 */
	public ChatScreenForm() {
		setupComponents();

		Question first = new Question("What is the name of the child?",
				"Child Name", Constants.SINGLE_CHOICE, new String[] { "Terry", "Michael",
				"Samanatha" });
		addQuestion(first);
	}

	/**
	 * Captures and propogates keypress events.
	 */
	protected void keyPressed(int arg0) {
		if (!a) {
			Question second = new Question(
					"Is the child having any trouble breathing?",
					"Trouble Breathing", Constants.SINGLE_CHOICE, new String[] { "Yes", "No" });
			addQuestion(second);
			a=true;
		} else {
			Question third = new Question("How is the child's hearing?",
					"Hearing", Constants.MULTIPLE_CHOICE, new String[] { "Good", "Bad",
						"Getting Worse", "Abysmal" });
			addQuestion(third);
		}
	}

	/**
	 * Lays out the static components for the form
	 */
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

	/**
	 * Pushes a new question onto the stack, setting all other 
	 * questions to inactive status, and displaying the new question
	 * to the user.
	 * 
	 * @param theQuestion The new question to be displayed
	 */
	public void addQuestion(Question theQuestion) {
		Frame newFrame = new Frame(theQuestion);
		newFrame.setWidth(this.getWidth());
		frameSet.insertElementAt(newFrame, 0);

		getContentComponent().add(newFrame);

		setupFrames();
		this.repaint();
	}

	/**
	 * Queries all frames for their optimal size, and then lays them out
	 * in a simple stack.
	 */
	private void setupFrames() {
		VectorIterator iter = new VectorIterator(frameSet);

		int frameCanvasHeight = getContentComponent().getHeight()
				- (getContentComponent().getHeight() / 11);

		int frameStart = frameCanvasHeight;

		//TODO: Stop displaying frames once they're off the screen
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