/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

//conditional include for polish?

package org.javarosa.formmanager.view.chatterbox;

import java.util.Vector;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.Graphics;

import org.javarosa.core.api.Constants;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.formmanager.api.JrFormEntryController;
import org.javarosa.formmanager.api.transitions.FormEntryTransitions;
import org.javarosa.formmanager.utility.SortedIndexSet;
import org.javarosa.formmanager.view.IFormEntryView;
import org.javarosa.formmanager.view.chatterbox.widget.ChatterboxWidget;
import org.javarosa.formmanager.view.chatterbox.widget.ChatterboxWidgetFactory;
import org.javarosa.formmanager.view.chatterbox.widget.CollapsedWidget;
import org.javarosa.j2me.view.J2MEDisplay;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.CommandListener;
import de.enough.polish.ui.Container;
import de.enough.polish.ui.Displayable;
import de.enough.polish.ui.FramedForm;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.UiAccess;


public class Chatterbox extends FramedForm implements CommandListener, IFormEntryView{
	private static int LANGUAGE_CYCLE_KEYCODE = Canvas.KEY_POUND;
	
    private static final String PROMPT_REQUIRED_QUESTION = Localization.get("view.sending.RequiredQuestion");

	private static final String PROMPT_DEFAULT_CONSTRAINT_VIOL = "Answer is outside of the allowed range";
	
    public static int KEY_CENTER_LETS_HOPE = -5;
    
    public static final int UIHACK_SELECT_PRESS = 1;
	
	private JrFormEntryController controller;
    private FormEntryModel model;
    
    private ChatterboxWidgetFactory widgetFactory;
    private boolean multiLingual;
    private SortedIndexSet questionIndexes;
    private FormIndex activeQuestionIndex;
    
    /** The active question's index when a key is pressed */
    private FormIndex indexWhenKeyPressed = null;
    
    private boolean activeIsInterstitial = false; //true if the question corresponding to activeQuestionIndex is a 'create new repeat' question
    //active repeat for deleting? //TODO figure this out
    
    //TODO get the progress bar working
    
    //GUI elements
    private Command backCommand;
    private Command exitNoSaveCommand;
    private Command exitSaveCommand;
    private Command saveCommand;
    private Command languageSubMenu;
    private Command[] languageCommands;
    //private Command deleteRepeatCommand; //TODO do something with this
    private Gauge progressBar;
        
    public Chatterbox (String formTitle, JrFormEntryController controller) {
        //#style framedForm
    	super(formTitle);
    	
    	//TODO: READONLY FLAG!
    	if(false) {
    		//#style ReviewFramedForm
    		UiAccess.setStyle(this);
    	}
    	
    	//22 Jan, 2009 - csims@dimagi.com
    	//This constructor code supresses the ability to scroll the chatterbox by dragging the mouse
    	//pointer. This "Feature" was causing tons of problems for our nurses. Let me know if anyone else
    	//wants to make this a customizable inclusion.
    	this.container = new Container(false) {
    		protected boolean handlePointerScrollReleased(int relX, int relY) {
    			return false;
    		}
    	};
    	
    	this.model = controller.getModel();
    	this.controller = controller;

    	widgetFactory = new ChatterboxWidgetFactory(this);
    	
    	//TODO: READONLY FLAG!
    	widgetFactory.setReadOnly(false);
    	
    	multiLingual = (model.getForm().getLocalizer() != null);
    	questionIndexes = new SortedIndexSet();
    	activeQuestionIndex = FormIndex.createBeginningOfFormIndex(); //null is not allowed
    	
    	initGUI();
    	
    	//#if device.identifier == Sony-Ericsson/P1i
    	KEY_CENTER_LETS_HOPE = 13;
    	//#endif
    	
    	//#if device.identifier == Sony-Ericsson/K610i
    	LANGUAGE_CYCLE_KEYCODE = Canvas.KEY_STAR;
    	//#endif

    }

    public void destroy () {
    	for (int i = 0; i < size(); i++) {
    		((ChatterboxWidget)get(i)).destroy();
    	}
    }
    
    public void show () {
    	J2MEDisplay.setView(this);
    }
    
