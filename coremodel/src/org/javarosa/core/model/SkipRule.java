package org.javarosa.core.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.javarosa.util.db.Persistent;
import org.javarosa.util.db.PersistentHelper;


/**
 * A definition for skipping or branching rules. 
 * These could for example be enabling or disabling, hiding or showing, maing mandatory or optional 
 * of questions basing on values of others.
 * 
 * @author Daniel Kayiwa
 *
 */
public class SkipRule implements Persistent{
	
	/** The numeric identifier of a rule. This is assigned in code and hence
	 * is not known by the user.
	 */
	private byte id = EpihandyConstants.NULL_ID;
	
	/** A list of conditions to be tested for a rule. 
	 * E.g. If sex is Male. If age is greatern than 4. etc
	 */
	private Vector conditions;
	
	/** The action taken when conditions are true.
	 * Example of actions are Disable, Hide, Show, etc
	 */
	private byte action = EpihandyConstants.ACTION_NONE;
	
	/** A list of question identifiers acted upon when conditions for the rule are true. */
	private Vector actionTargets;
	
	/** The skip rule name. */
	private String name;
		
	/** Constructs a rule object ready to be initialized. */
	public SkipRule(){
		super();
	}
	
	/** Construct a Rule object from parameters. 
	 * 
	 * @param ruleId 
	 * @param conditions 
	 * @param action
	 * @param actionTargets
	 */
	public SkipRule(byte ruleId, Vector conditions, byte action, Vector actionTargets, String name) {
		this();
		setId(ruleId);
		setConditions(conditions);
		setAction(action);
		setActionTargets(actionTargets);
		setName(name);
	}
	
	public byte getAction() {
		return action;
	}

	public void setAction(byte action) {
		this.action = action;
	}

	public Vector getActionTargets() {
		return actionTargets;
	}

	public void setActionTargets(Vector actionTargets) {
		this.actionTargets = actionTargets;
	}

	public Vector getConditions() {
		return conditions;
	}

	public void setConditions(Vector conditions) {
		this.conditions = conditions;
	}

	public byte getId() {
		return id;
	}

	public void setId(byte id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/** 
	 * Checks conditions of a rule and executes the corresponding actions
	 * 
	 * @param data
	 */
	public void fire(FormData data){
		for(byte i=0; i<this.getConditions().size(); i++){
			Condition condition = (Condition)this.getConditions().elementAt(i);
			ExecuteAction(data,condition.isTrue(data));
		}
	}
	
	/** Executes the action of a rule for its conditition's true or false value. */
	public void ExecuteAction(FormData data,boolean conditionTrue){
		Vector qtns = this.getActionTargets();
		for(byte i=0; i<qtns.size(); i++)
			ExecuteAction(data.getQuestion(Byte.parseByte(qtns.elementAt(i).toString())).getDef(),conditionTrue);
	}
	
	/** Executes the rule action on the supplied question. */
	public void ExecuteAction(QuestionDef qtn,boolean conditionTrue){
		switch(getAction()){
			case EpihandyConstants.ACTION_ENABLE:
				qtn.setEnabled(conditionTrue);
				break;
			case EpihandyConstants.ACTION_DISABLE:
				qtn.setEnabled(!conditionTrue);
				break;
			case EpihandyConstants.ACTION_SHOW:
				qtn.setVisible(conditionTrue);
				break;
			case EpihandyConstants.ACTION_HIDE:
				qtn.setVisible(!conditionTrue);
				break;
			case EpihandyConstants.ACTION_MAKE_MANDATORY:
				qtn.setMandatory(conditionTrue);
				break;
			case EpihandyConstants.ACTION_MAKE_OPTIONAL:
				qtn.setMandatory(!conditionTrue);
				break;
		}
	}
	
	/**
	 * @see org.javarosa.util.db.Persistent#read(java.io.DataInputStream)
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		if(!PersistentHelper.isEOF(dis)){
			setId(dis.readByte());
			setAction(dis.readByte());
			setConditions(PersistentHelper.read(dis,new Condition().getClass()));
			setActionTargets(PersistentHelper.readBytes(dis));
			setName(dis.readUTF());
		}
	}

	/**
	 * @see org.javarosa.util.db.Persistent#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream dos) throws IOException {
		dos.writeByte(getId());
		dos.writeByte(getAction());
		PersistentHelper.write(getConditions(), dos);
		PersistentHelper.writeBytes(getActionTargets(), dos);
		dos.writeUTF(getName());
	}
	
	public String toString() {
		return getName();
	}
}
 