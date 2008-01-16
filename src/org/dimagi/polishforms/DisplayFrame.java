package org.dimagi.polishforms;

import java.util.Enumeration;

import org.celllife.clforms.api.Constants;
import org.celllife.clforms.api.Prompt;
import org.dimagi.entity.Question;

import de.enough.polish.ui.ChoiceGroup;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.TextField;

public class DisplayFrame {
    
    private Question theQuestion;
    private Prompt thePrompt;
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
    public DisplayFrame(Prompt thePrompt) {
        this.thePrompt = thePrompt;
        
        //#style questionText
        questionText = new StringItem(null,thePrompt.getLongText());
        
        loadWidget();
    }
    
    private void loadWidget() {
        Enumeration itr;
        switch(thePrompt.getFormControlType()) {
        case Constants.TEXTAREA:
            //#style textBox
            TextField inputBox = new TextField("","",15,TextField.ANY);
            theWidget = inputBox;
            break;
        case Constants.TEXTBOX:
            //#style textBox
            TextField textBox = new TextField("","",50,TextField.ANY);
            theWidget = textBox;
            break;
        case Constants.SELECT1:
            ChoiceGroup choiceGroup = new ChoiceGroup("", ChoiceGroup.EXCLUSIVE);
            itr = thePrompt.getSelectMap().keys();
            while (itr.hasMoreElements()) {
                String label = (String) itr.nextElement();
                //#style choiceItem
                choiceGroup.append(label, null);
            }
            
            theWidget = choiceGroup;
            break;
        case Constants.SELECT:
            ChoiceGroup multipleGroup = new ChoiceGroup("", ChoiceGroup.MULTIPLE) {
                protected boolean handlePointerPressed(int x, int y) {
                    System.out.println("PointerPressReceived at "+ x+ ", " + y);
                    return false;
                }
            };
            itr = thePrompt.getSelectMap().keys();
            while (itr.hasMoreElements()) {
                String label = (String) itr.nextElement();
                //#style choiceItem
                multipleGroup.append(label, null);
            }
            theWidget = multipleGroup;
            break;
        default:
            System.out.println("Unhandled Widget type: " + thePrompt.getFormControlType());
            break;
        }
        /*
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
            ChoiceGroup multipleGroup = new ChoiceGroup("", ChoiceGroup.MULTIPLE) {
                protected boolean handlePointerPressed(int x, int y) {
                    System.out.println("PointerPressReceived at "+ x+ ", " + y);
                    return false;
                }
            };

            for(int i = 0 ; i < theQuestion.options.length; ++i) {
                //#style choiceItem
                multipleGroup.append(theQuestion.options[i], null);
            }
            theWidget = multipleGroup;
            break;
        }*/
    }
    
    public void evaluateResponse() {
        switch(thePrompt.getFormControlType()) {
        case Constants.TEXTAREA:
            thePrompt.setValue(((StringItem)theWidget).getText());
            break;
        case Constants.TEXTBOX:
            thePrompt.setValue(((StringItem)theWidget).getText());
            break;
        case Constants.SELECT1:
            ChoiceGroup singleWidget = (ChoiceGroup)theWidget;
            int index = singleWidget.getSelectedIndex();
            thePrompt.setSelectedIndex(index);
            thePrompt.setValue(singleWidget.getItem(index).getText());
            break;
        case Constants.SELECT:
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
            thePrompt.setValue(questionValue);
            break;
        }
        /*switch(theQuestion.inputType){
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
        }*/
    }
    
    public void drawLargeFormOnScreen(ChatScreen target) {
        //#style questionText
        questionText.setStyle();
        target.append(questionText);
        target.append(theWidget);
        displayedItems = new Item[] {questionText,theWidget};
        target.focus(theWidget);
    }
    public void drawSmallFormOnScreen(ChatScreen target) {
        //#style oldPromptText
        questionText.setStyle();
        target.append(questionText);
        //#style valueText
        questionResponse = new StringItem(null,questionValueToString(thePrompt));
        target.append(questionResponse);
        displayedItems = new Item[] {questionText,questionResponse};
    }
    public void removeFromScreen(ChatScreen target) {
        for(int i = 0 ; i < displayedItems.length; i++) {
            target.removeItem(displayedItems[i]);
        }
    }    
    
    private String questionValueToString(Prompt thePrompt) {
        if(thePrompt.getFormControlType() == Constants.TEXTAREA || 
                thePrompt.getFormControlType() == Constants.TEXTBOX ) {
            return thePrompt.getValue().toString();
        }
        else if(thePrompt.getFormControlType() == Constants.SELECT1) {
            System.out.println(thePrompt.getValue());
            return thePrompt.getValue().toString();
        }
        else if(thePrompt.getFormControlType() == Constants.SELECT) {
            System.out.println(thePrompt.getValue());
            String returnString = "";
            int[] selectedIndicies = (int[])thePrompt.getValue();
            for(int i = 0 ; i < selectedIndicies.length ; i++) {
                returnString = returnString + thePrompt.getSelectMap().elementAt(selectedIndicies[i]) + ", ";
            }
            return returnString.substring(0,returnString.length() - 2);
        }
        else {
            return thePrompt.getValue().toString();
        }
        /*if(theQuestion.inputType == Question.TEXT || theQuestion.inputType == Question.NUMBER) {
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
        }*/
    }
}
