package org.dimagi.polishforms;

import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import de.enough.polish.ui.ChoiceGroup;

public class ScreenMidlet extends MIDlet {

    Screen mainScreen;
    //List itemList;
    
    protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
        // TODO Auto-generated method stub

    }

    protected void pauseApp() {
        // TODO Auto-generated method stub

    }

    protected void startApp() throws MIDletStateChangeException {
        mainScreen  = new Screen();        
        Display.getDisplay(this).setCurrent(mainScreen);
    }

}