    private void initGUI () {
    	setUpCommands();
    	initProgressBar();
    	
    	//Mode 1: Read only review screen.
    	//TODO: READONLY FLAG!
    	if(false) {
    		while(controller.stepToNextEvent() != FormEntryController.END_OF_FORM_EVENT) {
    			//TODO: Anything?
    		}
    	} else if(null != null) { //TODO: Starting from a specific question
    		
    		//Mode 2: Seek to current question
    		while(!model.getCurrentFormIndex().equals(null)) {
    			controller.stepToNextEvent();
    		}
    	} else {
    		//Default Mode: Start at first question
    		controller.stepToNextEvent();
    		jumpToQuestion(model.getCurrentFormIndex());
    	}
    	this.currentlyActiveContainer = this.container;
    }
    
    private void setUpCommands () {
        backCommand = new Command("Back", Command.BACK, 2);
        exitNoSaveCommand = new Command("Exit", Command.EXIT, 4);
        exitSaveCommand = new Command("Save and Exit", Command.SCREEN, 4);
        saveCommand = new Command("Save", Command.SCREEN, 4);
        
        if (multiLingual) {
            languageSubMenu = new Command("Language", Command.SCREEN, 2);
        	populateLanguages();
        }
        
        //next command is added on a per-widget basis
        
        //TODO: READ ONLY FLAG!
        if(false) {
            addCommand(backCommand);
            addCommand(exitSaveCommand);
            addCommand(saveCommand);
    	}

        addCommand(exitNoSaveCommand);        

        
        if (languageSubMenu != null) {
        	addCommand(languageSubMenu);
        	for (int i = 0; i < languageCommands.length; i++)
        		addSubCommand(languageCommands[i], languageSubMenu);
        }
        
        setCommandListener(this);        
    }
    
    private void populateLanguages () {
    	String[] availableLocales = model.getForm().getLocalizer().getAvailableLocales();
    	languageCommands = new Command[availableLocales.length];
    	for (int i = 0; i < languageCommands.length; i++)
    		languageCommands[i] = new Command(availableLocales[i], Command.SCREEN, 3);
    }

    private void initProgressBar () {
        //#style progressbar
        progressBar = new Gauge(null, false, model.getNumQuestions(), 0);
        append(Graphics.BOTTOM, progressBar);
    }
    
    private void step(int event) {
    	switch(event) {
    	case FormEntryController.BEGINNING_OF_FORM_EVENT:
    		break;
    	case FormEntryController.END_OF_FORM_EVENT:
    		formComplete();
    		break;
    		default:
    			FormIndex index = model.getCurrentFormIndex();
    			jumpToQuestion(index);
    			break;
    	}
    }
    
