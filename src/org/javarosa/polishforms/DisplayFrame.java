package org.javarosa.polishforms;

import java.util.Enumeration;

import org.javarosa.clforms.api.Constants;
import org.javarosa.clforms.api.Prompt;

import de.enough.polish.ui.ChoiceGroup;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.TextField;

/**
 * DisplayFrames are a UI component used to interact with Prompts.
 * 
 * The frame will determine the proper UI component to use for a Prompt, and set return values appropriately.
 * 
 * @author ctsims
 *
 */
public class DisplayFrame {
    
    private Prompt thePrompt;
    private Item theWidget;
    private StringItem questiontext;
    private StringItem questionResponse;
    private Item[] displayedItems;
   
    /**
     * Creates a Display frame for the given prompt
     * @param thePrompt The prompt to be displayed
     */
    public DisplayFrame(Prompt thePrompt) {
        this.thePrompt = thePrompt;
        
        //#style questiontext
        questiontext = new StringItem(null,thePrompt.getLongText());
        
        loadWidget();
    }
    
    /**
     * Selects the proper Control to use for the given input type of the Prompt
     */
    private void loadWidget() {
        Enumeration itr;
        switch(thePrompt.getFormControlType()) {
        case Constants.INPUT:
            //#style textBox
            TextField inputBox = new TextField("","",30,TextField.ANY);
            theWidget = inputBox;
            break;
        case Constants.TEXTAREA:
            //#style textBox
            TextField textArea = new TextField("","",15,TextField.ANY);
            theWidget = textArea;
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
            ChoiceGroup multipleGroup = new ChoiceGroup("", ChoiceGroup.MULTIPLE);
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
    }
    
    /**
     * Evaluates a response based on the input type, and sets the Value of the Prompt
     */
    public void evaluateResponse() {
        switch(thePrompt.getFormControlType()) {
        case Constants.INPUT:
            thePrompt.setValue(((StringItem)theWidget).getText());
            break;
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
    }
    
    /**
     * Draws the Large Version of the frame onto a Screen
     * 
     * @param target the Screen to which the Frame will be Added
     */
    public void drawLargeFormOnScreen(ChatScreen target) {
      //#style questiontext
        questiontext = new StringItem(null,questiontext.getText());
        target.append(questiontext);
        target.append(theWidget);
        displayedItems = new Item[] {questiontext,theWidget};
        target.focus(theWidget);
    }
    /**
     * Draws the Small (read only) Version of the frame onto a Screen
     * 
     * @param target the Screen to which the Frame will be Added
     */
    public void drawSmallFormOnScreen(ChatScreen target) {
      //#style oldprompttext
        questiontext = new StringItem(null,questiontext.getText());
        target.append(questiontext);
        //#style valueText
        questionResponse = new StringItem(null,questionValueToString(thePrompt));
        target.append(questionResponse);
        displayedItems = new Item[] {questiontext,questionResponse};
    }
    
    /**
     * Removes any of the Frame's Items from a given Screen
     * 
     * @param target The screen from which the items will be removed
     */
    public void removeFromScreen(ChatScreen target) {
        for(int i = 0 ; i < displayedItems.length; i++) {
            target.removeItem(displayedItems[i]);
        }
    }    
    
    private String questionValueToString(Prompt thePrompt) {
        if(thePrompt.getFormControlType() == Constants.TEXTAREA || 
                thePrompt.getFormControlType() == Constants.TEXTBOX ||
                thePrompt.getFormControlType() == Constants.INPUT) {
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
    }
    
    /**
     * Identifies whether a given Item is auto-playable, IE "Select" need not be pressed to set the value for that question.
     * @param item The item that will be identified for autoplayability
     * @return True if the item can set its value without being explicitly selected. False otherwise
     */
    public boolean autoPlayItem(Item item) {
        if(thePrompt.getFormControlType() == Constants.SELECT) {
            return false;
        }
        else {
            return true;
        }
    }
}
