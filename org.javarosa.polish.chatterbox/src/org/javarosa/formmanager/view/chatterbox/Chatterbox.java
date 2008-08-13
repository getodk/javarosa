//conditional include for polish?

package org.javarosa.formmanager.view.chatterbox;

import java.util.Enumeration;

import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.Graphics;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.formmanager.activity.FormEntryContext;
import org.javarosa.formmanager.controller.FormEntryController;
import org.javarosa.formmanager.model.FormEntryModel;
import org.javarosa.formmanager.utility.FormEntryModelListener;
import org.javarosa.formmanager.utility.SortedIntSet;
import org.javarosa.formmanager.view.IFormEntryView;
import org.javarosa.formmanager.view.chatterbox.util.ChatterboxContext;
import org.javarosa.formmanager.view.chatterbox.widget.ChatterboxWidget;
import org.javarosa.formmanager.view.chatterbox.widget.ChatterboxWidgetFactory;
import org.javarosa.formmanager.view.chatterbox.widget.IWidgetStyle;

import de.enough.polish.ui.Alert;
import de.enough.polish.ui.Container;
import de.enough.polish.ui.FramedForm;
import de.enough.polish.ui.UiAccess;

public class Chatterbox extends FramedForm implements IFormEntryView, FormEntryModelListener, CommandListener {
	private static final int INDEX_NOT_SET = -1;
	private static final int LANGUAGE_CYCLE_KEYCODE = Canvas.KEY_POUND;
	
	private static final String PROMPT_REQUIRED_QUESTION = "Required question; you must answer";
	
    public static int KEY_CENTER_LETS_HOPE = -5;
    
    public static final int UIHACK_SELECT_PRESS = 1;
	
	private FormEntryController controller;
    private FormEntryModel model;
    
    private ChatterboxWidgetFactory widgetFactory;
    private boolean multiLingual;
    private SortedIntSet questionIndexes;
    private int activeQuestionIndex;
    
    /** The active question's index when a key is pressed */
    private int indexWhenKeyPressed = -1;
    
    //GUI elements
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
    	controller.setView(this);

    	widgetFactory = new ChatterboxWidgetFactory(this);
    	multiLingual = (model.getForm().getLocalizer() != null);
    	questionIndexes = new SortedIntSet();
    	activeQuestionIndex = INDEX_NOT_SET;
    	
    	model.registerObservable(this);
    	
    	initGUI();
    	
