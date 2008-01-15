package org.dimagi.polishforms;

import javax.microedition.lcdui.Graphics;

import org.dimagi.entity.Question;

import de.enough.polish.ui.ChoiceGroup;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.TextField;

public class DisplayFrame {
    
    private Question theQuestion;
    private Item theWidget;
    private StringItem questionText;
    private StringItem questionResponse;
    private Item[] displayedItems;
    
    public DisplayFrame(Question theQuestion) {
        this.theQuestion = theQuestion;
        
        //#style questionText
        questionText = new StringItem(null,theQuestion.longText);
        
        loadWidget();
    }
    
    private void loadWidget() {
        switch(theQuestion.inputType){
        case Question.TEXT:
            //#style textBox
            TextField inputBox = new TextField("","",15,TextField.ANY);
            
            theWidget = inputBox;
            break;
        case Question.NUMBER:
            //#style textBox
            TextField numberBox = new TextField("","",15,TextField.NUMERIC);
            
            theWidget = numberBox;
            break;
        case Question.SINGLE_SELECT:
            ChoiceGroup choiceGroup = new ChoiceGroup("", ChoiceGroup.EXCLUSIVE);

            for(int i = 0 ; i < theQuestion.options.length; ++i) {
                //#style choiceItem
                choiceGroup.append(theQuestion.options[i], null);
            }
            
            theWidget = choiceGroup;
            break;
        case Question.MULTIPLE_SELECT:
            ChoiceGroup multipleGroup = new ChoiceGroup("", ChoiceGroup.MULTIPLE);

            for(int i = 0 ; i < theQuestion.options.length; ++i) {
                //#style choiceItem
                multipleGroup.append(theQuestion.options[i], null);
            }
            theWidget = multipleGroup;
            break;
        }
    }
    
    public void evaluateResponse() {
        switch(theQuestion.inputType){
        case Question.TEXT:
            theQuestion.value = ((StringItem)theWidget).getText();
            break;
        case Question.NUMBER:
            theQuestion.value = ((StringItem)theWidget).getText();
            break;
        case Question.SINGLE_SELECT:
            ChoiceGroup singleWidget = (ChoiceGroup)theWidget;
            theQuestion.value = new int[] { singleWidget.getSelectedIndex() };
            break;
        case Question.MULTIPLE_SELECT:
            ChoiceGroup multiWidget = (ChoiceGroup)theWidget;
            int numItems = multiWidget.getItems().length;
            int numSelectedItems = 0;
            int[] selectedIndicies = new int[numItems];
            for(int i = 0 ; i < multiWidget.getItems().length ; i++) {
                if(multiWidget.isSelected(i)) {
                    selectedIndicies[numSelectedItems] = i;
                    numSelectedItems++;
                }
            }
            int[] questionValue = new int[numSelectedItems];
            for(int i = 0 ; i < numSelectedItems ; i++) {
                questionValue[i] = selectedIndicies[i];
            }
            theQuestion.value = questionValue;
        }
    }
    
    public void drawLargeFormOnScreen(Screen target) {
        //#style questionText
        questionText.setStyle();
        target.append(questionText);
        target.append(theWidget);
        displayedItems = new Item[] {questionText,theWidget};
        target.focus(theWidget);
    }
    public void drawSmallFormOnScreen(Screen target) {
        //#style oldPromptText
        questionText.setStyle();
        target.append(questionText);
        //#style valueText
        questionResponse = new StringItem(null,questionValueToString(theQuestion));
        target.append(questionResponse);
        displayedItems = new Item[] {questionText,questionResponse};
    }
    public void removeFromScreen(Screen target) {
        for(int i = 0 ; i < displayedItems.length; i++) {
            target.removeItem(displayedItems[i]);
        }
    }    
    
    private String questionValueToString(Question theQuestion) {
        if(theQuestion.inputType == Question.TEXT || theQuestion.inputType == Question.NUMBER) {
            return theQuestion.value.toString();
        }
        else if(theQuestion.inputType == Question.SINGLE_SELECT || 
                theQuestion.inputType == Question.MULTIPLE_SELECT) {
            String returnString = "";
            int[] selectedIndicies = (int[])theQuestion.value;
            for(int i = 0 ; i < selectedIndicies.length ; i++) {
                returnString = returnString + theQuestion.options[selectedIndicies[i]] + ", ";
            }
            return returnString.substring(0,returnString.length() - 2);
        }
        else {
            return theQuestion.value.toString();
        }
    }
}
