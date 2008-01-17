package org.dimagi.polishforms;

import java.util.Stack;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import org.celllife.clforms.Controller;
import org.celllife.clforms.MVCComponent;
import org.celllife.clforms.api.Constants;
import org.celllife.clforms.api.Prompt;
import org.celllife.clforms.api.ResponseEvent;
import org.celllife.clforms.view.FormView;
import org.celllife.clforms.view.IPrompter;
import org.dimagi.entity.Question;

import de.enough.polish.ui.Item;
import de.enough.polish.ui.ItemStateListener;
import de.enough.polish.ui.UiAccess;

public class ChatScreen extends de.enough.polish.ui.FramedForm  implements IPrompter, FormView, CommandListener, ItemStateListener{
    
    Controller controller;
    Question[] sampleQuestions = new Question[]{
        new Question("What is the child's Age?","Child Age",Question.NUMBER),
        new Question("What is the Child's Gender?","Gender",Question.SINGLE_SELECT, new String[]{"Male","Female"}),
        new Question("What Symptoms does the child have?","Symptoms",Question.MULTIPLE_SELECT, new String[]{"Cough","Fever"}),
        new Question("What is the child's name?","Child Name",Question.TEXT)
    };
    int currentQuestion = 0;

    Command menuCommand = new Command("Menu", Command.SCREEN, 1);

    Command selectCommand = new Command("Select", Command.BACK, 1);
    
    Command exitCommand = new Command("Exit", Command.EXIT, 1);
    
    Stack displayFrames = new Stack();

    public ChatScreen() {
        //#style framedForm
        super("Chatterbox");

        this.addCommand(menuCommand);
        this.addCommand(selectCommand);
        
        UiAccess.addSubCommand(exitCommand, menuCommand,this);
        
        this.setCommandListener(this); 
    }
    private void selectPressed() {
        System.out.println("selected!");
        if(!displayFrames.empty()) {
            DisplayFrame topFrame = (DisplayFrame)(displayFrames.peek());
            topFrame.evaluateResponse();
        }
    }    
    private void addPrompt(Prompt nextPrompt) {
        DisplayFrame frame = new DisplayFrame(nextPrompt);
        frame.wireWidgetAutoSelect(this,this, selectCommand);
        this.setItemStateListener(this);
        displayFrames.push(frame);
        frame.drawLargeFormOnScreen(this);
    }
    
    public int removeItem(Item theItem) {
        for(int i = 0 ; i < this.size(); i++ ) {
            if(this.get(i).equals(theItem)) {
                this.delete(i);
                return i;
            }
        }
        return -1;
    }
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
    
    public void showPrompt(Prompt prompt, int screenIndex, int totalScreens) {
        showPrompt(prompt);
    }

    public void registerController(Controller controller) {
        this.controller = controller;
    }
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
    
    public void itemStateChanged(Item item) {
        if(!displayFrames.empty()) {
            DisplayFrame topFrame = (DisplayFrame)(displayFrames.peek());
            if(topFrame.autoPlayItem(item)) {
                commandAction(selectCommand,this);
            }
        }
    }
}
