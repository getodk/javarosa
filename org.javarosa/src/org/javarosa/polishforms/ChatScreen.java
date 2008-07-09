//#if polish.usePolishGui

package org.javarosa.polishforms;

import java.util.Stack;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.midlet.MIDlet;

import org.javarosa.clforms.Controller;
import org.javarosa.clforms.MVCComponent;
import org.javarosa.clforms.api.Prompt;
import org.javarosa.clforms.api.ResponseEvent;
import org.javarosa.clforms.view.FormView;
import org.javarosa.clforms.view.IPrompter;
import org.javarosa.clforms.api.Constants;

import de.enough.polish.ui.Item;
import de.enough.polish.ui.ItemStateListener;
import de.enough.polish.ui.Gauge;
import de.enough.polish.ui.UiAccess;

import org.javarosa.view.widget.chart.*;

import java.util.Vector;
import org.javarosa.dtree.i18n.*;
import javax.microedition.lcdui.Canvas;
import java.util.Enumeration;


/***
 * The ChatScreen is a view for Mobile MRS that presents an entire XForm to the user. 
 * 
 * It implements both the IPrompter and FormView interfaces, to be responsible for presenting forms and prompts in
 * the same screen.
 * 
 * The Screen presents each Prompt as a DisplayFrame, one at a time scrolling from the bottom of the screen to the top.
 * 
 * @author ctsims
 *
 */
public class ChatScreen extends de.enough.polish.ui.FramedForm  implements IPrompter, FormView, CommandListener, ItemStateListener{
    
    Controller controller;

    Command menuCommand = new Command("Menu", Command.SCREEN, 2);

    Command selectCommand = new Command("Select", Command.SCREEN, 1);
    
    Command backCommand = new Command("Back", Command.BACK, 2);
    
    Command saveAndReloadCommand = new Command("Save and Start Over", Command.SCREEN, 4);
    
    Command saveAndExitCommand = new Command("Save and Exit", Command.SCREEN, 4);
    
    Command exitCommand = new Command("Exit", Command.EXIT, 4);
    
    Gauge progressBar;
    
    Stack displayFrames = new Stack();
    Stack prompts = new Stack();
    
    int currentPromptIndex = -1;
    
    Vector langCommandLabel = new Vector();
    
    public static int countXForm = 0;

    Command languageCommand = new Command("Select Language", Command.SCREEN, 2);

    /**
     * Creates a ChatScreen, and loads the menus
     */
    public ChatScreen() {
        //#style framedForm
        super("GATHER");

        //this.addCommand(menuCommand);
        this.addCommand(selectCommand);
        
        this.addCommand(exitCommand);
        this.addCommand(saveAndReloadCommand);
        this.addCommand(saveAndExitCommand);
        this.addCommand(backCommand);
        
        this.setCommandListener(this);
        
        //#style progressbar
        progressBar = new Gauge(null, false, 1, 0);
        append(Graphics.BOTTOM, progressBar);
    }
    
    
    
    public void showError(String title, String message, Display display)
    {
    	//#style mailAlert
    	Alert alert = new Alert(title, message, null, AlertType.ERROR);
    	alert.setTimeout(Alert.FOREVER);
    	display.setCurrent(alert, this);  
    }
    
//    /**
//     * Handler for when the answer for the previous question has been selected 
//     */
//    private void selectPressed() {
//        System.out.println("selected!");
//        if(!displayFrames.empty()) {
//            DisplayFrame topFrame = (DisplayFrame)(displayFrames.peek());
//            topFrame.evaluateResponse();
//        }
//    }    
    
    /**
     * Adds a new Prompt to the screen
     * 
     * @param nextPrompt The prompt to be added
     */
    private void addPrompt(Prompt nextPrompt) {
        DisplayFrame frame = new DisplayFrame(nextPrompt);
        this.setItemStateListener(this);
        displayFrames.push(frame);
        prompts.push(nextPrompt);
        frame.drawLargeFormOnScreen(this);
    }
    
    /**
     * Removes a generic MIDP Item that is contained in this screen
     * 
     * @param theItem The item to be removed
     * 
     * @return The old index of the removed item. -1 if the Item was not found.
     */
    public int removeItem(Item theItem) {
        for(int i = 0 ; i < this.size(); i++ ) {
            if(this.get(i).equals(theItem)) {
                this.delete(i);
                return i;
            }
        }
        return -1;
    }
    
