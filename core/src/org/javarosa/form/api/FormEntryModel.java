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

import java.util.Enumeration;
import java.util.Vector;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.formmanager.utility.FormEntryModelListener;
import org.javarosa.formmanager.view.FormElementBinding;

public class FormEntryModel {
	private FormDef form;

	private FormIndex activeQuestionIndex;
	private FormIndex startIndex;
	private int instanceID;
	private boolean unsavedChanges;
	private boolean formCompleted;

	private Vector observers;

	public int totalQuestions; // total number of questions in the form; used
								// for progress bar

	private boolean readOnly;

	public FormEntryModel(FormDef form) {
		this(form, -1);
	}

	public FormEntryModel(FormDef form, int instanceID) {
		this(form, instanceID, null, false);
	}

	public FormEntryModel(FormDef form, int instanceID, FormIndex firstIndex, boolean readOnly) {
		this.form = form;
		this.instanceID = instanceID;
		this.observers = new Vector();

		this.activeQuestionIndex = FormIndex.createBeginningOfFormIndex();
		this.unsavedChanges = true; // we want them to be able to save the form
									// initially, even with nothing in it
		this.formCompleted = false;
		this.startIndex = firstIndex;
		
		this.setReadOnly(readOnly);
	}

	public FormIndex getQuestionIndex() {
		return activeQuestionIndex;
	}

	public void setQuestionIndex(FormIndex index) {
		if (!activeQuestionIndex.equals(index)) {

			// See if a hint exists that says we should have a model for this
			// already
			createModelIfNecessary(index);

			activeQuestionIndex = index;

			for (Enumeration e = observers.elements(); e.hasMoreElements();) {
				((FormEntryModelListener) e.nextElement())
						.questionIndexChanged(activeQuestionIndex);
			}
		}
	}

	// depending on boolean, counts the number of unanswered required questions,
	// or
	// counts the number of unanswered questions.
	public int countUnansweredQuestions(boolean countRequiredOnly) {
		int counter = 0;

		for (FormIndex a = form.incrementIndex(FormIndex
				.createBeginningOfFormIndex()); a.compareTo(FormIndex
				.createEndOfFormIndex()) < 0; a = form.incrementIndex(a)) {
			FormElementBinding bind = new FormElementBinding(null, a, form);

			if (countRequiredOnly && bind.instanceNode.required
					&& bind.getValue() == null) {
				counter++;
			} else if (bind.getValue() == null) {
				counter++;
			}
		}
		return counter;
	}

	public FormDef getForm() {
		return form;
	}

	public int getInstanceID() {
		return instanceID;
	}

	public boolean isSaved() {
		return !unsavedChanges;
	}

	public void modelChanged() {
		if (!unsavedChanges) {
			unsavedChanges = true;

			for (Enumeration e = observers.elements(); e.hasMoreElements();) {
				((FormEntryModelListener) e.nextElement()).saveStateChanged(instanceID, unsavedChanges);
			}
		}
	}

	public void modelSaved(int instanceID) {
		this.instanceID = instanceID;
		unsavedChanges = false;

		for (Enumeration e = observers.elements(); e.hasMoreElements();) {
			((FormEntryModelListener) e.nextElement()).saveStateChanged(instanceID, unsavedChanges);
		}
	}

	public boolean isFormComplete() {
		return formCompleted;
	}

	public void setFormComplete() {
		if (!formCompleted) {
			formCompleted = true;

			if (!activeQuestionIndex.isEndOfFormIndex()) {
				setQuestionIndex(FormIndex.createEndOfFormIndex());
			}

			for (Enumeration e = observers.elements(); e.hasMoreElements();) {
				((FormEntryModelListener) e.nextElement()).formComplete();
			}
		}
	}

	public void notifyStartOfForm() {
		for (Enumeration e = observers.elements(); e.hasMoreElements();) {
			((FormEntryModelListener) e.nextElement()).startOfForm();
		}
	}

	public int getNumQuestions() {
		return form.getDeepChildCount();
	}

