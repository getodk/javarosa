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

package org.javarosa.form.api;

import java.util.ArrayList;
import java.util.List;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.ItemsetBinding;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.condition.Constraint;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.pivot.ConstraintHint;
import org.javarosa.core.model.condition.pivot.UnpivotableExpressionException;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.services.Logger;
import org.javarosa.core.util.NoLocalizedTextException;
import org.javarosa.core.util.UnregisteredLocaleException;
import org.javarosa.formmanager.view.IQuestionWidget;



/**
 * This class gives you all the information you need to display a question when
 * your current FormIndex references a QuestionEvent.
 *
 * @author Yaw Anokwa
 */
public class FormEntryPrompt extends FormEntryCaption {

    TreeElement mTreeElement;
    boolean dynamicChoicesPopulated = false;

    /**
     * This empty constructor exists for convenience of any supertypes of this prompt
     */
    protected FormEntryPrompt() {
    }

    /**
	 * Creates a FormEntryPrompt for the element at the given index in the form.
	 *
	 * @param form
	 * @param index
	 */
    public FormEntryPrompt(FormDef form, FormIndex index) {
        super(form, index);
        if (!(element instanceof QuestionDef)) {
        	throw new IllegalArgumentException("FormEntryPrompt can only be created for QuestionDef elements");
        }
        this.mTreeElement = form.getMainInstance().resolveReference(index.getReference());
    }

    public int getControlType() {
        return getQuestion().getControlType();
    }

    public int getDataType() {
        return mTreeElement.getDataType();
    }

    // attributes available in the bind, instance and body
    public String getPromptAttributes() {
        // TODO: implement me.
        return null;
    }

    //note: code overlap with FormDef.copyItemsetAnswer
    public IAnswerData getAnswerValue() {
    	QuestionDef q = getQuestion();

		ItemsetBinding itemset = q.getDynamicChoices();
    	if (itemset != null) {
    		if (itemset.valueRef != null) {
            List<SelectChoice> choices = getSelectChoices();
            List<String> preselectedValues = new ArrayList<String>();

	    		//determine which selections are already present in the answer
	    		if (itemset.copyMode) {
	    			TreeReference destRef = itemset.getDestRef().contextualize(mTreeElement.getRef());
               List<TreeReference> subNodes = form.getEvaluationContext().expandReference(destRef);
	    			for (int i = 0; i < subNodes.size(); i++) {
	    				TreeElement node = form.getMainInstance().resolveReference(subNodes.get(i));
    					String value = itemset.getRelativeValue().evalReadable(form.getMainInstance(), new EvaluationContext(form.getEvaluationContext(), node.getRef()));
    					preselectedValues.add(value);
	    			}
	    		} else {
               List<Selection> sels;
	    			IAnswerData data = mTreeElement.getValue();
	    			if (data instanceof SelectMultiData) {
	    				sels = (List<Selection>)data.getValue();
	    			} else if (data instanceof SelectOneData) {
	    				sels = new ArrayList<Selection>(1);
	    				sels.add((Selection)data.getValue());
	    			} else {
	    				sels = new ArrayList<Selection>(0);
	    			}
	    			for (int i = 0; i < sels.size(); i++) {
	    				preselectedValues.add(sels.get(i).xmlValue);
	    			}
	    		}

    			//populate 'selection' with the corresponding choices (matching 'value') from the dynamic choiceset
            List<Selection> selection = new ArrayList<Selection>();
	    		for (int i = 0; i < preselectedValues.size(); i++) {
	    			String value = preselectedValues.get(i);
	    			SelectChoice choice = null;
	    			for (int j = 0; j < choices.size(); j++) {
	    				SelectChoice ch = choices.get(j);
	    				if (value.equals(ch.getValue())) {
	    					choice = ch;
	    					break;
	    				}
	    			}
	    			//if it's a dynamic question, then there's a good choice what they selected last time
	    			//will no longer be an option this go around
	    			if(choice != null)
	    			{
	    				selection.add(choice.selection());
	    			}
	    		}

	    		//convert to IAnswerData
	    		if (selection.size() == 0) {
	    			return null;
	    		} else if (q.getControlType() == Constants.CONTROL_SELECT_MULTI) {
	    			return new SelectMultiData(selection);
	    		} else if (q.getControlType() == Constants.CONTROL_SELECT_ONE) {
	    			return new SelectOneData(selection.get(0)); //do something if more than one selected?
	    		} else {
	    			throw new RuntimeException("can't happen");
	    		}
    		} else {
    			return null; //cannot map up selections without <value>
    		}
    	} else { //static choices
            return mTreeElement.getValue();
    	}
    }


