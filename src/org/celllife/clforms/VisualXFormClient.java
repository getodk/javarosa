/*
 * GUIImageClient.java
 *
 * Created on 2007/10/24, 10:14:50
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.celllife.clforms;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.StringItem;
import org.celllife.clforms.storage.XFormRMSUtility;



public class VisualXFormClient implements CommandListener
{

    
    private final Command CMD_BACK = new Command("Back", Command.BACK, 2);
    
    private final Command CMD_FIND_SERVICES = new Command("Find", Command.OK, 1);
    
    private final Command CMD_CANCEL_FIND = new Command("Cancel", Command.BACK, 2);
    
    private final Command CMD_CLIENT_MAIN = new Command("Back", Command.BACK, 2);
    
    private final Command CMD_XFORMS_LOAD = new Command("Load", Command.OK, 1);
    
    private final Command CMD_CANCEL_LOAD = new Command("Cancel", Command.BACK, 2);
    
    private final Command CMD_SHOW_BACK = new Command("Back", Command.BACK, 2);
    
    private final Form clientMainScreen = new Form("Main Screen");
    
    private final List listScreen = new List("Available XForms", List.IMPLICIT);
    
       
    private TransportShell mainShell;
    
    private XFormClientBT clientBT;

    
    VisualXFormClient(TransportShell mainShell)
    {
        this.mainShell = mainShell;
        clientBT = new XFormClientBT(this);
        clientMainScreen.addCommand(CMD_BACK);
        clientMainScreen.addCommand(CMD_FIND_SERVICES);
        clientMainScreen.setCommandListener(this);
        listScreen.addCommand(CMD_CLIENT_MAIN);
        listScreen.addCommand(CMD_XFORMS_LOAD);
        listScreen.setCommandListener(this);
    }

   
    public void commandAction(Command c, Displayable d)
    {
        // back to main shell
        if (c == CMD_BACK)
        {
            destroy();
            mainShell.createView();
            return;
        }

        // XForm, device/services search
        if (c == CMD_FIND_SERVICES)
        {
            Form tempForm = new Form("Searching for available services ...");
            tempForm.addCommand(CMD_CANCEL_FIND);
            tempForm.setCommandListener(this);
            tempForm.append(new Gauge("Searching XForms...", false, Gauge.INDEFINITE, Gauge.CONTINUOUS_RUNNING));
            Display.getDisplay(mainShell).setCurrent(tempForm);
            clientBT.requestSearch();
            return;
        }

        // cancels device/services search
        if (c == CMD_CANCEL_FIND)
        {
            clientBT.cancelSearch();
            Display.getDisplay(mainShell).setCurrent(clientMainScreen);
            return;
        }

        // back to client main screen
        if (c == CMD_CLIENT_MAIN)
        {
            clientBT.requestLoad(null);
            Display.getDisplay(mainShell).setCurrent(clientMainScreen);
            return;
        }

        
        if (c == CMD_XFORMS_LOAD)
        {
            Form tempForm = new Form("Loading...");
            tempForm.addCommand(CMD_CANCEL_LOAD);
            tempForm.setCommandListener(this);
            tempForm.append(new Gauge("Loading XForm...", false, Gauge.INDEFINITE, Gauge.CONTINUOUS_RUNNING));
            Display.getDisplay(mainShell).setCurrent(tempForm);

            List xformList = (List) d;
            clientBT.requestLoad(xformList.getString(xformList.getSelectedIndex()));

            return;
        }

        // cancels loading
        if (c == CMD_CANCEL_LOAD)
        {
            clientBT.cancelLoad();
            Display.getDisplay(mainShell).setCurrent(listScreen);

            return;
        }

        // back to main shell
        if (c == CMD_SHOW_BACK)
        {
            Display.getDisplay(mainShell).setCurrent(listScreen);

            return;
        }
    }

    void initialise(boolean isReady)
    {
        // bluetooth intialised successfully
        if (isReady)
        {
            StringItem message = 
                    new StringItem("Bluetooth successfully initialised.", null);
            message.setLayout(StringItem.LAYOUT_CENTER | StringItem.LAYOUT_VCENTER);
            clientMainScreen.append(message);
            Display.getDisplay(mainShell).setCurrent(clientMainScreen);
            return;
        }

        // error
        Alert alert = 
                new Alert("Error", 
                "Error in initialising bluetooth device.", null, AlertType.ERROR);
        alert.setTimeout(TransportShell.ALERT_TIMEOUT);
        Display.getDisplay(mainShell).setCurrent(alert, mainShell.getDisplayable());
    }

    void destroy()
    {
        clientBT.destroy();
    }

  
    void showSearchError(String message)
    {
        Alert alert = new Alert("Error", message, null, AlertType.ERROR);
        alert.setTimeout(TransportShell.ALERT_TIMEOUT);
        Display.getDisplay(mainShell).setCurrent(alert, clientMainScreen);
    }


    void showLoadError(String message)
    {
        Alert alert = new Alert("Error", message, null, AlertType.ERROR);
        alert.setTimeout(TransportShell.ALERT_TIMEOUT);
        Display.getDisplay(mainShell).setCurrent(alert, listScreen);
    }


    void writeDownloadedXFormToRMS(org.celllife.clforms.api.Form form)
    {
        XFormRMSUtility rms = new XFormRMSUtility(Controller.XFORM_RMS);
        System.out.println("RMS SIZE BEFORE : " + rms.getNumberOfRecords());
        rms.writeToRMS(form);
        System.out.println("RMS SIZE AFTER : " + rms.getNumberOfRecords());
        this.mainShell.createView();
    }

   
    boolean showXFormNames(Hashtable base)
    {
        Enumeration keys = base.keys();
        
        if (!keys.hasMoreElements())
        {
            showSearchError("No XForms found.");
            return false;
        }

        while (listScreen.size() != 0)
        {
            listScreen.delete(0);
        }

        while (keys.hasMoreElements())
        {
            listScreen.append((String) keys.nextElement(), null);
        }

        Display.getDisplay(mainShell).setCurrent(listScreen);

        return true;
    }
} 