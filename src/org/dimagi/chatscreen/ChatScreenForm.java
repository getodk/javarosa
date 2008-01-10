package org.dimagi.chatscreen;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;

import minixpath.XPathExpression;

import org.celllife.clforms.api.IForm;
import org.celllife.clforms.api.Prompt;
import org.celllife.clforms.storage.Model;
import org.celllife.clforms.util.J2MEUtil;

import org.dimagi.utils.ViewUtils;
import org.dimagi.view.Component;
import org.dimagi.view.IRefreshListener;
import org.dimagi.view.NavBar;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;

import de.enough.polish.util.VectorIterator;

/**
 * The base container for the Chat Screen interface.
 * 
 * The ChatScreenForm is responsible for containing and laying out frames,
 * as well as the control logic used for navigation of protocols.
 * 
 * It is the primary interface between the Protocol object and the View.
 * 
 * @author ctsims
 *
 */
public class ChatScreenForm extends DForm implements IForm {

	//TODO: Add (...) objects to the top and bottom of the display to signal
	//that there are frames above or below the current view.
	
	private Vector frameSet = new Vector();
	//private Vector prompts = new Vector();
	private Vector prompts;
	int activeQuestion = 0;
	int totalQuestions = 1;
	private String name;
	private Model xmlModel;
	private int recordId;
	
	/**
	 * Creates a new ChatScreen Form
	 */
	public ChatScreenForm() {
		setupComponents();
//		definePrompts();
	}

	public ChatScreenForm(Vector prompts) {
		this.prompts = prompts;
		setupComponents();
//		this.activeQuestion = activeQuestion;
		Enumeration itr = prompts.elements();
		while ( itr.hasMoreElements() ) {
			addPrompt((Prompt)itr.nextElement());
		}
	}
	
	public ChatScreenForm(Vector prompts, int activeQuestion, int totalQuestions) {
		System.out.println("ChatScreenForm(activeQuestion)");
		setupComponents();
//		this.activeQuestion = activeQuestion;
//		Enumeration itr = prompts.elements();
//		while ( itr.hasMoreElements() ) {
//			addPrompt((Prompt)itr.nextElement());
//		}
	}
	
	private void definePrompts() {
		Prompt first = new Prompt();
		first.setLongText("Enter the patient's ID number:");
		first.setShortText("ID");
		first.setFormControlType(Constants.TEXTBOX);
		prompts.addElement((Object) first);		
		addPrompt(first);
		Prompt second = new Prompt();
		second.setLongText("Enter the patient's ID number:");
		second.setShortText("ID");
		second.setFormControlType(Constants.TEXTBOX);
		prompts.addElement((Object) second);		
		
//		Question third = new Question(
//				"Has the patient had any of the following symptoms since their last visit?", "Symptoms",
//				Constants.MULTIPLE_CHOICE, new String[] { "Fever",
//						"Night Sweats", "Weight Loss", "Vomiting" },
//				Constants.LABEL_LEFT);
//		questions.addElement((Object)third);
//		Question fourth = new Question("Name of the city?", "City", Constants.DROPDOWN, 
//				new String[] {"Cambridge", "Boston", "Newton", "Quincy", "Brookline"}, Constants.LABEL_TOP);
//		questions.addElement((Object) fourth);	
	}
	
	/**
	 * Lays out the static components for the form
	 */
	private void setupComponents() {
		int width = this.getWidth();
		int height = this.getHeight();
		int frameCanvasHeight = height - (height / 11);
		getContentComponent().setBackgroundColor(ViewUtils.GREY);
	}

	/**
	 * Pushes a new question onto the stack, setting all other 
	 * questions to inactive status, and displaying the new question
	 * to the user.
	 * 
	 * @param theQuestion The new question to be displayed
	 */
	public void addPrompt(Prompt p) {
		System.out.println("ChatScreenForm.addPrompt()");
		Frame newFrame = new Frame(p);
		newFrame.setWidth(this.getWidth());
		frameSet.addElement(newFrame);
		getContentComponent().add(newFrame);
		setupFrames();
		this.repaint();
	}
	
	public void goToNextPrompt() {
		System.out.println("ChatScreenForm.goToNextPrompt()");
//		activeQuestion++;
//		// add a new question
//		if (activeQuestion == totalQuestions) {
//			if ( activeQuestion < prompts.size() ) {
//				totalQuestions++;
//				addPrompt((Prompt)prompts.elementAt(activeQuestion));
//			} else { // repeat questions in loop
//			    totalQuestions++;
//				addPrompt((Prompt)prompts.elementAt(activeQuestion % 4));
//			}
//		} else { // advance to question that's already there
//			getContentComponent().add((Frame)frameSet.elementAt(activeQuestion));
//			setupFrames();
//		}
	}
	
	public void goToPreviousPrompt() {
		// Don't do anything if user hits prev command for first question
		if (activeQuestion > 0) {
			getContentComponent().remove((Frame)frameSet.elementAt(activeQuestion));
			activeQuestion--;
			setupFrames();
		}
	}

	/**
	 * Queries all frames for their optimal size, and then lays them out
	 * in a simple stack.
	 */
	private void setupFrames() {
		int frameCanvasHeight = getContentComponent().getHeight()
				- (getContentComponent().getHeight() / 11);

		int frameStart = frameCanvasHeight;
		for (int i=activeQuestion; i >=0; i--) {
			Frame aFrame = (Frame) frameSet.elementAt(i);
			if ( i == activeQuestion ) {
				aFrame.setActiveFrame(true);
			} else {
				aFrame.setActiveFrame(false);
			}
			frameStart -= aFrame.getHeight();
			aFrame.setY(frameStart);
		}
	}
	
	// IForm interface
	public Prompt getPrompt(int promptId) {
		return (Prompt) prompts.elementAt(promptId);
	}

