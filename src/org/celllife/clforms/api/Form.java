package org.celllife.clforms.api;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import minixpath.XPathExpression;

import org.celllife.clforms.storage.Externalizable;
import org.celllife.clforms.storage.IDRecordable;
import org.celllife.clforms.storage.Model;
import org.celllife.clforms.util.J2MEUtil;
import org.celllife.clforms.xml.XMLUtil;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;


//import java.lang.Locale;

public class Form implements IForm, IDRecordable, Externalizable {

	private String name;
	private Vector prompts;
	private Model xmlModel;
	private Hashtable bindings;
	private String url;
	private int recordId;

	public Form() {
		super();
		this.prompts = new Vector();
		this.bindings = new Hashtable(20);	
	}
	
	public Form(String data){
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data.getBytes()));
		InputStreamReader isr = new InputStreamReader(dis);
		try {
			XMLUtil.parseForm(isr,this);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Form(String name, Vector prompts, Model model) {
		super();
		this.name = name;
		this.prompts = prompts;
		this.xmlModel = model;
		try {
			
			this.bindings = new Hashtable(20);		
		}
		catch (Exception e){
			System.out.println(e.getMessage());
		}
	}

	public Hashtable getBindings() {
		return bindings;
	}

	public void setBindings(Hashtable bindings) {
		this.bindings = bindings;
	}

	public int getRecordId() {
		return recordId;
	}

	public void addPrompt(Prompt newPrompt) {
		// TODO: why cast to Object?
		prompts.addElement((Object) newPrompt);
	}

	public Prompt getPrompt(int promptId) {
		return (Prompt) prompts.elementAt(promptId);
	}

	public Vector getPrompts() {
		return prompts;
	}

	public void setPrompts(Vector prompts) {
		this.prompts = prompts;
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

	public int getPromptCount() {
		return prompts.size();
	}

	/**
	 * Returns a Vector containing all the prompts of the specified type.
	 * 
	 * @param type
	 * @return
	 */
	public Vector getPromptsByType(int type) {
		Vector typePrompts = new Vector();
		Enumeration e = prompts.elements();
		while (e.hasMoreElements()) {
			Prompt prompt = (Prompt) e.nextElement();
			if (prompt.getReturnType() == type)
				typePrompts.addElement(prompt);
		}

		return typePrompts;
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

	/**
	 * Evaluates an Xpath expression on the xmlModel and returns a Vector result
	 * set.
	 * 
	 * @param string
	 * @return Vector result set
	 */
	public Vector evaluateXpath(String xpath) {
		XPathExpression xpls = new XPathExpression(xmlModel.getXmlModel(), xpath);
		return xpls.getResult();
	}

	/**
	 * 
	 */
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
	
	/**
	 * 
	 */
/*	public void writeModel(OutputStream stream) {
		try {
			KXmlSerializer serializer = new KXmlSerializer();
			serializer.setOutput(stream, null);
			xmlModel.write(serializer);
			serializer.flush();
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage()); //$NON-NLS-1$
		}

	}*/

	public void setRecordId(int recordId) {

		this.recordId = recordId;
	}

	/**
	 * @param in
	 * @throws IOException
	 */
	public void readExternal(DataInputStream in) throws IOException {
		// TODO handle xformparseexception
		try {
			XMLUtil.parseForm(new InputStreamReader(in), this);
		} catch (Exception e) {
			if (e.getClass().equals(IOException.class))
				throw new IOException();				
			e.printStackTrace();
		}
	}

	public void writeExternal(DataOutputStream out) throws IOException {

		ExternaliseFormMethod efm = new ExternaliseFormMethod();
		out.write(efm.externaliseForm(this).getBytes());
		System.out.println("externalise form: "+ efm.externaliseForm(this));
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
	
		/**
	 * Populates the XFPrompts with the data contained in the  xmlModel
	 */
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
	
	public void updatePromptsDefaultValues() {
		Enumeration e = prompts.elements();
		while (e.hasMoreElements()) {
			Prompt elem = (Prompt) e.nextElement();
				updatePrompt(elem, true);
		}
	}
	
	public void loadPromptsDefaultValues()
	{
		for (int i = 0; i < this.prompts.size(); i++)
		{
			this.getPrompt(i).setValue(this.getPrompt(i).getDefaultValue());
			//LOG
			System.out.println("Updating prompt: "+this.getPrompt(i).getBindID()+ " with default val:"+ this.getPrompt(i).getDefaultValue());
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
						if (defaultVal){
							Object obVal = prompt.getValueByTypeFromString(value);
							prompt.setDefaultValue(obVal);
						}
						else {
							Object obVal = prompt.getValueByTypeFromString(value);
							prompt.setValue(obVal);
							//LOG
							System.out.println("Updating prompt: "+prompt.getBindID()+ " with val:"+ value);
						}
					}			
			}
		}
	}
	

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public void addBinding(Binding b) {
		bindings.put(b.getId(), b);
	}
	
	public void clearPromptValues()
    {
        for (int i = 0; i < this.prompts.size(); i++)
        {
            this.getPrompt(i).setValue(null);
        }
    }
	
/*	public void updatePromptsRequired() {
		Enumeration e = prompts.elements();
		while (e.hasMoreElements()) {
			Prompt elem = (Prompt) e.nextElement();
			elem.setRequired(elem.getBind().isRequired());
			if (elem.getValue() == null){
				updatePrompt(elem, false);
			}
		}
		
	}*/

}