    public String getAnswerText() {
    	IAnswerData data = this.getAnswerValue();

        if (data == null)
            return null;
        else {
        	String text;

        	//csims@dimagi.com - Aug 11, 2010 - Added special logic to
        	//capture and display the appropriate value for selections
        	//and multi-selects.
        	if(data instanceof SelectOneData) {
        		text = this.getSelectItemText((Selection)data.getValue());
        	} else if(data  instanceof SelectMultiData) {
        		StringBuilder b = new StringBuilder();
            List<Selection> values = (List<Selection>)data.getValue();
        		for(Selection value : values) {
        			b.append(this.getSelectItemText(value)).append(" ");
        		}
        		text = b.toString();
        	} else {
        		text = data.getDisplayText();
        	}

        	if(getControlType() == Constants.CONTROL_SECRET) {
				StringBuilder b = new StringBuilder();
				for(int i =0 ; i < text.length() ; ++i ) {
					b.append("*");
				}
				text = b.toString();
        	}
        	return text;
        }
    }

    public String getConstraintText() {
        return getConstraintText(null);
    }

    public String getConstraintText(IAnswerData attemptedValue) {
        return getConstraintText(null, attemptedValue);
    }

    public String getConstraintText(String textForm, IAnswerData attemptedValue) {
    	if (mTreeElement.getConstraint() == null) {
            return null;
        } else {
        	EvaluationContext ec = new EvaluationContext(form.getEvaluationContext(), mTreeElement.getRef());
        	if(textForm != null) {
        		ec.setOutputTextForm(textForm);
        	}
        	if(attemptedValue != null) {
        		ec.isConstraint = true;
        		ec.candidateValue = attemptedValue;
        	}
            return mTreeElement.getConstraint().getConstraintMessage(ec, form.getMainInstance(), textForm);
        }
    }

    public List<TreeElement> getBindAttributes() {
    	return mTreeElement.getBindAttributes();
    }
    public List<SelectChoice> getSelectChoices() {
    	QuestionDef q = getQuestion();

		ItemsetBinding itemset = q.getDynamicChoices();
    	if (itemset != null) {
    		if (!dynamicChoicesPopulated) {
    			form.populateDynamicChoices(itemset, mTreeElement.getRef());
    			dynamicChoicesPopulated = true;
    		}
    		return itemset.getChoices();
    	} else { //static choices
    		return q.getChoices();
    	}
    }

    public void expireDynamicChoices () {
    	dynamicChoicesPopulated = false;
		ItemsetBinding itemset = getQuestion().getDynamicChoices();
		if (itemset != null) {
			itemset.clearChoices();
		}
    }



    public boolean isRequired() {
        return mTreeElement.isRequired();
    }

    public boolean isReadOnly() {
        return !mTreeElement.isEnabled();
    }

    public QuestionDef getQuestion() {
    	return (QuestionDef)element;
    }

    //==== observer pattern ====//

	public void register (IQuestionWidget viewWidget) {
		super.register(viewWidget);
		mTreeElement.registerStateObserver(this);
	}

	public void unregister () {
		mTreeElement.unregisterStateObserver(this);
		super.unregister();
	}

	public void formElementStateChanged(TreeElement instanceNode, int changeFlags) {
		if (this.mTreeElement != instanceNode)
			throw new IllegalStateException("Widget received event from foreign question");
		if (viewWidget != null)
			viewWidget.refreshWidget(changeFlags);
	}

