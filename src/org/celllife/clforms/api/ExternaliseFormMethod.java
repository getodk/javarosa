package org.celllife.clforms.api;

import java.util.Enumeration;
import org.celllife.clforms.storage.Model;
import org.celllife.clforms.storage.ModelMetaData;

public class ExternaliseFormMethod {

	private Form form;
	private String namespace = "xf";
	private String formatting ="\n";

	public ExternaliseFormMethod() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public String externaliseForm(Form formObj) {
		this.form = formObj;
		String result = "";
		
		result += writeDocumentHeader()+formatting;
		
		result += writeModel();
		
		result += writeFormControls()+formatting;
		
		result += writeDocumentFooter()+formatting;
		
		return result;
	}

	private String writeFormControls() {
		String result = "";
		
		for(int i=0; i<form.getPrompts().size();i++){
			result += "<"+namespace+":";
			// form control type
			result += writeFormControlType(form.getPrompt(i));
			// attributes
			result += writeBindingFCAttributes(form.getPrompt(i));
			result += writeCommonFCAttributes(form.getPrompt(i));
			result +=">"+formatting;
			// common children label, hint
			result+= writeCommonElements(form.getPrompt(i));
			
			//if selects write Items
			if(form.getPrompt(i).getFormControlType()==Constants.SELECT || form.getPrompt(i).getFormControlType()==Constants.SELECT1 ){
				result += writeItem(form.getPrompt(i));
			}
			
			result += "</"+namespace+":"+writeFormControlType(form.getPrompt(i))+">"+formatting;
		}
		
		return result;
	}

	private String writeItem(Prompt prompt) {
		String result = "";
		Enumeration keys = prompt.getSelectMap().keys();				
		Enumeration enumeration = prompt.getSelectMap().elements();
		while (keys.hasMoreElements()) {
			result += "<"+namespace+":item>"+formatting;
			String label = (String) keys.nextElement();
			result += element("label",label)+formatting;
			String value = (String) enumeration.nextElement();
			result += element("value",value)+formatting;
			result += "</"+namespace+":item>"+formatting;
		}
		return result;
	}

	private String writeCommonFCAttributes(Prompt prompt) {
		String result = "";
		//required
		if(prompt.isRequired())
			result += "required=\"true\" ";		
		//relevant
		if(prompt.getRelevantString()!=null)
			result += "relevant=\""+prompt.getRelevantString()+ "\" ";
		return result;
	}

	private String writeBindingFCAttributes(Prompt prompt) {
		String result = "";
		//bind
		if(prompt.getId() != null)
			result += "bind=\""+prompt.getId()+"\" ";
		return result;
	}

	private String writeFormControlType(Prompt prompt) {
		String result = "";
		
		switch (prompt.getFormControlType()) {
		case Constants.INPUT:
			result += "input ";
			break;
		case Constants.SELECT1:
			result += "select1 ";
			break;
		case Constants.SELECT:
			result += "select ";
			break;
		}
		
		return result;
	}

	private String writeCommonElements(Prompt prompt) {
		String result = "";
		//label 
		if(prompt.getLongText()!=null)
			result+= element("label",prompt.getLongText())+formatting;
		//hint
		if(prompt.getHint()!=null)
			result+= element("hint",prompt.getHint())+formatting;
		return result;
	}

	private String element(String label, String text) {
		return "<"+namespace +":"+label+">"+text+"</"+namespace+":"+label+">";
	}

	private String writeModel() {
		String result = "";
		
		result += writeModelOpen()+formatting;
		
		result += writeInstance()+formatting;
		
		result += writeBinds();
		
		result += "</"+namespace+":model>"+formatting;
		
		return result;
	}

	private String writeBinds() {
		String result = "";
		
		
		Enumeration enumeration = form.getBindings().elements();
		while (enumeration.hasMoreElements()) {
			Binding bind = (Binding) enumeration.nextElement();
			
			result += "<"+namespace+":bind ";
			
			result += writeBindAttributes(bind);
			
			result += "/>"+formatting;
		}

		return result;
	}

	private String writeBindAttributes(Binding bind) {
		String result = "";
		//id
		if(bind.getId()!=null)
			result += "id=\""+bind.getId()+"\" ";
		//nodeset
		if(bind.getNodeset()!=null)
			result += "nodeset=\""+bind.getNodeset()+ "\" ";
		//type
		if(bind.getType()!=null)
			result += "type=\"xsd:"+bind.getType()+ "\" ";
		return result;
	}

	private String writeInstance() {
		String result = "";
		
		result += "<"+namespace+":instance>"+formatting;
		
		result += writeModelData()+formatting;
		
		result += "</"+namespace+":instance>";
		
		return result;
	}

	private String writeModelData() {
		String result = "";
		
		result += form.getXmlModel().toString();
		
		return result;
	}

	private String writeModelOpen() {
		String result = "";
		Model model = form.getXmlModel();
		result += "<"+namespace+":model ";
		
		result += writeModelAttributes(model);
				
		result += ">";
		
		return result;
	}

	private String writeModelAttributes(Model model) {
		String result = "";
		ModelMetaData mdata = new ModelMetaData(model);
		System.out.println("EXTERNALIZE model: "+mdata.toString());
		
		result += "id=\""+mdata.getName()+ "\"";
				
		return result;
	}

	private String writeDocumentHeader() {
		return "" +
				"<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:xf=\"http://www.w3.org/2002/xforms\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">";
	}

	private String writeDocumentFooter() {
		return "</html>";		
	}
}