	public Vector getPrompts() {
		return prompts;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Model getXmlModel() {
		return xmlModel;
	}

	public void setXmlModel(Model model) {
		this.xmlModel = model;
	}

	public void calculateRelavant(Prompt p) {
		
		//System.out.println("calcRel of: " + p.getLongText());
		if (p.getRelevantString()== null)
			p.setRelevant(true);
		else{
			XPathExpression xpls = new XPathExpression(xmlModel.getXmlModel().getRootElement(), p.getRelevantString());
			Vector result = xpls.getResult();
			for (Enumeration e = result.elements(); e.hasMoreElements();) {
				Object obj = e.nextElement();
				if (obj instanceof Element){
					for (int i = 0; i < ((Element)obj).getChildCount(); i++) {
						if (((Element)obj).getType(i)== Node.TEXT){
							//System.out.println(" val:"+((Element)obj).getText(i));		
							xpls.getOperation().setValue(((Element)obj).getText(i));
						}
					}
				}
			}
			if (xpls.getOperation().getValue()!= null){
				xpls.getOperation().setArgumentType(p.getReturnType());
				boolean relevancy = xpls.getOperation().evaluateBooleanOperation();
				p.setRelevant(relevancy);
				if (relevancy == false){
					p.setValue(null);
					updateModel(p);
				}
			}else
				p.setRelevant(false);
		}
	}
	
	public void calculateRelevantAll(){
		for (int i = 0; i < prompts.size(); i++) {
			calculateRelavant((Prompt) prompts.elementAt(i)); 
		}
	}

	public void setRecordId(int recordId) {

		this.recordId = recordId;
	}

	public int getRecordId() {
		return recordId;
	}
	
	public void setShortForms() {
		// TODO get Short forms properly from XForm designer
		Enumeration e = prompts.elements();
		while (e.hasMoreElements()) {
			Prompt elem = (Prompt) e.nextElement();
			elem.setShortText(elem.getBindID());
			//System.out.println(elem.getLongText()+"="+elem.getShortText()+"="+elem.getBindID());
		}
	}
	
	public void updatePromptsValues() {
		// TODO COMBINE this with update model somehow as they are doing similar things
		Enumeration e = prompts.elements();
		while (e.hasMoreElements()) {
			Prompt elem = (Prompt) e.nextElement();
			if (elem.getValue() == null){
				updatePrompt(elem, false);
			}
		}
	}
	
	/**
	 *  Populates the XFPrompts with the data contained in the xmlModel
	 */
	public void updatePromptsDefaults() {
		// TODO COMBINE this with update model somehow as they are doing similar things
		Enumeration e = prompts.elements();
		while (e.hasMoreElements()) {
			Prompt elem = (Prompt) e.nextElement();
			if (elem.getValue() == null){
				updatePrompt(elem, true);
			}
		}
	}

	private void updatePrompt(Prompt prompt, boolean defaultVal) {
		String xpath = prompt.getXpathBinding();
		String value;
		
		XPathExpression xpls = new XPathExpression(xmlModel.getXmlModel(), xpath);
		Vector result = xpls.getResult();
		
		// log 		System.out.println("Updating prompt: " + xpath);
		
		for (Enumeration e = result.elements(); e.hasMoreElements();) {
			Object obj = e.nextElement();
			if (obj instanceof Element){
				Element node = (Element) obj;
				for (int i = 0; i < node.getChildCount(); i++) 
					if (node.getType(i) == Node.TEXT) {
						value = node.getText(i);//
						if (defaultVal)
							prompt.setDefaultValue(value);
						else {
							prompt.setValue(value);
							//System.out.println("Updating prompt: " + value);
						}
					}			
			}
		}
	}

	/**
	 * Populates the xmlModel with the data contained in the XFPrompts
	 */
	public void populateModel() {
		Enumeration e = prompts.elements();
		while (e.hasMoreElements()) {
			Prompt elem = (Prompt) e.nextElement();
			if (elem.getValue() != null){
				updateModel(elem);
			}
		}
	}

	/**
	 * Updates the xmlModel with the data in a particular prompt.
	 * 
	 * @param prompt
	 */
	public void updateModel(Prompt prompt) {
		
		String xpath = prompt.getXpathBinding();
		String value = J2MEUtil.getStringValue(prompt.getValue(), prompt.getReturnType());
		
		//System.out.println("Updating Model"+prompt.getXpathBinding()+" - "+value);		
		if (value != null) {
			XPathExpression xpls = new XPathExpression(xmlModel.getXmlModel(), xpath);
			Vector result = xpls.getResult();
			
			//System.out.println("XPath result.size()"+result.size());
			for (Enumeration e = result.elements(); e.hasMoreElements();) {
				Object obj = e.nextElement();
				if (obj instanceof Element){
					boolean textfound = false;
					//System.out.println(((Element)obj).getName()+" kids: "+((Element)obj).getChildCount());
					for (int i = 0; i < ((Element)obj).getChildCount(); i++) {
						if (((Element)obj).getType(i) == Node.TEXT){
							((Element) obj).removeChild(i);
							((Element) obj).addChild(i,Node.TEXT, value);	
							//System.out.println("added1 "+value);
							textfound = true;
							break;
						}						
					}
					if (!textfound){
						((Element) obj).addChild(Node.TEXT, value);	
						//System.out.println("added2 "+value);						
					}
				}
			}
		}
	}
	
	public void loadPromptsDefaultValues()
	{
		for (int i = 0; i < this.prompts.size(); i++)
		{
			this.getPrompt(i).setValue(this.getPrompt(i).getDefaultValue());
		}
	}

	public void setPrompts(Vector prompts) {
		this.prompts = prompts;
	}
	
}
		