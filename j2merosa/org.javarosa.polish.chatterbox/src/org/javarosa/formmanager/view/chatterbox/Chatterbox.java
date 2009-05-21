//conditional include for polish?

package org.javarosa.formmanager.view.chatterbox;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.Graphics;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.Constants;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.formmanager.activity.FormEntryContext;
import org.javarosa.formmanager.controller.FormEntryController;
import org.javarosa.formmanager.model.FormEntryModel;
import org.javarosa.formmanager.utility.FormEntryModelListener;
import org.javarosa.formmanager.utility.SortedIndexSet;
import org.javarosa.formmanager.view.IFormEntryView;
import org.javarosa.formmanager.view.chatterbox.util.ChatterboxContext;
import org.javarosa.formmanager.view.chatterbox.widget.ChatterboxWidget;
import org.javarosa.formmanager.view.chatterbox.widget.ChatterboxWidgetFactory;
import org.javarosa.formmanager.view.chatterbox.widget.CollapsedWidget;
import org.javarosa.formmanager.view.chatterbox.widget.IWidgetStyle;

import de.enough.polish.ui.Alert;
import de.enough.polish.ui.Container;
import de.enough.polish.ui.FramedForm;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.UiAccess;

//#if javarosa.usepolishlocalisation
//# import de.enough.polish.util.Locale;
//#endif


public class Chatterbox extends FramedForm implements IFormEntryView, FormEntryModelListener, CommandListener{
	private static int LANGUAGE_CYCLE_KEYCODE = Canvas.KEY_POUND;
	
    //#if javarosa.usepolishlocalisation
    //# private static final String PROMPT_REQUIRED_QUESTION = Locale.get("view.sending.RequiredQuestion");
    //#else
    private static final String PROMPT_REQUIRED_QUESTION = "Required question; You must answer";
    //#endif

	private static final String PROMPT_DEFAULT_CONSTRAINT_VIOL = "Answer is outside of the allowed range";
	
    public static int KEY_CENTER_LETS_HOPE = -5;
    
    public static final int UIHACK_SELECT_PRESS = 1;
	
	private FormEntryController controller;
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
        