    //make given question active; deal with all necessary questions in between
    private void jumpToQuestion (FormIndex questionIndex) {
    	boolean newRepeat = false;
    	
    	if (questionIndex.isInForm() && !model.isRelevant(questionIndex))
			throw new IllegalStateException();

		// Determine what should and shouldn't be pinned.
    	updatePins(questionIndex);

    	//figure out kind of reference and how to handle it
    	IFormElement last = model.getForm().getChild(questionIndex);
    	if (last instanceof GroupDef) {
    		if (((GroupDef)last).getRepeat() &&	
    			model.getForm().getInstance().resolveReference(model.getForm().getChildInstanceRef(questionIndex)) == null) {
    			
    			//We're at a repeat interstitial point. If the group has the right configuration, we are able
    			//to trigger a new repeat here. Otherwise, we'll have to ask the controller to move along.
    			
    			if(((GroupDef)last).noAddRemove) {
    				//We can't show anything meaningful here. Go back to the controller.
    				boolean forwards = questionIndex.compareTo(activeQuestionIndex) > 0;
    				if(forwards) {
    					step(controller.stepToNextEvent());
    				} else {
    					step(controller.stepToPreviousEvent());
    				}
    				return;
    			} else {
    				//All Systems Go. Display an interstitial "Add another FOO" question.
        			newRepeat = true;	
    			}
    		} else {
    			boolean forwards = questionIndex.compareTo(activeQuestionIndex) > 0;
    			if(forwards) {
    				createHeaderForElement(questionIndex);
    			} else {
    				removeHeaderForElement(questionIndex);
    			}
    			if(forwards) {
					step(controller.stepToNextEvent());
				} else {
					step(controller.stepToPreviousEvent());
				}
    			return;
    		}
    	} else if (questionIndex.isInForm() && model.isReadonly(questionIndex)) {
			boolean forwards = questionIndex.compareTo(activeQuestionIndex) > 0;
			if(forwards) {
				step(controller.stepToNextEvent());
			} else {
				step(controller.stepToPreviousEvent());
			}
			return;
    	}
    	    	
    	if (questionIndex.compareTo(activeQuestionIndex) > 0) {
    		if (activeQuestionIndex.isInForm()) {
    			if (activeIsInterstitial) {
    				removeFrame(activeQuestionIndex);
    			} else {
    				((ChatterboxWidget)get(questionIndexes.indexOf(activeQuestionIndex, true))).setViewState(ChatterboxWidget.VIEW_COLLAPSED);
    			}
    		}
    			
    		FormIndex index = activeQuestionIndex;
    		while(!index.equals(questionIndex)) {
    			index = model.getForm().incrementIndex(index);    			
    			putQuestion(index, index.equals(questionIndex), newRepeat);
    		}
    	} else if (questionIndex.compareTo(activeQuestionIndex) <= 0) {
    		FormIndex index = activeQuestionIndex;
    		while(!index.equals(questionIndex)) {
    			removeFrame(index);
    			index = model.getForm().decrementIndex(index);
    		}
    		
    		if (questionIndex.isInForm()) {
    			if (newRepeat) {
    				putQuestion(questionIndex, true, newRepeat);
    			} else {
    				((ChatterboxWidget)get(questionIndexes.indexOf(questionIndex, true))).setViewState(ChatterboxWidget.VIEW_EXPANDED);    
    			}
    		}
    	}
    	
    	if (!questionIndex.equals(activeQuestionIndex)) {
    		activeQuestionIndex = questionIndex;

    		if (activeQuestionIndex.isInForm()) {
    			int index = questionIndexes.indexOf(activeQuestionIndex, true);
    			ChatterboxWidget widget = (ChatterboxWidget)get(index);
    		
    			//Feb 4, 2009 - csims@dimagi.com
    			//The current widget's header should always be pinned in case it overruns the
    			//screen with options
    			//Feb 9, 2009 - csims@dimagi.com
    			//behaves strangely with the 3110c sized screens. Disabling for now.
    			//Feb 20, 2009 - csims@dimagi.com
    			//Tweaked a bunch of settings, should be fine for ~90% of our use cases now, if it's
    			//causing problems, set the polish flag.
    			//#if chatterbox.pinning.current != false
    			widget.setPinned(true);
    			//#endif

    			widget.showCommands();
    			
    			//Focus's efforts end up trying to scroll the focussed item upwards as if it were
    			//already displayed. If we supress the scrolling ahead of time we prevent the
    			//new item from getting a double-dose of scrolling.
    			int prevheight = this.container.getScrollHeight();
    			this.container.setScrollHeight(-1);
    			
    			this.focus(widget, true);
    			
    			//Return to normal scrolling behavior.
    			this.container.setScrollHeight(prevheight);
    		}
    			
    		//FIXME: no!
    		progressBar.setMaxValue(model.getNumQuestions());
    		progressBar.setValue(questionIndexes.size());    		
    	}
    	
    	//UI hacks ho!
    	babysitStyles();
    }
    
    private void updatePins(FormIndex questionIndex) {
		for (int i = 0; i < this.size(); ++i) {
			FormIndex index = this.questionIndexes.get(i);
			ChatterboxWidget cw = this.getWidgetAtIndex(i);

			//First reset everything by default
			cw.setPinned(false);
			
			if (cw.getViewState() == ChatterboxWidget.VIEW_LABEL) {
				if (FormIndex.isSubElement(index, questionIndex)) {
					cw.setPinned(true);
				} else {
					cw.setPinned(false);
				}
			}
		}
    }
    

	private void createHeaderForElement(FormIndex questionIndex) {
		String headerText = model.getCaptionPrompt(questionIndex).getLongText();
		if(headerText != null) {
			ChatterboxWidget headerWidget = widgetFactory.getNewLabelWidget(questionIndex, headerText);
			//If there is no valid header, there's no valid header. Possibly no label.
			this.append(headerWidget);
			this.questionIndexes.add(questionIndex);
			headerWidget.setPinned(true);
		}
	}
	
