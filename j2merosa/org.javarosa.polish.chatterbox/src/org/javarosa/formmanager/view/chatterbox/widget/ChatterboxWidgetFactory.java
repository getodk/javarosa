package org.javarosa.formmanager.view.chatterbox.widget;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.util.externalizable.PrototypeFactoryDeprecated;
import org.javarosa.formmanager.view.FormElementBinding;
import org.javarosa.formmanager.view.chatterbox.Chatterbox;

import de.enough.polish.ui.ChoiceGroup;

public class ChatterboxWidgetFactory {
	Chatterbox cbox;
	
	PrototypeFactoryDeprecated widgetFactory;
	
	boolean readOnly = false;
	
	public ChatterboxWidgetFactory (Chatterbox cbox) {
		widgetFactory = new PrototypeFactoryDeprecated();
		this.cbox = cbox;
	}
	
	public void registerExtendedWidget(int controlType, IWidgetStyle prototype) {
		widgetFactory.addNewPrototype(String.valueOf(controlType), prototype.getClass());
	}
	
	/**
	 * NOTE: Only applicable for Questions right now, not any other kind of IFormElement
	 * @param questionIndex
	 * @param form
	 * @param initViewState
	 * @return
	 */
	public ChatterboxWidget getWidget (FormIndex questionIndex, FormDef form, int initViewState) {
		IWidgetStyle collapsedStyle = null;
		IWidgetStyleEditable expandedStyle = null;
		
		FormElementBinding binding = new FormElementBinding(null, questionIndex, form);
		
		if(!(binding.element instanceof QuestionDef)) {
			throw new IllegalArgumentException("Only QuestionDefs can be currently resolved from getWidget()");
		}
		
		QuestionDef question = (QuestionDef)binding.element;
		
		int controlType = question.getControlType();
		int dataType = binding.instanceNode.dataType;
		
		String appearanceAttr = question.getAppearanceAttr();
		
		collapsedStyle = new CollapsedWidget();
		((CollapsedWidget)collapsedStyle).setSeekable(this.readOnly);
		
		switch (controlType) {
		case Constants.CONTROL_INPUT:
			switch (dataType) {
			case Constants.DATATYPE_INTEGER:
				expandedStyle = new NumericEntryWidget();
				break;
			case Constants.DATATYPE_DECIMAL:
				expandedStyle = new NumericEntryWidget(true);
				break;
			case Constants.DATATYPE_DATE:
				//#if javarosa.useNewDatePicker 
				expandedStyle = new SimpleDateEntryWidget();
				//expandedStyle = new InlineDateEntryWidget();
				//#else
				expandedStyle = new DateEntryWidget();
				//#endif
				break;
			case Constants.DATATYPE_TIME:
				expandedStyle = new TimeEntryWidget();
				break;
			}
			break;
		case Constants.CONTROL_SELECT_ONE:
			int style;

			if ("minimal".equals(appearanceAttr))
				style = ChoiceGroup.POPUP;
			else
				style = ChoiceGroup.EXCLUSIVE;

			expandedStyle = new SelectOneEntryWidget(style);
			break;
		case Constants.CONTROL_SELECT_MULTI:
			expandedStyle = new SelectMultiEntryWidget();
			break;
		case Constants.CONTROL_TEXTAREA:
			expandedStyle = new TextEntryWidget();
			break;
		case Constants.CONTROL_TRIGGER:
			expandedStyle = new MessageWidget();
			break;
		case Constants.CONTROL_IMAGE_CHOOSE:
			expandedStyle = new ImageChooserWidget();
			break;
		case Constants.CONTROL_AUDIO_CAPTURE:
			expandedStyle = new AudioCaptureWidget();
			break;	
		}

		if (expandedStyle == null) { //catch types text, null, unsupported
			expandedStyle = new TextEntryWidget();
			
			String name = String.valueOf(controlType); //huh? controlType is an int
			Object widget = widgetFactory.getNewInstance(name);
			if (widget != null) {
				expandedStyle = (IWidgetStyleEditable) widget;
			}
		}
		
		if (collapsedStyle == null || expandedStyle == null)
			throw new IllegalStateException("No appropriate widget to render question");
		
		return new ChatterboxWidget(cbox, binding, initViewState, collapsedStyle, expandedStyle);
	}
	
    public ChatterboxWidget getNewRepeatWidget (FormIndex index, FormDef f, Chatterbox cbox) {
    	GroupDef repeat = (GroupDef)f.explodeIndex(index).lastElement();

    	//damn linked lists...
    	FormIndex end = index;
    	while (!end.isTerminal()) {
    		end = end.getNextLevel();
    	}
    	int multiplicity = end.getInstanceIndex();
    	
    	QuestionDef q = new QuestionDef(-1, "New Repeat?", Constants.CONTROL_SELECT_ONE);
    	
    	String label = repeat.getLongText();
    	
    	q.setLongText("Add " + (multiplicity > 0 ? "another " : "") + (label == null || label.length() == 0 ? "repetition" : label) + "?"); //this caption will not localize, even though repeat label is taken from the current locale
    	q.addSelectItem("Yes", "y");
    	q.addSelectItem("No", "n");
    	
    	FormElementBinding binding = new FormElementBinding(null, q, new TreeElement(null, 0));
		
		return new ChatterboxWidget(cbox, binding, ChatterboxWidget.VIEW_EXPANDED, new CollapsedWidget(), new SelectOneEntryWidget(ChoiceGroup.EXCLUSIVE));
    }
    
    public ChatterboxWidget getNewLabelWidget(FormIndex index, FormDef f, Chatterbox cbox){
    	IFormElement element = f.getChild(index);
    	if(!(element instanceof GroupDef)) {
    		throw new IllegalArgumentException("Attempted to create a Label for something that was not a Group");
    	}
    	GroupDef group = (GroupDef)element;
    	
    	String labelText = group.getLongText();
    	if(labelText != null && labelText != "") {
    		FormElementBinding binding = new FormElementBinding(null, index, f);
    		int mult = -1;
    		if(group.getRepeat()) {
    			mult = binding.instanceNode.getMult() + 1;
    		}
    		ChatterboxWidget newLabel = new ChatterboxWidget(cbox, binding,ChatterboxWidget.VIEW_LABEL, new LabelWidget(mult), null);
    		return newLabel;
    	} else {
    		return null;
    	}
    }
    
    public void setReadOnly(boolean readOnly) {
    	this.readOnly = readOnly;
    }
}