    public Chatterbox (String formTitle, FormEntryModel model, FormEntryController controller) {
        //#style framedForm
    	super(formTitle);
    	
    	if(model.isReadOnly()) {
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
    	
    	this.model = model;
    	this.controller = controller;
    	controller.setFormEntryView(this);

    	widgetFactory = new ChatterboxWidgetFactory(this);
    	widgetFactory.setReadOnly(this.model.isReadOnly());
    	multiLingual = (model.getForm().getLocalizer() != null);
    	questionIndexes = new SortedIndexSet();
    	activeQuestionIndex = FormIndex.createBeginningOfFormIndex(); //null is not allowed
    	
    	model.registerObservable(this);
    	
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
    	
    	model.unregisterObservable(this);
    }
    
    public void show () {
    	controller.setView(this);
    }
    
    private void initGUI () {
    	setUpCommands();
    	initProgressBar();
    	
    	//Mode 1: Read only review screen.
    	if(model.isReadOnly()) {
    		while(!model.getQuestionIndex().isEndOfFormIndex()) {
    			controller.stepQuestion(true);
    		}
    	} else if(model.getStartIndex() != null) {
    		
    		//Mode 2: Seek to current question
    		while(!model.getQuestionIndex().equals(model.getStartIndex())) {
    			controller.stepQuestion(true);
    		}
    	} else {
    		//Default Mode: Start at first question
    		controller.stepQuestion(true);
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
        
        if(!model.isReadOnly()) {
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
    
    //make given question active; deal with all necessary questions in between
    private void jumpToQuestion (FormIndex questionIndex) {
    	boolean newRepeat = false;
    	
    	if (questionIndex.isInForm() && !model.isRelevant(questionIndex))
			throw new IllegalStateException();
    	
		// Determine what should and shouldn't be pinned.
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
    	//figure out kind of reference and how to handle it
    	Vector defs = model.getForm().explodeIndex(questionIndex);
    	IFormElement last = (defs.size() == 0 ? null : (IFormElement)defs.lastElement());
    	if (last instanceof GroupDef) {
    		if (((GroupDef)last).getRepeat() &&
    			model.getForm().getDataModel().resolveReference(model.getForm().getChildInstanceRef(questionIndex)) == null) {
    			//new repeat
    			newRepeat = true;
    		} else {
    			boolean forwards = questionIndex.compareTo(activeQuestionIndex) > 0;
    			if(forwards) {
    				createHeaderForElement(questionIndex);
    			} else {
    				removeHeaderForElement(questionIndex);
    			}
    			controller.stepQuestion(forwards);
    			return;
    		}
    	} else if (questionIndex.isInForm() && model.isReadonly(questionIndex)) {
			boolean forwards = questionIndex.compareTo(activeQuestionIndex) > 0;
			controller.stepQuestion(forwards);
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
    			ChatterboxWidget widget = (ChatterboxWidget)get(questionIndexes.indexOf(activeQuestionIndex, true));
    		
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
    			
    			this.focus(widget, true);
    			//this.
    			widget.showCommands();
    		}
    			
    		//FIXME: no!
    		progressBar.setMaxValue(model.getNumQuestions());
    		progressBar.setValue(questionIndexes.size());    		
    	}
    	
    	//UI hacks ho!
    	babysitStyles();
    }
    

	private void createHeaderForElement(FormIndex questionIndex) {
    	ChatterboxWidget headerWidget = widgetFactory.getNewLabelWidget(questionIndex, model.getForm(), this);
		if(headerWidget != null) {
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
    		cw = widgetFactory.getNewRepeatWidget(questionIndex, model.getForm(), this);
    		activeIsInterstitial = true;
    	} else if (model.getForm().explodeIndex(questionIndex).lastElement() instanceof GroupDef) {
    		//do nothing
    	} else if (model.isRelevant(questionIndex)) { //FIXME relevancy check
    		cw = widgetFactory.getWidget(questionIndex, model.getForm(),
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
    	if(!model.isReadOnly()) {
	    	jumpToQuestion(FormIndex.createEndOfFormIndex());
	    	babysitStyles();
			progressBar.setValue(progressBar.getMaxValue());
			
			repaint();
			try {
				Thread.sleep(1000); //let them bask in their completeness
			} catch (InterruptedException ie) { }
				
			controller.save();
			controller.exit();
    	} else { 
    		
    	}
    }
    
    public void startOfForm () {
    	controller.selectQuestion(activeQuestionIndex);
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
    			if(model.isReadOnly()) {
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
    	} else if (command.getLabel()== Constants.ACTIVITY_TYPE_GET_IMAGES) {
    		suspendActivity(command);
    	} else if (command.getLabel()== Constants.ACTIVITY_TYPE_GET_AUDIO) {
        	suspendActivity(command);
    	} else if (command.getLabel()== "Capture") {
    		doCapture();
    	} else if (command.getLabel()== "Back") {
    		backFromCamera();
    	} else if (command.getLabel().equals(CollapsedWidget.UPDATE_TEXT)) { //TODO: Put this static string in a better place.
    		//Return to shell providing the question index.
    		model.setQuestionIndex(this.questionIndexes.get(this.getCurrentIndex()));
    		controller.exit("update");
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

    
	private void suspendActivity(Command command) {
		controller.suspendActivity(command);
	}
    private void commitAndSave () {
       	ChatterboxWidget frame = (activeIsInterstitial ? null : activeFrame());
    	if (frame != null) {
    		controller.commitAnswer(frame.getBinding(), frame.getData());
    	}
    	controller.save();
    }
    
    public void questionAnswered () {
    	ChatterboxWidget frame = activeFrame();
	
    	if (activeIsInterstitial) {
    		//'new repeat?' answered
    		String answer = ((Selection)frame.getData().getValue()).getValue();
    		if (answer.equals("y")) {
    			controller.newRepeat(activeQuestionIndex);
    			createHeaderForElement(activeQuestionIndex);
    		}
    		controller.stepQuestion(true);
    	} else {
	    	int status = controller.questionAnswered(frame.getBinding(), frame.getData());
	    	if (status == FormEntryController.QUESTION_REQUIRED_BUT_EMPTY) {
	    		showError(null, PROMPT_REQUIRED_QUESTION);
	    	} else if (status == FormEntryController.QUESTION_CONSTRAINT_VIOLATED) {
	    		String msg = frame.getBinding().instanceNode.constraint.constraintMsg; //yikes
	    		showError(null, msg != null ? msg : PROMPT_DEFAULT_CONSTRAINT_VIOL);
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
    		//#if javarosa.supresscycle
    		if(keyCode != LANGUAGE_CYCLE_KEYCODE) {
        		super.keyReleased(keyCode);
    		}
    		//#else
    		super.keyReleased(keyCode);
    		//#endif
    	}
    }

    //good utility function
    private void showError(String title, String message) {
    	//#style mailAlert
    	Alert alert = new Alert(title, message, null, AlertType.ERROR);
    	alert.setTimeout(Alert.FOREVER);
    	//alert.setCommandListener(this);
    	
    	// Clayton Sims - Apr 27, 2009 : 
    	//Really not-thrilled with how this works. Unfortunately this is an instance of views not being able
    	//to propogate up new views. Maybe there should be a centralized way to use alerts? Or are we fine
    	//with using the existing elements?
    	Alert.setCurrent((Display)JavaRosaServiceProvider.instance().getDisplay().getDisplayObject(), alert, this);
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