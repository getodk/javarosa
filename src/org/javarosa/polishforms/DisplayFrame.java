//#if polish.usePolishGui

package org.javarosa.polishforms;

import java.util.Enumeration;
import java.util.Date;

import org.javarosa.clforms.api.Constants;
import org.javarosa.clforms.api.Prompt;

import javax.microedition.lcdui.Font;

import de.enough.polish.ui.ChoiceGroup;
import de.enough.polish.ui.ChoiceItem;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.TextField;
import de.enough.polish.ui.Container;
import de.enough.polish.ui.DateField;

import org.javarosa.clforms.util.*;

import org.javarosa.view.widget.ButtonItem;
import org.javarosa.view.widget.chart.LineChart;

/**
 * DisplayFrames are a UI component used to interact with Prompts.
 * 
 * The frame will determine the proper UI component to use for a Prompt, and set return values appropriately.
 * 
 * @author ctsims
 *
 */

/* droos: there are too many switch statements in the class; this is not object-oriented */

public class DisplayFrame {
    
    private Prompt thePrompt;
    private Item theWidget;
    private StringItem questiontext;
    private StringItem questionResponse;
    private Item[] displayedItems;
   
    private ButtonItem button;
    private LineChart chart;
    
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
        	if (thePrompt.getReturnType() != Constants.RETURN_DATE) {
        		//#style textBox
        		TextField inputBox = new TextField("", "",200,TextField.ANY);
        		inputBox.setInputMode(TextField.MODE_UPPERCASE);
        		inputBox.setText((String)thePrompt.getValue());
        		
        		if (thePrompt.getReturnType() == Constants.RETURN_INTEGER)
        			inputBox.setConstraints(TextField.NUMERIC);
            
        		theWidget = inputBox;
        	} else {
        		//#style textBox
        		DateField datefield = new DateField(null, DateField.DATE);
        		datefield.setDate((Date)thePrompt.getValue());
        		theWidget = datefield;
        	}
            break;
        case Constants.TEXTAREA:
        	TextField textArea;
        	if(thePrompt.getValue()!= null){
        		//#style textBox
        		textArea = new TextField("",thePrompt.getValue().toString(),15,TextField.ANY);
        	} else{
        		//#style textBox
        		textArea = new TextField("","",15,TextField.ANY);
        	}

        	theWidget = textArea;
        	break;
        case Constants.TEXTBOX:
        	TextField textBox;
        	if(thePrompt.getValue() != null){
        		//#style textBox
        		textBox = new TextField("",thePrompt.getValue().toString(),50,TextField.ANY);
        	}else{
        		//#style textBox
        		textBox = new TextField("","",50,TextField.ANY);
        	}

        	theWidget = textBox;
        	break;        
        case Constants.SELECT1:
        	ChoiceGroup choiceGroup;
        	String appearance = thePrompt.getAppearanceString();
        	if(appearance != null && appearance.equalsIgnoreCase("minimal")) {
        		choiceGroup = new ChoiceGroup("", ChoiceGroup.POPUP);  //droos: watch this space
        	} else {
        		choiceGroup = new ChoiceGroup("", ChoiceGroup.EXCLUSIVE);
        	}