       /**
	 * ONLY RELEVANT to Question elements!
	 * Will throw runTimeException if this is called for anything that isn't a Question.
	 * Returns null if no help text is available
	 * @return
	 */
	public String getHelpText() {
		if(!(element instanceof QuestionDef)){
			throw new RuntimeException("Can't get HelpText for Elements that are not Questions!");
		}

		String textID = ((QuestionDef)element).getHelpTextID();
		String helpText = ((QuestionDef)element).getHelpText();
		String helpInnerText = ((QuestionDef)element).getHelpInnerText();

		try{
			if (textID != null) {
			    helpText=substituteStringArgs(localizer().getLocalizedText(textID));
			} else {
			    helpText=substituteStringArgs(((QuestionDef)element).getHelpInnerText());
			}
		}catch(NoLocalizedTextException nlt){
			//use fallback helptext
		}catch(UnregisteredLocaleException ule){
			System.err.println("Warning: No Locale set yet (while attempting to getHelpText())");
		}catch(Exception e){
			Logger.exception("FormEntryPrompt.getHelpText", e);
			e.printStackTrace();
		}

		return helpText;

	}



	/**
	 * Attempts to return the specified Item (from a select or 1select) text.
	 * Will check for text in the following order:<br/>
	 * Localized Text (long form) -> Localized Text (no special form) <br />
	 * If no textID is available, method will return this item's labelInnerText.
	 * @param sel the selection (item), if <code>null</code> will throw a IllegalArgumentException
	 * @return Question Text.  <code>null</code> if no text for this element exists (after all fallbacks).
	 * @throws RunTimeException if this method is called on an element that is NOT a QuestionDef
	 * @throws IllegalArgumentException if Selection is <code>null</code>
	 */
	public String getSelectItemText(Selection sel){
		//throw tantrum if this method is called when it shouldn't be or sel==null
		if(!(getFormElement() instanceof QuestionDef)) throw new RuntimeException("Can't retrieve question text for non-QuestionDef form elements!");
		if(sel == null) throw new IllegalArgumentException("Cannot use null as an argument!");

		//Just in case the selection hasn't had a chance to be initialized yet.
		if(sel.index == -1) { sel.attachChoice(this.getQuestion()); }

		//check for the null id case and return labelInnerText if it is so.
		String tid = sel.choice.getTextID();
		if(tid == null || tid.length() == 0) return substituteStringArgs(sel.choice.getLabelInnerText());

		//otherwise check for 'long' form of the textID, then for the default form and return
		String returnText;
		returnText = getIText(tid, "long");
		if(returnText == null) returnText = getIText(tid,null);

		return substituteStringArgs(returnText);
	}

	/**
	 * @see getSelectItemText(Selection sel)
	 */
	public String getSelectChoiceText(SelectChoice selection){
		return getSelectItemText(selection.selection());
	}

	/**
	 * This method is generally used to retrieve special forms for a
	 * (select or 1select) item, e.g. "audio", "video", etc.
	 *
	 * @param sel - The Item whose text you're trying to retrieve.
	 * @param form - Special text form of Item you're trying to retrieve.
	 * @return Special Form Text. <code>null</code> if no text for this element exists (with the specified special form).
	 * @throws RunTimeException if this method is called on an element that is NOT a QuestionDef
	 * @throws IllegalArgumentException if <code>sel == null</code>
	 */
	public String getSpecialFormSelectItemText(Selection sel,String form){
		if(sel == null) throw new IllegalArgumentException("Cannot use null as an argument for Selection!");

		//Just in case the selection hasn't had a chance to be initialized yet.
		if(sel.index == -1) { sel.attachChoice(this.getQuestion()); }

		String textID = sel.choice.getTextID();
		if(textID == null || textID.length() == 0) return null;

		String returnText = getIText(textID, form);

		return substituteStringArgs(returnText);

	}

	public String getSpecialFormSelectChoiceText(SelectChoice sel,String form){
		return getSpecialFormSelectItemText(sel.selection(),form);
	}

	public void requestConstraintHint(ConstraintHint hint) throws UnpivotableExpressionException {
		//NOTE: Technically there's some rep exposure, here. People could use this mechanism to expose the instance.
		//We could hide it by dispatching hints through a final abstract class instead.
		Constraint c =  mTreeElement.getConstraint();
		if(c != null) {
			hint.init(new EvaluationContext(form.getEvaluationContext(), mTreeElement.getRef()), c.constraint, this.form.getMainInstance());
		} else {
			//can't pivot what ain't there.
			throw new UnpivotableExpressionException();
		}
	}

}

