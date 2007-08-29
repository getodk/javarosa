package org.dimagi.chatscreen;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;
import de.esoco.ewt.UserInterfaceContext;
import org.dimagi.entity.Question;

public class ChatScreenMIDlet extends MIDlet {

	protected void startApp() throws MIDletStateChangeException {
		ChatScreenForm chatScreenCanvas = new ChatScreenForm();
		//myNewForm.insert(0, chatScreenCanvas);
		
		Display.getDisplay(this).setCurrent(chatScreenCanvas);
		Display rDisplay = Display.getDisplay(this);
		UserInterfaceContext aContext = new UserInterfaceContext(rDisplay);

	}

	protected void pauseApp() {
		// TODO Auto-generated method stub

	}

	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
		// TODO Auto-generated method stub

	}

}
