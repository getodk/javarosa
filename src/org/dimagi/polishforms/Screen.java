package org.dimagi.polishforms;

import java.util.Stack;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import org.dimagi.entity.Question;

import de.enough.polish.ui.Item;

public class Screen extends de.enough.polish.ui.FramedForm {
    
    Question[] sampleQuestions = new Question[]{
        new Question("What is the child's Age?","Child Age",Question.NUMBER),
        new Question("What is the Child's Gender?","Gender",Question.SINGLE_SELECT, new String[]{"Male","Female"}),
        new Question("What Symptoms does the child have?","Symptoms",Question.MULTIPLE_SELECT, new String[]{"Cough","Fever"}),
        new Question("What is the child's name?","Child Name",Question.TEXT)
    };
    int currentQuestion = 0;

    Command menuCommand = new Command("Menu", Command.SCREEN, 1);

    Command selectCommand = new Command("Select", Command.BACK, 1);
    
    Stack displayFrames = new Stack();

    public Screen() {
        //#style framedForm
        super("Chatterbox");

        this.addCommand(menuCommand);
        this.addCommand(selectCommand);

        this.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                if (c == selectCommand) {
                    selectPressed();
                }
            }
        }); 
    }
    private void selectPressed() {
        System.out.println("selected!");
        currentQuestion = (currentQuestion + 1) % 4;
        if(!displayFrames.empty()) {
            DisplayFrame topFrame = (DisplayFrame)(displayFrames.peek());
            topFrame.evaluateResponse();
            topFrame.removeFromScreen(this);
            topFrame.drawSmallFormOnScreen(this);
        }
        addQuestion(sampleQuestions[currentQuestion]);
    }    
    private void addQuestion(Question nextQuestion) {
        DisplayFrame frame = new DisplayFrame(nextQuestion);
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
}
