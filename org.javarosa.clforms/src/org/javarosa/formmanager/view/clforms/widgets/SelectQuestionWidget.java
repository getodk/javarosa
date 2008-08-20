package org.javarosa.formmanager.view.clforms.widgets;

import javax.microedition.lcdui.Command;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.formmanager.view.clforms.SingleQuestionScreen;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ChoiceGroup;

import de.enough.polish.ui.ChoiceGroup;
import de.enough.polish.ui.Container;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;

public class SelectQuestionWidget extends SingleQuestionScreen
{
	private QuestionDef prompt;
	protected Item entryWidget;
	protected QuestionDef question;
	private int choiceType;
	
	public SelectQuestionWidget(String title)
	{
		super(title);
		reset();//clear widget on initialising
	}
	
	public Item initWidget(QuestionDef question)
	{
		//set question text
		prompt = question;
		getEntryWidget(prompt);		
		entryWidget = getEntryWidget(question);
		
		//set widget data value through IAnswerData
		//setWidgetValue(IAnswerData.getValue);
		
		//set hint
		setHint("Allowed to make multiple choices");
		return entryWidget;
	}
	
	public Item getEntryWidget (QuestionDef question) 
	{
		this.question = question;

		ChoiceGroup cg = new ChoiceGroup("",choiceType ); //{
			/*public int getRelativeScrollYOffset() {
				if (!this.enableScrolling && this.parent instanceof Container) {
					//This line here (The + this.parent.relativeY part) is the fix.
					return ((Container)this.parent).getScrollYOffset() + this.relativeY + this.parent.relativeY;
				}
				int offset = this.targetYOffset;
				//#ifdef polish.css.scroll-mode
					if (!this.scrollSmooth) {
						offset = this.yOffset;
					}
				//#endif
				return offset;
			}*/
		//};
		
		for (int i = 0; i < question.getSelectItems().size(); i++)
			cg.append(question.getLongText(), null);
		
		return cg;
	}

/*	protected ChoiceGroup choiceGroup () {
		return (ChoiceGroup)entryWidget;    
	}*/

/*	protected void updateWidget (QuestionDef question) {
		for (int i = 0; i < choiceGroup().size(); i++) {
			choiceGroup().getItem(i).setText((String)question.getSelectItems().keyAt(i));
		}
	}*/	
	
	
	public void reset () {
		prompt = null;
		entryWidget = null;
	}
	
	//Utility methods
	protected void setHint(String helpText)
	{
		//should be abstract and handled by question-type child classes.
	}
}