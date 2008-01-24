package org.javarosa.polishforms;

import java.util.Stack;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import org.javarosa.clforms.Controller;
import org.javarosa.clforms.MVCComponent;
import org.javarosa.clforms.api.Prompt;
import org.javarosa.clforms.api.ResponseEvent;
import org.javarosa.clforms.view.FormView;
import org.javarosa.clforms.view.IPrompter;
import org.javarosa.properties.PropertyManager;

import de.enough.polish.ui.Item;
import de.enough.polish.ui.ItemStateListener;
import de.enough.polish.ui.UiAccess;

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

    Command menuCommand = new Command("Menu", Command.SCREEN, 1);

    Command selectCommand = new Command("Select", Command.BACK, 1);
    
    Command exitCommand = new Command("Exit", Command.EXIT, 1);
    
    Stack displayFrames = new Stack();

    /**
     * Creates a ChatScreen, and loads the menus
     */
    public ChatScreen() {
        //#style framedForm
        super("Chatterbox");

        this.addCommand(menuCommand);
        this.addCommand(selectCommand);
        
        UiAccess.addSubCommand(exitCommand, menuCommand,this);
        
        this.setCommandListener(this); 
    }
    
    /**
     * Handler for when the answer for the previous question has been selected 
     */
    private void selectPressed() {
        System.out.println("selected!");
        if(!displayFrames.empty()) {
            DisplayFrame topFrame = (DisplayFrame)(displayFrames.peek());
            topFrame.evaluateResponse();
        }
    }    
    
    /**
     * Adds a new Prompt to the screen
     * 
     * @param nextPrompt The prompt to be added
     */
    private void addPrompt(Prompt nextPrompt) {
        DisplayFrame frame = new DisplayFrame(nextPrompt);
        this.setItemStateListener(this);
        displayFrames.push(frame);
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
    
    /**
     * (Inherited from FormView) Displays a prompt
     */
    public void displayPrompt(Prompt prompt) {
        System.out.println("Display Prompt");
        showPrompt(prompt);
    }
    /**
     * Shows a prompt on the screen
     */
    public void showPrompt(Prompt prompt) {
        MVCComponent.display.setCurrent(this);
        if(!displayFrames.empty()) {
            DisplayFrame topFrame = (DisplayFrame)(displayFrames.peek());
            topFrame.evaluateResponse();
            topFrame.removeFromScreen(this);
            topFrame.drawSmallFormOnScreen(this);
        }
        addPrompt(prompt);
    }
    
    /**
     * Shows a prompt on the screen at position screenIndex of totalScreens
     */
    public void showPrompt(Prompt prompt, int screenIndex, int totalScreens) {
        displayPrompt(prompt);
    }

    /**
     * Registers a controller with this FormView 
     */
    public void registerController(Controller controller) {
        this.controller = controller;
    }
    /**
     * Handles command events
     */
    public void commandAction(Command command, Displayable s) {
        try {
            if (command == selectCommand) {
                selectPressed();
                controller.processEvent(new ResponseEvent(ResponseEvent.NEXT, -1));
            }
            /*if (command == saveAndReloadCommand) {
                controller.processEvent(new ResponseEvent(ResponseEvent.SAVE_AND_RELOAD, -1));
            }
            else if (command == List.SELECT_COMMAND){
                System.out.println("FormViewScreen.commandAction(SELECT_COMMAND) selectedIndex: " + ((List)screen).getSelectedIndex());
                controller.processEvent(new ResponseEvent(ResponseEvent.GOTO,((List)screen).getSelectedIndex()));
            }*/
            else if (command == exitCommand){
                controller.processEvent(new ResponseEvent(ResponseEvent.EXIT, -1));
            }
            
        } catch (Exception e) {
            Alert a = new Alert("error.screen" + " 2"); //$NON-NLS-1$
            a.setString(e.getMessage());
            a.setTimeout(Alert.FOREVER);
            MVCComponent.display.setCurrent(a);
        }
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
}