	protected boolean isAskNewRepeat(FormIndex questionIndex) {
		Vector defs = form.explodeIndex(questionIndex);
		IFormElement last = (defs.size() == 0 ? null : (IFormElement) defs
				.lastElement());
		if (last instanceof GroupDef
				&& ((GroupDef) last).getRepeat()
				&& form.getDataModel().resolveReference(
						form.getChildInstanceRef(questionIndex)) == null) {
			return true;
		}
		return false;
	}

	public boolean isReadonly(FormIndex questionIndex) {
		TreeReference ref = form.getChildInstanceRef(questionIndex);
		boolean isAskNewRepeat = isAskNewRepeat(questionIndex);

		if (isAskNewRepeat) {
			return false;
		} else {
			TreeElement node = form.getDataModel().resolveReference(ref);
			return !node.isEnabled();
		}
	}

	public boolean isRelevant(FormIndex questionIndex) {
		TreeReference ref = form.getChildInstanceRef(questionIndex);
		boolean isAskNewRepeat = isAskNewRepeat(questionIndex);

		boolean relevant;
		if (isAskNewRepeat) {
			relevant = form.canCreateRepeat(ref);
		} else {
			TreeElement node = form.getDataModel().resolveReference(ref);
			relevant = node.isRelevant(); // check instance flag first
		}

		if (relevant) { // if instance flag/condition says relevant, we still
						// have to check the <group>/<repeat> hierarchy
			Vector defs = form.explodeIndex(questionIndex);

			FormIndex ancestorIndex = null;
			FormIndex cur = null;
			FormIndex qcur = questionIndex;
			for (int i = 0; i < defs.size() - 1; i++) {
				FormIndex next = new FormIndex(qcur.getLocalIndex(), qcur
						.getInstanceIndex());
				if (ancestorIndex == null) {
					ancestorIndex = next;
					cur = next;
				} else {
					cur.setNextLevel(next);
					cur = next;
				}
				qcur = qcur.getNextLevel();

				TreeElement ancestorNode = form.getDataModel()
						.resolveReference(
								form.getChildInstanceRef(ancestorIndex));
				if (!ancestorNode.isRelevant()) {
					relevant = false;
					break;
				}
			}
		}

		return relevant;
	}

	public void registerObservable(FormEntryModelListener feml) {
		if (!observers.contains(feml)) {
			observers.addElement(feml);
		}
	}

	public void unregisterObservable(FormEntryModelListener feml) {
		observers.removeElement(feml);
	}

	public void unregisterAll() {
		observers.removeAllElements();
	}

	/**
	 * @return Whether or not the form model should be written to.
	 */
	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * @param readOnly
	 *            Whether or not the form model should be changed by the form
	 *            entry interaction.
	 */
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	/**
	 * @return the startIndex
	 */
	public FormIndex getStartIndex() {
		return startIndex;
	}

	/**
	 * For the current index: Checks whether the index represents a node which
	 * should exist given a non-interactive repeat, along with a count for that
	 * repeat which is beneath the dynamic level specified.
	 * 
	 * If this index does represent such a node, the new model for the repeat is
	 * created behind the scenes and the index for the initial question is
	 * returned.
	 * 
	 * Note: This method will not prevent the addition of new repeat elements in
	 * the interface, it will merely use the xforms repeat hint to create new
	 * nodes that are assumed to exist
	 * 
	 * @param The
	 *            index to be evaluated as to whether the underlying model is
	 *            hinted to exist
	 */
	private void createModelIfNecessary(FormIndex index) {
		if (index.isInForm()) {
			IFormElement e = getForm().getChild(index);
			if (e instanceof GroupDef) {
				GroupDef g = (GroupDef) e;
				if (g.getRepeat() && g.getCountReference() != null) {
					IAnswerData count = getForm().getDataModel().getDataValue(
							g.getCountReference());
					if (count != null) {
						int fullcount = ((Integer) count.getValue()).intValue();
						TreeReference ref = getForm()
								.getChildInstanceRef(index);
						TreeElement element = getForm().getDataModel()
								.resolveReference(ref);
						if (element == null) {
							if (index.getInstanceIndex() < fullcount) {
								getForm().createNewRepeat(index);
							}
						}
					}
				}
			}
		}
	}
}