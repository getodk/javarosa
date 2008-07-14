//conditional include for polish?

package org.javarosa.formmanager.view.chatterbox;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.AlertType;

import org.javarosa.core.*;
import org.javarosa.formmanager.controller.*;
import org.javarosa.formmanager.model.*;
import org.javarosa.formmanager.utility.*;
import org.javarosa.formmanager.view.*;
import org.javarosa.formmanager.view.chatterbox.widget.*;
import org.javarosa.polishforms.SubmitScreen;

import de.enough.polish.ui.*;

public class Chatterbox extends FramedForm implements IFormEntryView, FormEntryModelListener, CommandListener {
	private static final int INDEX_NOT_SET = -1;
	private static final int LANGUAGE_CYCLE_KEYCODE = Canvas.KEY_POUND;
	
	private static final String PROMPT_REQUIRED_QUESTION = "Required question; you must answer";
	
	private FormEntryController controller;
    private FormEntryModel model;
    
    private ChatterboxWidgetFactory widgetFactory;
    private boolean multiLingual;
    private SortedIntSet questionIndexes;
    private int activeQuestionIndex;
    
    //GUI elements
    private Command selectCommand;
    private Command backCommand;
    private Command exitNoSaveCommand;
    private Command exitSaveCommand;
    private Command saveCommand;
    private Command languageSubMenu;
    private Command[] languageCommands;
    private Gauge progressBar;
        
    public Chatterbox (String formTitle, FormEntryModel model, FormEntryController controller) {
        //#style framedForm
    	super(formTitle);
    	
    	this.model = model;
    	this.controller = controller;

    	widgetFactory = new ChatterboxWidgetFactory();
    	multiLingual = (model.getForm().getLocalizer() != null);
    	questionIndexes = new SortedIntSet();
    	activeQuestionIndex = INDEX_NOT_SET;
    	
    	model.registerObservable(this);
    	
    	initGUI();
    }

    public void destroy () {
    	model.unregisterObservable(this);
    }
    
    private void initGUI () {
    	setUpCommands();
    	initProgressBar();
    	jumpToQuestion(model.getQuestionIndex());
    }
    
    private void setUpCommands () {
        selectCommand = new Command("Select", Command.SCREEN, 1);
        backCommand = new Command("Back", Command.BACK, 2);
        exitNoSaveCommand = new Command("Exit", Command.EXIT, 4);
        exitSaveCommand = new Command("Save and Exit", Command.SCREEN, 4);
        saveCommand = new Command("Save", Command.SCREEN, 4);
        
        if (multiLingual) {
            languageSubMenu = new Command("Language", Command.SCREEN, 2);
        	populateLanguages();
        }
        
        addCommand(selectCommand);
        addCommand(backCommand);
        addCommand(exitNoSaveCommand);        
        addCommand(exitSaveCommand);
        addCommand(saveCommand);
        
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
        progressBar = new Gauge(null, false, 10 /*model.getForm().getNumQuestions()*/, 0);
        append(Graphics.BOTTOM, progressBar);
    }
    
    //make given question active; deal with all necessary questions in between
    private void jumpToQuestion (int questionIndex) {
    	if (questionIndex != INDEX_NOT_SET && !model.isRelevant(questionIndex))
    		throw new IllegalStateException();
    	    	
    	if (questionIndex > activeQuestionIndex) {
    		if (activeQuestionIndex != INDEX_NOT_SET)
    			((ChatterboxWidget)get(activeQuestionIndex)).setViewState(ChatterboxWidget.VIEW_COLLAPSED);
    		
    		for (int i = activeQuestionIndex + 1; i <= questionIndex; i++) {
    			putQuestion(i, i == questionIndex);
    		}
    	} else if (questionIndex <= activeQuestionIndex) {
    		for (int i = activeQuestionIndex; i > questionIndex; i++) {
    			removeFrame(i);
    		}
    		
    		if (questionIndex != INDEX_NOT_SET)
    			((ChatterboxWidget)get(questionIndex)).setViewState(ChatterboxWidget.VIEW_EXPANDED);    		
    	}
    	
    	activeQuestionIndex = questionIndex;
    	progressBar.setValue(questionIndex);
    }
    