    private void removeHeaderForElement(FormIndex questionIndex) {
		//int headerIndex = this.questionIndexes.remove(questionIndex);
		this.removeFrame(questionIndex);
	}

	//create a frame for a question and show it at the appropriate place in the form
    private void putQuestion (FormIndex questionIndex, boolean expanded, boolean newRepeat) {
    	ChatterboxWidget cw = null;
    	
    	if (!questionIndex.isInForm())
    		return;
    	
    	if (expanded && newRepeat) {
    		cw = widgetFactory.getNewRepeatWidget(questionIndex, model, this);
    		activeIsInterstitial = true;
    	} else if (model.getForm().explodeIndex(questionIndex).lastElement() instanceof GroupDef) {
    		//do nothing
    	} else if (model.isRelevant(questionIndex)) { //FIXME relevancy check
    		cw = widgetFactory.getWidget(questionIndex, model,
    									  expanded ? ChatterboxWidget.VIEW_EXPANDED
    									    	   : ChatterboxWidget.VIEW_COLLAPSED);
    	}
    	
    	if (cw != null) {
    		putFrame(cw, questionIndex);
    	}
    }
    
    //insert a chatterbox widget into the form at the appropriate place
    private void putFrame (ChatterboxWidget widget, FormIndex questionIndex) {
    	int frameIndex = questionIndexes.add(questionIndex);
    	insert(frameIndex, widget);
    	widget.requestInit();
    }
    
    //remove the frame corresponding to a particular question from display
    private void removeFrame (FormIndex questionIndex) {
    	int frameIndex = questionIndexes.remove(questionIndex);
    	if (frameIndex == -1)
    		return; //question not present in chatterbox (hidden/non-relevant)
    	
    	ChatterboxWidget cw = (ChatterboxWidget)get(frameIndex);
    	cw.destroy();
    	delete(frameIndex);
    	
    	if (questionIndex.equals(activeQuestionIndex) && activeIsInterstitial) {
    		activeIsInterstitial = false;
    	}
    }
    
    public void formComplete () {
    	//TODO: READONLY FLAG!
    	if(true) {
	    	controller.jumpToIndex(FormIndex.createEndOfFormIndex());
	    	babysitStyles();
			progressBar.setValue(progressBar.getMaxValue());
			
			repaint();
			try {
				Thread.sleep(1000); //let them bask in their completeness
			} catch (InterruptedException ie) { }
				
			controller.saveAndExit();
    	} else { 
    		
    	}
    }
    
    
    private ChatterboxWidget activeFrame () {
    	int frameIndex = questionIndexes.indexOf(activeQuestionIndex, true);
    	if (frameIndex == -1)
    		return null;
    	else
    		return (ChatterboxWidget)get(frameIndex);
    }
    
    //probably not the most efficient way of doing this...
    private void babysitStyles () {
    	for (int i = 0; i < size(); i++) {
    		ChatterboxWidget cw = (ChatterboxWidget)get(i);
    		switch (cw.getViewState()) {
    		case ChatterboxWidget.VIEW_COLLAPSED:
    			//TODO: READONLY FLAG!
    			if(false) {
    				//#style ReviewSplit
    				UiAccess.setStyle(cw);
    			} else {
    				//#style split
    				UiAccess.setStyle(cw);
    			}
    			break;
    		case ChatterboxWidget.VIEW_EXPANDED:
    			//#style container
    			UiAccess.setStyle(cw);
    			break;
    		}
    	}
    	this.requestInit();
    	this.requestRepaint();
    }
    