        	itr = thePrompt.getSelectMap().keys();
        	int i = 0;
        	while (itr.hasMoreElements()) {
        		String label = (String) itr.nextElement();
        		//#style choiceItem
        		choiceGroup.append(label, null);
        		if(thePrompt.getValue() != null){ //droos: i'm skeptical that this works properly
        			String  selectedOption = thePrompt.getValue().toString();
        			if(selectedOption.equalsIgnoreCase((String)thePrompt.getSelectMap().get(label))){
        				choiceGroup.setSelectedIndex(i, true);
        			}
        		} 
        		i++;
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
        	if(thePrompt.getValue() != null){
        		Object selectedOptions = thePrompt.getValue();
        		if(selectedOptions instanceof String){
        			String []selectedIndices = J2MEUtil.split((String)selectedOptions, ",");
        			for(int index =0; index <selectedIndices.length; index++){
        				int selectedOption = Integer.parseInt(selectedIndices[index]);
        				multipleGroup.setSelectedIndex(selectedOption,true);
        			}
        		} else if(selectedOptions instanceof int[]){
        			int []selectedIndices = (int[])thePrompt.getValue();
        			for(int index=0; index < selectedIndices.length; index++){
        				multipleGroup.setSelectedIndex(selectedIndices[index],true);
        			}
        		}               
        	}

        	theWidget = multipleGroup;
        	break;
        case Constants.TRIGGER:
        	//#style choiceItem
        	button = new ButtonItem(thePrompt.getLongText());
        	theWidget = button;
        	break;
        case Constants.OUTPUT_GRAPH:
        	/* experimental */
        	
        	int [] chartXPointsArray = thePrompt.getControlDataArray();
        	int [] chartYPointsArray = {2, 8, 16, 32, 48, 55, 64, 70, 80, 87};
        	String [] chartXPointsLabelArray = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j"};
        	chart = new LineChart("Weight Graph"); 
        	chart.setUseDefaultColor(false);
          
        	chart.setFont(Font.FACE_PROPORTIONAL,Font.STYLE_PLAIN,Font.SIZE_SMALL);
        	chart.setDrawAxis(true);
        	// chart.setPreferredSize(120, 120);
        	chart.setMargins(5,3,30,35);
        	chart.makeShadowVisible(true);
        	chart.setShadowColor(20,20,20);
        	chart.setColor(0, 0, 0);
        	chart.resetData();
        	
        	for(int j = 0; j < chartXPointsArray.length; j++) {
        		chart.insertItem(chartXPointsLabelArray[j], chartXPointsArray[j], chartYPointsArray[j], 0,  0,   255);
        	}
        	
        	chart.setMaxYScaleFactor(100);
        	
        	break;        
        default:
            System.out.println("Unhandled Widget type: " + thePrompt.getFormControlType());
            break;
        }
    }
    
    private void reloadWidget(){
    	Enumeration itr;
    	switch(thePrompt.getFormControlType()) {
    	case Constants.INPUT: {
            questiontext.setText(thePrompt.getLongText());
    		break;
    	}

    	case Constants.TEXTAREA: {
            questiontext.setText(thePrompt.getLongText());
    		break;
    	} 

    	case Constants.TEXTBOX: {
            questiontext.setText(thePrompt.getLongText());
    		break;
    	}  

    	case Constants.SELECT1: {
            questiontext.setText(thePrompt.getLongText());

    		ChoiceGroup choice = (ChoiceGroup)theWidget;            	 
    		itr = thePrompt.getSelectMap().keys();
    		int i = 0;
    		while (itr.hasMoreElements()) {
    			String label = (String) itr.nextElement();

    			ChoiceItem selectItem = choice.getItem(i);
    			selectItem.setText(label);
    			i++;
    		}
    		break;
    	}

    	case Constants.SELECT : {
            questiontext.setText(thePrompt.getLongText());

    		ChoiceGroup multipleGroup = (ChoiceGroup) theWidget;
    		itr = thePrompt.getSelectMap().keys();
    		int i = 0;
    		while (itr.hasMoreElements()) {
    			String label = (String) itr.nextElement();
    			ChoiceItem item = multipleGroup.getItem(i);
    			item.setText(label);
    			i++;
    		}
    		break;
    	} 

    	case Constants.TRIGGER:{
    		ButtonItem item = (ButtonItem) theWidget;
    		item.setLabel(thePrompt.getLongText());
    	} 

    	case Constants.OUTPUT_GRAPH: {

    		break;
    	}
    	default:
    		//theWidget.setLabel(thePrompt.getLongText());
    	}     
    }
    
    /**
     * Evaluates a response based on the input type, and sets the Value of the Prompt
     */
    public void evaluateResponse() {
        switch(thePrompt.getFormControlType()) {
        case Constants.INPUT:
        	if (thePrompt.getReturnType() != Constants.RETURN_DATE) {
        	    String value = ((StringItem)theWidget).getText();
        	    if(value == null) {
        	        value = "";
        	    }
        		thePrompt.setValue(value);
        	} else {
        		Date d = ((DateField)theWidget).getDate();
        		thePrompt.setValue(d);
        	}
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
            
            String label = singleWidget.getItem(index).getText();
            String objValue = (String) thePrompt.getSelectMap().get(label);
            thePrompt.setValue(objValue);
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
        case Constants.TRIGGER:
            thePrompt.setValue("Ok");
            break;
       case Constants.OUTPUT_GRAPH:
            int[] arr = (int[]) thePrompt.getControlDataArray();
            thePrompt.setValue(thePrompt.getControlDataArray());
            thePrompt.setValue("Done");
            break;
        }
    }
    
    /**
     * Draws the Large Version of the frame onto a Screen
     * 
     * @param target the Screen to which the Frame will be Added
     */
    public void drawLargeFormOnScreen(ChatScreen target) {
    	if(theWidget == null) {
            if(chart != null) {
                chart.setPreferredSize(target.getWidth(), target.getAvailableHeight() - 10);
                target.append(chart); 
            }
        } else {
        	//#style questiontext
            questiontext = new StringItem(null,thePrompt.getLongText());
        	target.append(questiontext);
        	target.append(theWidget);
        }
        displayedItems = new Item[] {questiontext,theWidget};
        target.focus(theWidget);
        target.scrollRelative((theWidget.itemHeight)/2);
    }

    /**
     * Draws the Small (read only) Version of the frame onto a Screen
     * 
     * @param target the Screen to which the Frame will be Added
     */
    public void drawSmallFormOnScreen(ChatScreen target) {
        //#style splitleft
        questiontext = new StringItem(null,thePrompt.getShortText());
        //#style splitright
        questionResponse = new StringItem(null, questionValueToString(thePrompt)); 
    	
    	//#style split
    	Container c = new Container(false);
    	//polish has a quirk where it really wants to impose the parent styling onto the first item in the
    	//container, even if you explicitly override it with a new style. this null item takes the fall
    	c.add(new StringItem(null, null));
        c.add(questiontext);
        c.add(questionResponse);
        target.append(c);
        displayedItems = new Item[] {c};
    }
    
    /**
     * Removes any of the Frame's Items from a given Screen
     * 
     * @param target The screen from which the items will be removed
     */
    public void removeFromScreen(ChatScreen target) {
        for(int i = 0 ; i < displayedItems.length; i++) {
            if((displayedItems[i] == null ) && (chart != null)) {
                target.removeChart(chart);
                //chart = null;
            } else {
            	target.removeItem(displayedItems[i]);
            }
        }
    }    
    
    private String questionValueToString(Prompt thePrompt) {
        if(thePrompt.getFormControlType() == Constants.TEXTAREA || 
                thePrompt.getFormControlType() == Constants.TEXTBOX ||
                thePrompt.getFormControlType() == Constants.INPUT) {
        	if (thePrompt.getReturnType() == Constants.RETURN_DATE) { //J2MEUtil.getStringValue probably handles everything-- not investigating now
        		return J2MEUtil.getShortStringValue(thePrompt.getValue(), Constants.RETURN_DATE);
        	} else {
        		return thePrompt.getValue().toString();
        	}
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
        } else if(thePrompt.getFormControlType() == Constants.OUTPUT_GRAPH) {
            return "Done";
        } else {
            return thePrompt.getValue().toString();
        }
    }
    
    /**
     * Identifies whether a given Item is auto-playable, IE "Select" need not be pressed to set the value for that question.
     * @param item The item that will be identified for autoplayability
     * @return True if the item can set its value without being explicitly selected. False otherwise
     */
    public boolean autoPlayItem(Item item) {
        if(thePrompt.getFormControlType() == Constants.SELECT ||
           thePrompt.getFormControlType() == Constants.OUTPUT ||
           (thePrompt.getFormControlType() == Constants.INPUT && thePrompt.getReturnType() != Constants.RETURN_DATE)) {
            return false;
        }
        else {
            return true;
        }
    }
    
    public void refreshDisplayFrame(ChatScreen target) {
    	if(questionResponse == null){
    		reloadWidget();
    	} else {
            questiontext.setText(thePrompt.getShortText());
    		questionResponse.setText(questionValueToString(thePrompt));
    	}
    }
}

//#endif