    //create a frame for a question and show it at the appropriate place in the form
    private void putQuestion (int questionIndex, boolean expanded) {
    	if (model.isRelevant(questionIndex)) {
    		ChatterboxWidget cw = widgetFactory.getWidget(model.getQuestion(questionIndex), 
    													  expanded ? ChatterboxWidget.VIEW_EXPANDED
    													    	   : ChatterboxWidget.VIEW_COLLAPSED);
    		putFrame(cw, questionIndex);
    	}
    }
    
    //insert a chatterbox widget into the form at the appropriate place
    private void putFrame (ChatterboxWidget widget, int questionIndex) {
    	int frameIndex = questionIndexes.add(questionIndex);
    	insert(frameIndex, widget);
    }
    
    //remove the frame corresponding to a particular question from display
    private void removeFrame (int questionIndex) {
    	int frameIndex = questionIndexes.remove(questionIndex);
    	    	
    	ChatterboxWidget cw = (ChatterboxWidget)get(frameIndex);
    	cw.destroy();
    	delete(frameIndex);
    }
    
    public void formComplete () {
    	activeFrame().setViewState(ChatterboxWidget.VIEW_COLLAPSED);
    	//notify controller about being done
    }
    
    private ChatterboxWidget activeFrame () {
    	int frameIndex = questionIndexes.indexOf(activeQuestionIndex, true);
    	if (frameIndex == -1)
    		return null;
    	else
    		return (ChatterboxWidget)get(frameIndex);
    }
    
    public void commandAction(Command command, Displayable s) {
    	if (command == selectCommand) {
    		ChatterboxWidget frame = activeFrame();
    		
    		if (controller.questionAnswered(frame.getQuestion(), frame.getData()) == FormEntryController.QUESTION_REQUIRED_BUT_EMPTY) {
    			showError(null, PROMPT_REQUIRED_QUESTION);
    		}
    	} else if (command == backCommand) {
    		controller.stepQuestion(true);
    	} else if (command == exitNoSaveCommand) {
    		controller.exit();
    	} else if (command == exitSaveCommand) {
    		controller.save();
    		controller.exit();
    	} else if (command == saveCommand) {
    		controller.save();
    	} else {
    		String language = null;
    		for (int i = 0; i < languageCommands.length; i++) {
    			if (command == languageCommands[i]) {
    				language = command.getLabel();
    				break;
    			}
    		}
    		
    		if (language != null) {
    			controller.setLanguage(language);
    		} else {
    			System.err.println("Chatterbox: Unknown command event received [" + command.getLabel() + "]");
    		}
    	}
    }    	

    //we might need this
    public void itemStateChanged (Item item) {
    	if (item instanceof ChatterboxWidget && (ChatterboxWidget)item == activeFrame() /*&& autoPlayable()*/) {
    		commandAction(selectCommand, this); //calling into another event handler is bad form
    	}
    }
    
    public void questionIndexChanged (int questionIndex) {
    	jumpToQuestion(questionIndex);
    }    

    public void keyPressed(int keyCode) {
    	super.keyPressed(keyCode);

    	if(multiLingual && keyCode == LANGUAGE_CYCLE_KEYCODE)
    		controller.cycleLanguage();
    }

    //good utility function
    private void showError(String title, String message) {
    	//#style mailAlert
    	Alert alert = new Alert(title, message, null, AlertType.ERROR);
    	alert.setTimeout(Alert.FOREVER);
    	//alert.setCommandListener?
    	Alert.setCurrent(JavaRosaServiceProvider.instance().getDisplay(), alert, null);
    }
}
    
//display send now? screens -- these belong outside chatterbox, activity's responsibility to overlay on finished chatterbox displayable
//itemstatelistener? autoplayability