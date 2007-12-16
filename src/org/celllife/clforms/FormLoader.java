/*
 * VisualMIDlet.java
 *
 * Created on 2007/10/24, 02:02:09
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.celllife.clforms;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

/**
 * @author Munier
 */
public class FormLoader extends TextBox implements CommandListener
{

    private Command CMD_BACK;
    private Command CMD_OK;
    private TransportShell mainShell;

    public FormLoader(TransportShell mainShell)
    {
        super("Enter Form Number :", null, 100, TextField.NUMERIC);
        this.setTicker(new Ticker("Enter Form Number to Load : "));
        this.CMD_OK = new Command("Ok", Command.SCREEN, 1);
        this.CMD_BACK = new Command("Back", Command.BACK, 2);
        this.addCommand(this.CMD_OK);
        this.addCommand(this.CMD_BACK);
        this.setCommandListener(this);
        this.mainShell = mainShell;
        Display.getDisplay(mainShell).setCurrent(this);
    }

    public void commandAction(Command command,
                              Displayable displayable)
    {
        if (command == CMD_BACK)
        {
          this.mainShell.createView();  
        }
        else 
        if (command == CMD_OK)
        {
            this.mainShell.loadSpecificForm(Integer.parseInt(this.getString()));
        }
    }
}