    public void commandAction(Command command, Displayable s) {
    	System.out.println("cbox: command action");
    	
    	if (command == backCommand) {
    		System.out.println("back");
    		controller.stepToPreviousEvent();
    	} else if (command == exitNoSaveCommand) {
    		controller.abort();
    	} else if (command == exitSaveCommand) {
    		commitAndSave();
    		//controller.exit();
    		//TODO: EXIT?
    	} else if (command == saveCommand) {
    		commitAndSave();
    	} else if (command.getLabel() == Constants.ACTIVITY_TYPE_GET_IMAGES) {
    		suspendActivity(FormEntryTransitions.MEDIA_IMAGE);
    	} else if (command.getLabel() == Constants.ACTIVITY_TYPE_GET_AUDIO) {
        	suspendActivity(FormEntryTransitions.MEDIA_AUDIO);
    	} else if (command.getLabel() == "Capture") {
    		doCapture();
    	} else if (command.getLabel() == "Back") {
    		backFromCamera();
    	} else if (command.getLabel().equals(CollapsedWidget.UPDATE_TEXT)) { //TODO: Put this static string in a better place.
    		//Return to shell providing the question index.
    		model.setQuestionIndex(this.questionIndexes.get(this.getCurrentIndex()));
    		
    		throw new RuntimeException("NOT YET IMPLEMENTED: i don't where to transit to [droos 10/29]");
    		//controller.exit("update");
    	} else {
    		String language = null;
    		if (multiLingual) {
    			for (int i = 0; i < languageCommands.length; i++) {
    				if (command == languageCommands[i]) {
    					language = command.getLabel();
    					break;
    				}
    			}
    		}
    		
    		if (language != null) {
    			controller.setLanguage(language);
    		} else {
    			System.err.println("Chatterbox: Unknown command event received [" + command.getLabel() + "]");
    		}
    	}
    }

    
	private void suspendActivity(int mediaType) {
		//TODO: DEAL
		//controller.suspendActivity(mediaType);
	}
	
    private void commitAndSave () {
       	ChatterboxWidget frame = (activeIsInterstitial ? null : activeFrame());
    	if (frame != null) {
    		controller.answerQuestion(this.model.getCurrentFormIndex(), frame.getData());
    	}
    	//TODO: DEAL;
    	//controller.save();
    }
    
    public void questionAnswered () {
    	ChatterboxWidget frame = activeFrame();
	
    	if(activeQuestionIndex != this.model.getCurrentFormIndex()) {
    		//this is an error that comes from polish sending two events for button up and button
    		//down. We need to make it not send that message twice.
    		System.out.println("mismatching indices");
    		return;
    	}
    	if (activeIsInterstitial) {
    		//'new repeat?' answered
    		String answer = ((Selection)frame.getData().getValue()).getValue();
    		if (answer.equals("y")) {
    			controller.newRepeat(this.model.getCurrentFormIndex());
    			createHeaderForElement(this.model.getCurrentFormIndex());
    		}
    		step(controller.stepToNextEvent());
    	} else {
    		int status = controller.answerQuestion(this.model.getCurrentFormIndex(), frame.getData());
	    	if (status == FormEntryController.ANSWER_REQUIRED_BUT_EMPTY) {
	        	J2MEDisplay.showError(null, PROMPT_REQUIRED_QUESTION);
	    	} else if (status == FormEntryController.ANSWER_CONSTRAINT_VIOLATED) {
	    		String msg = model.getCurrentQuestionPrompt().getConstraintText();
	    		J2MEDisplay.showError(null, msg != null ? msg : PROMPT_DEFAULT_CONSTRAINT_VIOL);
	     	} else {
	     		step(controller.stepToNextEvent());
	     	}
    	}
    }
    
    public void questionIndexChanged (FormIndex questionIndex) {
   		jumpToQuestion(questionIndex);
    }    
    
	public void saveStateChanged (int instanceID, boolean dirty) {
		//do nothing
	}
	
    public void keyPressed(int keyCode) {
    	FormIndex keyDownSelectedWidget = this.activeQuestionIndex;
    	super.keyPressed(keyCode);
    	if(multiLingual && keyCode == LANGUAGE_CYCLE_KEYCODE) {
    		//TODO: CYCLE LANGUAGES
    		//this.cyclelanguagesomehow
    	} else if (keyCode == KEY_CENTER_LETS_HOPE) {
    		if (keyDownSelectedWidget == this.activeQuestionIndex) {
				ChatterboxWidget widget = activeFrame();
				if (widget != null) {
					widget.UIHack(UIHACK_SELECT_PRESS);
				}
			}
        	indexWhenKeyPressed = keyDownSelectedWidget;
    	}
    }
    
