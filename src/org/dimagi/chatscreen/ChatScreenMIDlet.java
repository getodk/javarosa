package org.dimagi.chatscreen;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;


public class ChatScreenMIDlet extends MIDlet implements CommandListener {

	ChatScreenForm chatScreenCanvas;
	private Command nextCommand;
	private Command prevCommand;
	private Command exitCommand;
	
	protected void startApp() throws MIDletStateChangeException {
		chatScreenCanvas = new ChatScreenForm();
		prevCommand = new Command("Prev", Command.SCREEN, 1);
		nextCommand = new Command("Next", Command.SCREEN, 2);
		exitCommand = new Command("Exit", Command.EXIT, 3);
		chatScreenCanvas.addCommand(nextCommand);
		chatScreenCanvas.addCommand(prevCommand);
		chatScreenCanvas.addCommand(exitCommand);
		chatScreenCanvas.setCommandListener(this);
		Display.getDisplay(this).setCurrent(chatScreenCanvas);
	}

	protected void pauseApp() {
		// TODO Auto-generated method stub
	}

	protected void destroyApp(boolean arg0) {
		// TODO Auto-generated method stub
	}
	
	public void commandAction(Command c, Displayable d) {
		if ( c == nextCommand ) {
			chatScreenCanvas.goToNextQuestion();
		} else if ( c == prevCommand ) {
			chatScreenCanvas.goToPreviousQuestion();
		} else if ( c == exitCommand ) {
			destroyApp(false);
			notifyDestroyed();
		}
	}

}
