package org.javarosa.core.model;

import java.util.Vector;


/**
 * Represents skip logic. This is the logic of hiding/showing, 
 * disabling/enabling, making mandatory/or optional of certain questions
 * depending on answer from other questions.
 * 
 * @author Daniel Kayiwa
 *
 */
public class EpiHandySkipRule extends SkipRule {
	
	/** Constructs a rule object ready to be initialized. */
	public EpiHandySkipRule(){
		super();
	}
	
	/** Construct a Rule object from parameters. 
	 * 
	 * @param ruleId 
	 * @param conditions 
	 * @param action
	 * @param actionTargets
	 */
	public EpiHandySkipRule(byte ruleId, Vector conditions, byte action, Vector actionTargets, String name) {
		super(ruleId, conditions, action, actionTargets, name);
	}
	
	/**
	 * Fires a rule when its condition is true.
	 */
	public void fire(FormData data){
		for(byte i=0; i<this.getConditions().size(); i++){
			Condition condition = (Condition)this.getConditions().elementAt(i);
			if(condition.isTrue(data))
				ExecuteAction(data,true);
		}
	}
	
	/** Executes the action of a rule for its conditition's true value. */
	public void ExecuteAction(FormData data,boolean conditionTrue){
		Vector qtns = this.getActionTargets();
		for(byte i=0; i<qtns.size(); i++)
			ExecuteAction(data.getQuestion(qtns.elementAt(i).toString()).getDef(),conditionTrue);
	}
	
	/** Executes the rule action on the supplied question. */
	public void ExecuteAction(QuestionDef qtn,boolean conditionTrue){
		switch(getAction()){
			case Constants.ACTION_ENABLE:
				qtn.setEnabled(true);
				break;
			case Constants.ACTION_DISABLE:
				qtn.setEnabled(false);
				break;
			case Constants.ACTION_SHOW:
				qtn.setVisible(true);
				break;
			case Constants.ACTION_HIDE:
				qtn.setVisible(false);
				break;
			case Constants.ACTION_MAKE_MANDATORY:
				qtn.setMandatory(true);
				break;
			case Constants.ACTION_MAKE_OPTIONAL:
				qtn.setMandatory(false);
				break;
		}
	}
}