    public int  removeChart(LineChart chart) {
        for(int i=0; i < this.size(); i++) {
            if(this.get(i).equals(chart)) {
                this.delete(i);
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Exits this form
     */
    private void exitForm() {
        controller.processEvent(new ResponseEvent(ResponseEvent.EXIT, -1));
        clearForm();
        
        if (controller.getForm().getLocalizer() != null)
        	controller.getForm().getLocalizer().unregisterLocalizable(controller.getForm());
        this.controller.setForm(null);
    }
    
    private void popPrompt() {
        if (!prompts.isEmpty()) {
            prompts.pop();
            ((DisplayFrame)displayFrames.pop()).removeFromScreen(this);
        }
    }
    
    /**
     * Clears the current form from this display
     */
    private void clearForm() {
        prompts.removeAllElements();
        while(!displayFrames.isEmpty()) {
            DisplayFrame frame = (DisplayFrame)displayFrames.pop();
            frame.removeFromScreen(this);
        }
    }
    
    /**
     * Identify whether the form is finished
     * 
     * @param prompt The last prompt provided by the controller
     * @return true if the form is finished, false otherwise.
     */
    private boolean checkFinishedWithForm(Prompt prompt) {
        if(prompts.contains(prompt)) {
            return true;
        }
        return false;
    }
    
    /**
     * (Inherited from FormView) Displays a prompt This is generally a sign that
     * we're either starting or finishing a form
     */
    public void displayPrompt(Prompt prompt) {
        System.out.println("Display Prompt");
        if (checkFinishedWithForm(prompt)) {
            commandAction(this.saveAndExitCommand, this);           
        } else {
        	if (controller.getForm().getLocalizer() != null) {
               	this.addCommand(languageCommand);
        		populateLanguageMenu();
        	}
            MVCComponent.display.setCurrent(this);
            showPrompt(prompt);
        }
     
        //droos 4/18/08: in the future we may want to be able to show a 'full' progress bar once the
        //form has been completed, but this depends on other changes to the end-of-form workflow
        progressBar.setValue(controller.getPromptIndex());
    }

    public void populateLanguageMenu () {
    	String[] availableLocales = controller.getForm().getLocalizer().getAvailableLocales();
    	for(int i=0; i < availableLocales.length; i++){
    		if(!langCommandLabel.contains(availableLocales[i])) {
    			UiAccess.addSubCommand(new Command(availableLocales[i], Command.SCREEN, 3), languageCommand, this);
    			langCommandLabel.addElement(availableLocales[i]);
    		}
    	}
    }
    
    /**
     * Shows a prompt on the screen
     */
    public void showPrompt(Prompt prompt) {
        if(!displayFrames.empty()) {
            DisplayFrame topFrame = (DisplayFrame)(displayFrames.peek());
            // @JJ May 26, 2008:
            //moving this earlier so that its actually populated when there is a call made back to the controller
            //I'm not sure how this works in general as thsi should be populated when the controller performs its logic
            //topFrame.evaluateResponse();
            
            topFrame.removeFromScreen(this);
            topFrame.drawSmallFormOnScreen(this);
        }
        addPrompt(prompt);
        
        progressBar.setValue(controller.getPromptIndex());
    }
    
    /**
     * Shows a prompt on the screen at position screenIndex of totalScreens
     */
    public void showPrompt(Prompt prompt, int screenIndex, int totalScreens) {
        showPrompt(prompt);
    }

    /**
     * Registers a controller with this FormView 
     */
    public void registerController(Controller controller) {
        this.controller = controller;
        progressBar.setMaxValue(controller.getForm().getPromptCount());
    }
    /**
     * Handles command events
     */
    public void commandAction(Command command, Displayable s) {
    	String label = command.getLabel();
    	
        try {
            if (command == selectCommand) {
                
            	// @JJ May 26, 2008: added from SetPrompt
            	if(!displayFrames.empty()) {
                    DisplayFrame topFrame = (DisplayFrame)(displayFrames.peek());
                    topFrame.evaluateResponse();
                }
                    
                controller.processEvent(new ResponseEvent(ResponseEvent.NEXT, -1));
            }
            else if (command == backCommand ) {
                popPrompt();
                popPrompt();
                controller.processEvent(new ResponseEvent(ResponseEvent.PREVIOUS, -1));
            }
            else if (command == saveAndExitCommand) {
                clearForm();
                controller.processEvent(new ResponseEvent(ResponseEvent.SAVE_AND_EXIT, -1));
                exitForm();
            }
            else if (command == saveAndReloadCommand) {
                clearForm();
                controller.processEvent(new ResponseEvent(ResponseEvent.SAVE_AND_RELOAD, -1));
            }
            else if (command == exitCommand){
                exitForm();
            } else {
            	if (controller.getForm().getLocalizer() != null) { //don't think localizer can be null here, but to be safe...
            		controller.getForm().getLocalizer().setLocale(label);
            		refreshDisplay();
            	}
            }
            
        } catch (Exception e) {
            Alert a = new Alert("error.screen" + " 2"); //$NON-NLS-1$
            a.setString(e.getMessage());
            a.setTimeout(Alert.FOREVER);
            MVCComponent.display.setCurrent(a);
        }
    }
    
    public void destroy () {
    	clearForm();
    	exitForm();
    }
    
    /**
     * Makes proper updates when an Item on this page is changed
     */
    public void itemStateChanged(Item item) {
        if(!displayFrames.empty()) {
            DisplayFrame topFrame = (DisplayFrame)(displayFrames.peek());
            if(topFrame.autoPlayItem(item)) {
                commandAction(selectCommand,this);
            }
        }
    }
    
    public void refreshDisplay() {
        Enumeration displayEnum = displayFrames.elements();
        while(displayEnum.hasMoreElements()){
     	   DisplayFrame frame = (DisplayFrame) displayEnum.nextElement();
     	   frame.refreshDisplayFrame(this);
        }
     }
      /**
      * This function is used to handle the keypressed Events
      * @param keyCode integer is passed to the function
      *
      **/
     public void keyPressed(int keyCode) {
         super.keyPressed(keyCode);
    	 
    	 Localizer l = controller.getForm().getLocalizer();
    	 if (l == null)
    		 return;
    	 
         if(keyCode == Canvas.KEY_POUND) {
             String nextLocale = l.getNextLocale();
             String currentLocale = l.getLocale();
             if(!currentLocale.equals(nextLocale)){
                 l.setLocale(nextLocale);
                 refreshDisplay();
             }             
         }
     }
}
//#endif