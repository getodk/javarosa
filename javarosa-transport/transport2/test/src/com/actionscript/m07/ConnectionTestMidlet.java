package com.actionscript.m07;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

/**
 * This MIDlet permits to test Different type of Connection available in a device
 *
 * @author  Massimo Carli
 * @version
 */
public class ConnectionTestMidlet extends MIDlet implements CommandListener,ConnectionThreadListener {
    
    // Commands
    private Command exitCommand     = new Command("Exit",Command.EXIT,0);
    private Command backCommand     = new Command("Back",Command.BACK,0);
    private Command connectCommand  = new Command("Connect",Command.OK,0);
    
    // Input form
    private Form inputForm;
    
    // URI Input
    private TextField uriInputText;
    
    // TextBox for output
    private TextBox output;
    
    /**
     * Initialize GUI
     */
    public void startApp() {
        // Show form
        Display.getDisplay(this).setCurrent(getInputForm());
    }
    
    public void pauseApp() {
    }
    
    public void destroyApp(boolean unconditional) {
    }
    
    public void commandAction(Command command, Displayable displayable) {
        // With the exit command we exit
        if (command == exitCommand) {
            Display.getDisplay(this).setCurrent(null);
            destroyApp(true);
            notifyDestroyed();
        }else if (command == connectCommand){
            // Here we have to get URI to connect to
            String uri = uriInputText.getString();
            if((uri!=null) && !("".equals(uri))){
                // We create the ConnectionThread
                ConnectionThread thread = new ConnectionThread(uri);
                thread.setConnectionListener(this);
                thread.start();
            }
        }else if (command == backCommand){
            // Show the input form
            Display.getDisplay(this).setCurrent(getInputForm());
        } 
    }
    
    /**
     * Callback method for Connection error notification
     *@param errorMessage Message for the error
     */
    public void notifyError(String errorMessage) {
        // Create an alert
        Alert errorAlert = new Alert("Connection Error",errorMessage,null,AlertType.ERROR);
        // Show it
        Display.getDisplay(this).setCurrent(errorAlert,getInputForm());
    }

    /**
     * Callback method for Connection data
     *@param data Data read from the Connection
     */    
    public void notifyData(String data) {
        // We set the data into output
        getOutput().setString(data);
        // Show it
        Display.getDisplay(this).setCurrent(getOutput());
    }    
    
    /*
     * Create InputForm if not done
     */
    private Form getInputForm(){
        if(inputForm==null){
            inputForm = new Form("Input Data");
            inputForm.addCommand(exitCommand);
            inputForm.addCommand(connectCommand);
            inputForm.setCommandListener(this);
            // TextField for input
            uriInputText = new TextField("URI","",255,TextField.URL);
            inputForm.append(uriInputText);
        }
        return inputForm;
    }
    
    
    private TextBox getOutput(){
        if(output==null){
            output = new TextBox("Output","",65555,TextField.ANY);
            output.addCommand(backCommand);
            output.setCommandListener(this);
        }
        return output;
    }
   
}