    public void keyReleased(int keyCode) {
    	if(keyCode == KEY_CENTER_LETS_HOPE && !(indexWhenKeyPressed == this.activeQuestionIndex)) {
    		//The previous select keypress was for a different item.
    	} else {
    		//#if javarosa.supresscycle
    		if(keyCode != LANGUAGE_CYCLE_KEYCODE) {
        		super.keyReleased(keyCode);
    		}
    		//#else
    		super.keyReleased(keyCode);
    		//#endif
    	}
    }

	public Object getScreenObject() {
		return this;
	}
	
    public ChatterboxWidget getWidgetAtIndex(int index) {
    	return (ChatterboxWidget)get(index);
    }
    
	private void computeHeaders() {
		int threshold = this.contentY;
		//bar.clearSpans();
		
		// Clayton Sims - Apr 3, 2009 : Removed this code. Tested on the MediaControlSkin
		// and the 3110c, and it seems to be behaving correctly now. this should all
		// be handled by this.contentY above.
		//if(this.topFrame != null && this.topFrame.size() != 0) {
		//	threshold += this.topFrame.getContentHeight();	
		//}
		Vector headers = new Vector();
    	for (int i = 0; i < size(); i++) {
    		ChatterboxWidget cw = getWidgetAtIndex(i);
    		
    		//If the widget is in the screen area
    		if(cw.getAbsoluteY() + cw.getContentHeight()  > threshold ) {
    			//if(i > 7 && i < 25) {
    				//bar.addSpan(cw.getAbsoluteY() + threshold, cw.getAbsoluteY() + cw.getContentHeight() + threshold);
    			//}
    		}
    		
    		// Test for whether this is a header, and should be pinned to the top of the screen because it is above
    		// the visible area
    		if(cw.isPinned()) {
    			//if(cw.getAbsoluteY() + cw.getContentHeight() < threshold ) {
	    		if(cw.getAbsoluteY() + cw.getPinnableHeight() < threshold ) {
	    	    	headers.addElement(cw.generateHeader());
	    		}
    		}
    	}

    	Item[] newHeaders = new Item[headers.size()];
    	headers.copyInto(newHeaders);
    	if((this.topFrame == null && headers.size() > 0) || (this.topFrame != null && !itemArraysEqual(newHeaders, this.topFrame.getItems()))) {
        	if(topFrame != null) {
        		this.topFrame.clear();
        	}
        	for(int i = 0 ; i < newHeaders.length ; ++i ) {
        		append(Graphics.TOP, newHeaders[i]);
        	}
        	if(newHeaders.length == 0) {
        		this.calculateContentArea(0, 0,this.getWidth(), this.getHeight());
        	}
        	
        	if(this.topFrame != null) {
        		// Clayton Sims - Apr 3, 2009 
        		//Nuclear Option: Just figure everything out again.
        		//Might slow down bad phones. Not sure yet.
        		this.topFrame.requestFullInit();
        	}
    	}
    }
	
    private boolean itemArraysEqual(Item[] array1, Item[] array2) {
		if(array1.length != array2.length) {
			return false;
		}
		boolean retVal = true;
		for(int i = 0 ; i < array1.length ; ++i ) {
			if(array1[i] instanceof StringItem && array2[i] instanceof StringItem) {
				return ((StringItem)array1[i]).getText().equals(((StringItem)array2[i]).getText());
			} else {
				if (!array1[i].equals(array2[i])) {
					retVal = false;
				}
			}
		}
		return retVal;
	}
	
	/* (non-Javadoc)
	 * @see de.enough.polish.ui.Screen#paint(javax.microedition.lcdui.Graphics)
	 */
	public void paint(Graphics g) {
		super.paint(g);
		//There are a lot of things that will need to happen after sizes for dynamic
		//things have been computed. These should happen after a paint.
		
		computeHeaders();
		//bar.setHeight(this.getAvailableHeight());
		//bar.requestInit();
	}

	private void backFromCamera() {
		// TODO Auto-generated method stub
		System.out.println("And we're back...");	
	}

	private void doCapture() {
		// TODO Auto-generated method stub
		System.out.println("Click!");
	}

	/*
	private void handleException(Exception e) {
		Alert a = new Alert("Exception", e.toString(), null, null);
		a.setTimeout(Alert.FOREVER);
		//JavaRosaServiceProvider.instance().getDisplay().setCurrent(a, mMainForm);
	}*/
}