    	//#if device.identifier == Sony-Ericsson/P1i
    	KEY_CENTER_LETS_HOPE = 13;
    	//#endif
    }

    public void destroy () {
    	for (int i = 0; i < size(); i++) {
    		((ChatterboxWidget)get(i)).destroy();
    	}
    	
    	model.unregisterObservable(this);
    }
    
    public void show () {
    	controller.setDisplay(this);
    }
    
    private void initGUI () {
    	setUpCommands();
    	initProgressBar();
    	jumpToQuestion(model.getQuestionIndex());
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
        progressBar = new Gauge(null, false, model.getNumQuestions(), 0);
        append(Graphics.BOTTOM, progressBar);
    }
    
    //make given question active; deal with all necessary questions in between
    private void jumpToQuestion (int questionIndex) {
    	if (questionIndex != INDEX_NOT_SET && !model.isRelevant(questionIndex))
    		throw new IllegalStateException();
    	    	
    	if (questionIndex > activeQuestionIndex) {
    		if (activeQuestionIndex != INDEX_NOT_SET) {
    			((ChatterboxWidget)get(questionIndexes.indexOf(activeQuestionIndex, true))).setViewState(ChatterboxWidget.VIEW_COLLAPSED);
    		}
    			
    		for (int i = activeQuestionIndex + 1; i <= questionIndex; i++) {
    			putQuestion(i, i == questionIndex);
    		}
    	} else if (questionIndex <= activeQuestionIndex) {
    		for (int i = activeQuestionIndex; i > questionIndex; i--) {
    			removeFrame(i);
    		}
    		
    		if (questionIndex != INDEX_NOT_SET) {
    			((ChatterboxWidget)get(questionIndexes.indexOf(questionIndex, true))).setViewState(ChatterboxWidget.VIEW_EXPANDED);    
    		}
    	}
    	
    	if (activeQuestionIndex != questionIndex) {
    		activeQuestionIndex = questionIndex;

    		ChatterboxWidget widget = (ChatterboxWidget)get(questionIndexes.indexOf(activeQuestionIndex, true));
    		
    		this.focus(widget, true);
    		widget.showCommands();

    		progressBar.setValue(questionIndex);
    		
    	}
    	
    	//UI hacks ho!
    	babysitStyles();
    }
    
    //create a frame for a question and show it at the appropriate place in the form
    private void putQuestion (int questionIndex, boolean expanded) {
    	if (model.isRelevant(questionIndex)) {
    		ChatterboxWidget cw = widgetFactory.getWidget(model.getQuestion(questionIndex), model.getForm(),
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
    	if (frameIndex == -1)
    		return; //question not present in chatterbox (hidden/non-relevant)
    	
    	ChatterboxWidget cw = (ChatterboxWidget)get(frameIndex);
    	cw.destroy();
    	delete(frameIndex);
    }
    
    public void formComplete () {
    	activeFrame().setViewState(ChatterboxWidget.VIEW_COLLAPSED);
    	babysitStyles();
		progressBar.setValue(model.getNumQuestions());
		
		repaint();
		try {
			Thread.sleep(1000); //let them bask in their completeness
		} catch (InterruptedException ie) { }
			
		controller.save();
		controller.exit();
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
    			//#style split
    			UiAccess.setStyle(cw);
    			break;
    		case ChatterboxWidget.VIEW_EXPANDED:
    			//#style container
    			UiAccess.setStyle(cw);
    			break;
    		}
    	}
    }
    
    public void commandAction(Command command, Displayable s) {
    	System.out.println("cbox: command action");
    	
    	if (command == backCommand) {
    		System.out.println("back");
    		controller.stepQuestion(false);
    	} else if (command == exitNoSaveCommand) {
    		controller.exit();
    	} else if (command == exitSaveCommand) {
    		commitAndSave();
    		controller.exit();
    	} else if (command == saveCommand) {
    		commitAndSave();
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

    private void commitAndSave () {
       	ChatterboxWidget frame = activeFrame();
    	if (frame != null) {
    		controller.commitAnswer(frame.getQuestion(), frame.getData());
    	}
    	controller.save();
    }
    
    public void questionAnswered () {
    	ChatterboxWidget frame = activeFrame();
	
    	if (controller.questionAnswered(frame.getQuestion(), frame.getData()) == FormEntryController.QUESTION_REQUIRED_BUT_EMPTY) {
    		showError(null, PROMPT_REQUIRED_QUESTION);
    	}
    }
    
    public void questionIndexChanged (int questionIndex) {
    	if (questionIndex != INDEX_NOT_SET)
    		jumpToQuestion(questionIndex);
    }    
    
	public void saveStateChanged (int instanceID, boolean dirty) {
		//do nothing
	}
	
    public void keyPressed(int keyCode) {
    	int keyDownSelectedWidget = this.activeQuestionIndex;
    	super.keyPressed(keyCode);
    	if(multiLingual && keyCode == LANGUAGE_CYCLE_KEYCODE) {
    		controller.cycleLanguage();
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
    		super.keyReleased(keyCode);
    	}
    }

    //good utility function
    private void showError(String title, String message) {
    	//#style mailAlert
    	Alert alert = new Alert(title, message, null, AlertType.ERROR);
    	alert.setTimeout(Alert.FOREVER);
    	//alert.setCommandListener?
    	Alert.setCurrent(JavaRosaServiceProvider.instance().getDisplay(), alert, null);
    }

	public void setContext(FormEntryContext context) {
		if(context instanceof ChatterboxContext && context != null) {
			ChatterboxContext cbcontext = (ChatterboxContext)context;
			Enumeration en = cbcontext.getCustomWidgets().elements();
			while(en.hasMoreElements()) {
				IWidgetStyle widget = (IWidgetStyle)en.nextElement();
				this.widgetFactory.registerExtendedWidget(widget.widgetType(), widget);
			}
		}
	}
}