package org.dimagi.chatscreen;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;

import org.dimagi.entity.Question;

public class ChatScreenForm extends MIDlet {

	protected void startApp() throws MIDletStateChangeException {
		Form myNewForm = new Form("Hello World");
		
		ChatScreenCanvas chatScreenCanvas = new ChatScreenCanvas();
		//myNewForm.insert(0, chatScreenCanvas);
		
		Display.getDisplay(this).setCurrent(chatScreenCanvas);
		
		chatScreenCanvas.addQuestion(new Question("What is the name of the child?","Child Name"));
		chatScreenCanvas.addQuestion(new Question("Is the child having any trouble breathing?","Trouble Breathing"));
		chatScreenCanvas.addQuestion(new Question("How is the child's hearing?","Hearing"));
	}

	protected void pauseApp() {
		// TODO Auto-generated method stub

	}

	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
		// TODO Auto-generated method stub

	}

}
