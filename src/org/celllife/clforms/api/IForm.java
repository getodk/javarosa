package org.celllife.clforms.api;

import java.util.Vector;

import org.celllife.clforms.storage.Model;

public interface IForm {

	public abstract Prompt getPrompt(int PromptID);
	
	public abstract Vector getPrompts();
	
	public abstract void updateModel(Prompt prompt);
	
	public abstract String getName();
	
	public abstract void setXmlModel(Model model);
	
	public abstract Model getXmlModel();
	
	public abstract void updatePromptsValues();
	
	public abstract void updatePromptsDefaults();
	
	public abstract void calculateRelavant(Prompt p);
	
	public abstract void calculateRelevantAll();
	
	public abstract void setShortForms();
	
	public abstract void loadPromptsDefaultValues();
	
	public abstract void setPrompts(Vector prompts);
	
	public abstract void setName(String name);
	
	public abstract void setRecordId(int recordId);
	
	public abstract int getRecordId();
	
	public abstract void populateModel();
}
