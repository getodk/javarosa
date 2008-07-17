package org.javarosa.xform.util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.Reader;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import minixpath.XPathExpression;

import org.javarosa.core.model.Condition;
import org.javarosa.core.model.Constants;
import org.javarosa.core.model.DataBinding;
import org.javarosa.core.model.DataModelTree;
import org.javarosa.core.model.FormData;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.GroupData;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.OptionDef;
import org.javarosa.core.model.QuestionData;
import org.javarosa.core.model.QuestionDataElement;
import org.javarosa.core.model.QuestionDataGroup;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.SkipRule;
import org.javarosa.core.model.TreeElement;
import org.javarosa.model.xform.XPathReference;
import org.kxml2.io.KXmlParser;
import org.kxml2.io.KXmlSerializer;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;
import org.xmlpull.v1.XmlPullParser;



/**
 * Provides conversion from xform to epihandy object model and vice vasa.
 * 
 * @author Daniel Kayiwa
 *
 */
public class JavaRosaXformsParser{
	
	/**
	 * Default Constructor
	 *
	 */
	public JavaRosaXformsParser(){
		     
	}
	
	private static String getXmlType(int type){
		switch(type){
			case Constants.QTN_TYPE_BOOLEAN:
				return "xsd:boolean";
			case Constants.QTN_TYPE_DATE:
				return "xsd:date";
			case Constants.QTN_TYPE_DATE_TIME:
				return "xsd:dateTime";
			case Constants.QTN_TYPE_TIME:
				return "xsd:time";
			case Constants.QTN_TYPE_DECIMAL:
				return "xsd:decimal";
			case Constants.QTN_TYPE_NUMERIC:
				return "xsd:int";
			case Constants.QTN_TYPE_TEXT:
			case Constants.QTN_TYPE_LIST_EXCLUSIVE:
			case Constants.QTN_TYPE_LIST_MULTIPLE:
				return "xsd:string";
		}
		
		return "";
	}
	
	public static String fromFormDef2Xform(FormDef formDef){
		Document doc = new Document();
		doc.setEncoding("UTF-8");
		Element htmlNode = doc.createElement("http://www.w3.org/1999/xhtml", null);
		htmlNode.setName("html");
		htmlNode.setPrefix(null, "http://www.w3.org/1999/xhtml");
		htmlNode.setPrefix("xf", "http://www.w3.org/2002/xforms");
		htmlNode.setPrefix("xsd", "http://www.w3.org/2001/XMLSchema");
		htmlNode.setPrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		doc.addChild(org.kxml2.kdom.Element.ELEMENT, htmlNode);
		
		Element headNode = doc.createElement("http://www.w3.org/1999/xhtml", null);
		headNode.setName("head");
		htmlNode.addChild(org.kxml2.kdom.Element.ELEMENT, headNode);
		
		Element titleNode =  doc.createElement("http://www.w3.org/1999/xhtml", null);
		titleNode.setName("title");
		titleNode.addChild(Element.TEXT,formDef.getName());
		headNode.addChild(Element.ELEMENT,titleNode);
		
		Element modelNode =  doc.createElement("http://www.w3.org/2002/xforms", null);
		modelNode.setName("model");
		headNode.addChild(Element.ELEMENT,modelNode);
		
		Element instanceNode =  doc.createElement("http://www.w3.org/2002/xforms", null);
		instanceNode.setName("instance");
		modelNode.addChild(Element.ELEMENT,instanceNode);
		
		Element formNode =  doc.createElement("http://www.w3.org/1999/xhtml", null);
		formNode.setName(formDef.getVariableName());
		//formNode.setAttribute(null, "id", Integer.toString(formDef.getId()));
		instanceNode.addChild(Element.ELEMENT,formNode);
		
		Element bodyNode =  doc.createElement("http://www.w3.org/1999/xhtml", null);
		bodyNode.setName("body");
		htmlNode.addChild(Element.ELEMENT,bodyNode);
		
		for(int pageNo=0; pageNo<formDef.getGroups().size(); pageNo++){
			GroupDef page = (GroupDef)formDef.getGroups().elementAt(pageNo);
			Vector questions = page.getQuestions();
			for(int i=0; i<questions.size(); i++){
				QuestionDef qtn = (QuestionDef)questions.elementAt(i);
				DataBinding bind = (DataBinding) qtn.getBind();
				XPathReference reference  = (XPathReference)bind.getReference();
				Element dataNode =  doc.createElement("http://www.w3.org/1999/xhtml", null);
				dataNode.setName((String)reference.getReference());
				//dataNode.addChild(Element.TEXT,"data");
				formNode.addChild(Element.ELEMENT,dataNode);
				
				//If the bind ID is "", it's a ref, not a bind
				if (bind.getId() != "") {
					Element bindNode = doc.createElement(
							"http://www.w3.org/2002/xforms", null);
					bindNode.setName("bind");
					bindNode.setAttribute(null, "id", bind.getId());
					bindNode.setAttribute(null, "nodeset", (String)reference.getReference());
					bindNode.setAttribute(null, "type", getXmlType(bind.getDataType()));
					if (qtn.isMandatory())
						bindNode.setAttribute(null, "required", "true()");
					if (!qtn.isEnabled())
						bindNode.setAttribute(null, "readonly", "true()");
					modelNode.addChild(Element.ELEMENT, bindNode);
				}
		
				
				Element inputNode =  getXformInputElementName(doc,qtn);
				bodyNode.addChild(Element.ELEMENT,inputNode);
				
				//TODO Relevancy, update with conditions
				//if(bind.getRelevancy() != null && bind.getRelevancy() != "") {
					// TODO should this be in the xforms namespace?
				//	inputNode.setAttribute(null, "relevant", bind.getRelevancy());
				//}
				
				Element labelNode =  doc.createElement("http://www.w3.org/2002/xforms", null);
				labelNode.setName("label");
				labelNode.addChild(Element.TEXT,qtn.getLongText());
				inputNode.addChild(Element.ELEMENT,labelNode);
				
				String helpText = qtn.getHelpText();
				if(helpText != null && helpText.length() > 0){
					Element hintNode =  doc.createElement("http://www.w3.org/2002/xforms", null);
					hintNode.setName("hint");
					hintNode.addChild(Element.TEXT,helpText);
					inputNode.addChild(Element.ELEMENT,hintNode);
				}
				
				Vector options = qtn.getOptions();
				if(options != null && options.size() > 0){
					for(int j=0; j<options.size(); j++){
						OptionDef optionDef = (OptionDef)options.elementAt(j);
						
						Element itemNode =  doc.createElement("http://www.w3.org/2002/xforms", null);
						itemNode.setName("item");
						
						Element node =  doc.createElement("http://www.w3.org/2002/xforms", null);
						node.setName("label");
						node.addChild(Element.TEXT,optionDef.getText());
						itemNode.addChild(Element.ELEMENT,node);
						
						node =  doc.createElement("http://www.w3.org/2002/xforms", null);
						node.setName("value");
						node.addChild(Element.TEXT,optionDef.getVariableName());
						itemNode.addChild(Element.ELEMENT,node);
						
						inputNode.addChild(Element.ELEMENT,itemNode);
					}
				}
			}
		}
		
		return fromDoc2String(doc);
	}
	
	private static Element getXformInputElementName(Document doc, QuestionDef qtnDef){
		Element node = doc.createElement("http://www.w3.org/2002/xforms", null);
		//TODO update with reference
		DataBinding bind = null;// = qtnDef.getBinding();
		if(bind.getId() != "") {
			node.setAttribute(null, "bind", bind.getId());
		}
		else {
		    XPathReference reference = (XPathReference)bind.getReference();
			node.setAttribute(null, "ref", (String)reference.getReference());
		}

		switch(qtnDef.getType()){
			case Constants.QTN_TYPE_LIST_EXCLUSIVE:
				node.setName("select1");
				node.setAttribute(null, "selection", "closed");
				break;
			case Constants.QTN_TYPE_LIST_MULTIPLE:
				node.setName("select");
				node.setAttribute(null, "selection", "closed");
				break;
			default:
				node.setName("input");
		}
		
		return node;
	}
	
	/*/**
	 * Updates the XForm model with the answers.
	 * 
	 * @param xml -  the XForm xml having the model.
	 * @param formData - the form data having the answers.
	 * @return - a string representing the xml of the updated model only.
	 */
	/*public static String updateXformModel(String xml, FormData formData){
		Document doc = getDocument(new java.io.StringReader(xml));
		
		for(int pageNo=0; pageNo<formData.getGroups().size(); pageNo++){
			GroupData page = (GroupData)formData.getGroups().elementAt(pageNo);
			Vector questions = page.getQuestions();
			for(int i=0; i<questions.size(); i++)
				updateModel(doc,(QuestionData)questions.elementAt(i));
		}
				
		Element instanceDataNode = getInstanceDataNode(doc);
		return fromDoc2String(getDocumentFromNode(instanceDataNode));
	}*/
	
	/**
	 * Updates the XForm model with the answers.
	 * 
	 * @param doc -  the XForm document having the model.
	 * @param formData - the form data having the answers.
	 * @return - a string representing the xml of the updated model only.
	 */
	public static String updateXformModel(Document doc, FormData formData){
		for(int pageNo=0; pageNo<formData.getGroups().size(); pageNo++){
			GroupData page = (GroupData)formData.getGroups().elementAt(pageNo);
			Vector questions = page.getQuestions();
			for(int i=0; i<questions.size(); i++)
				updateModel(doc,(QuestionData)questions.elementAt(i));
		}
				
		Element instanceDataNode = getInstanceDataNode(doc);
		return fromDoc2String(getDocumentFromNode(instanceDataNode));
	}
	
	public static Document getDocumentFromNode(Element element){
		Document doc = new Document();
		doc.setEncoding("UTF-8");
		/*Element rootNode = doc.createElement(null, null);
		rootNode.setName(element.getName());
		doc.addChild(org.kxml2.kdom.Element.ELEMENT, rootNode);
		
		updateDocumentFromNode(doc,rootNode,element);*/
		
		doc.addChild(org.kxml2.kdom.Element.ELEMENT, element);
		
		element.setPrefix("xf", "http://www.w3.org/2002/xforms");
		element.setPrefix("xsd", "http://www.w3.org/2001/XMLSchema");
		element.setPrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");
				
		return doc;
	}
	
	private static void updateDocumentFromNode(Document doc,Element copyTo,Element copyFrom){
		int numOfEntries = copyFrom.getChildCount();
		for (int i = 0; i < numOfEntries; i++) {
			Element oldNode = copyFrom.getElement(i);
			Element newNode = doc.createElement(null, null);
			if(!copyFrom.isText(i)){
				newNode.setName(oldNode.getName());
				copyTo.addChild(Element.ELEMENT, newNode);
				updateDocumentFromNode(doc,newNode,oldNode);
			}
			else
				copyTo.addChild(Element.TEXT, copyFrom.getText(i));
		}
	}
	
	private static void updateModel(Document doc, QuestionData qtnData){
		
		//we dot spit out answers for invisible and disabled questions since
		//they are considered non-relevant.
		if(qtnData.getDef().isVisible() && qtnData.getDef().isEnabled()){
			
			String xpath = qtnData.getDef().getVariableName();
			String value = qtnData.getValueAnswer();
			
			if (value != null) {
				Element elem = getInstanceNode(doc);
				xpath = new String(xpath.toCharArray(), 1, xpath.length()-1);
				XPathExpression xpls = new XPathExpression(elem, xpath);
				Vector result = xpls.getResult();
				
				for (Enumeration e = result.elements(); e.hasMoreElements();) {
					Object obj = e.nextElement();
					if (obj instanceof Element)
						((Element) obj).addChild(Node.TEXT, value);
				}
			}
		}
	}
	
	private static Element getInstanceNode(Document doc){
		return getInstanceNode(doc.getRootElement());
		
	}
	
	private static Element getInstanceDataNode(Document doc){
		return getInstanceDataNode(getInstanceNode(doc));
		
	}
	
	private static Element getInstanceNode(Element element){
		int numOfEntries = element.getChildCount();
		for (int i = 0; i < numOfEntries; i++) {
			if (!element.isText(i) && element.getType(i) == Element.ELEMENT) {
				Element child = element.getElement(i);
				String tagname = child.getName();
				if (tagname.equals("instance"))
					return child;
				else{
					child = getInstanceNode(child);
					if(child != null)
						return child;
				}
			}
		}
		return null;
	}
	
	private static Element getInstanceDataNode(Element element){
		int numOfEntries = element.getChildCount();
		for (int i = 0; i < numOfEntries; i++) {
			if (!element.isText(i) && element.getType(i) == Element.ELEMENT) 
				return element.getElement(i);
		}
		
		return null;
	}
	
	public static String fromFormData2XformModel(FormData formData){
		FormDef formDef = formData.getDef();
		Document doc = new Document();
		doc.setEncoding("UTF-8");
		Element rootNode = doc.createElement(null, null);
		rootNode.setName(formDef.getVariableName());
		//rootNode.setAttribute(null, "id", Integer.toString(formDef.getId()));
		doc.addChild(org.kxml2.kdom.Element.ELEMENT, rootNode);

		for(int pageNo=0; pageNo<formData.getGroups().size(); pageNo++){
			GroupData page = (GroupData)formData.getGroups().elementAt(pageNo);
			Vector questions = page.getQuestions();
			for(int i=0; i<questions.size(); i++){
				QuestionData qtnData = (QuestionData)questions.elementAt(i);
				//we dot spit out answers for invisible and disabled questions since
				//they are considered non-relevant.
				if(qtnData.getDef().isVisible() && qtnData.getDef().isEnabled()){
					Element node =  doc.createElement(null, null);
					//TODO update with reference
					//node.setName(((XPathBinding)qtnData.getDef().getBind()).getNodeset());
					if(qtnData.getValueAnswer() != null)
						node.addChild(Element.TEXT,qtnData.getValueAnswer());
					rootNode.addChild(Element.ELEMENT,node);
				}
			}		
		}
		
		return fromDoc2String(doc);
	}
	
	public static ByteArrayOutputStream fromDoc2Stream(Document doc) {
		KXmlSerializer serializer = new KXmlSerializer();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		
		try{
			serializer.setOutput(dos,null);
			doc.write(serializer);
			serializer.flush();
			return bos;
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public static String fromDoc2String(Document doc){
		ByteArrayOutputStream bos = fromDoc2Stream(doc);		
		
		byte[] byteArr = bos.toByteArray();
		char[]charArray = new char[byteArr.length];
		for(int i=0; i<byteArr.length; i++)
			charArray[i] = (char)byteArr[i];
		
		return String.valueOf(charArray);
	}
	
	public static String fromFormData2Xform(FormData formData){
		FormDef formDef = formData.getDef();
		Document doc = new Document();
		//TODO: Shouldn't namespaces be handled more cleanly?
		doc.setEncoding("UTF-8");
		Element htmlNode = doc.createElement("http://www.w3.org/1999/xhtml", null);
		htmlNode.setName("html");
		htmlNode.setPrefix(null, "http://www.w3.org/1999/xhtml");
		htmlNode.setPrefix("xf", "http://www.w3.org/2002/xforms");
		htmlNode.setPrefix("xsd", "http://www.w3.org/2001/XMLSchema");
		htmlNode.setPrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		doc.addChild(org.kxml2.kdom.Element.ELEMENT, htmlNode);
		
		Element headNode = doc.createElement("http://www.w3.org/1999/xhtml", null);
		headNode.setName("head");
		htmlNode.addChild(org.kxml2.kdom.Element.ELEMENT, headNode);
		
		Element titleNode =  doc.createElement("http://www.w3.org/1999/xhtml", null);
		titleNode.setName("title");
		titleNode.addChild(Element.TEXT,formDef.getName());
		headNode.addChild(Element.ELEMENT,titleNode);
		
		Element modelNode =  doc.createElement("http://www.w3.org/2002/xforms", null);
		modelNode.setName("model");
		headNode.addChild(Element.ELEMENT,modelNode);
		
		Element instanceNode =  doc.createElement("http://www.w3.org/2002/xforms", null);
		instanceNode.setName("instance");
		modelNode.addChild(Element.ELEMENT,instanceNode);
		
		Element formNode =  doc.createElement("http://www.w3.org/1999/xhtml", null);
		formNode.setName(formDef.getVariableName());
		//formNode.setAttribute(null, "id", Integer.toString(formDef.getId()));
		instanceNode.addChild(Element.ELEMENT,formNode);
		
		Element bodyNode =  doc.createElement("http://www.w3.org/1999/xhtml", null);
		bodyNode.setName("body");
		htmlNode.addChild(Element.ELEMENT,bodyNode);
		
		for(int groupNo=0; groupNo<formData.getGroups().size(); groupNo++){
			GroupData group = (GroupData)formData.getGroups().elementAt(groupNo);
			Vector questions = group.getQuestions();
			for(int i=0; i<questions.size(); i++){
				QuestionData qtn = (QuestionData)questions.elementAt(i);
				Element dataNode =  doc.createElement("http://www.w3.org/1999/xhtml", null);
				dataNode.setName(qtn.getDef().getVariableName());
				if(qtn.getValueAnswer() != null)
					dataNode.addChild(Element.TEXT,qtn.getValueAnswer());
				formNode.addChild(Element.ELEMENT,dataNode);
				
				//TODO: Update with reference
				DataBinding bind = null;//qtn.getDef().getBinding();
				XPathReference reference = (XPathReference)bind.getReference();
				// An empty string bind ID is actually a ref
				if(!bind.getId().equals("")) {
				Element bindNode = doc.createElement(
							"http://www.w3.org/2002/xforms", null);
					bindNode.setName("bind");
					bindNode.setAttribute(null, "id", bind.getId());
					bindNode.setAttribute(null, "nodeset", (String)reference.getReference());
					bindNode.setAttribute(null, "type", getXmlType(bind.getDataType()));
					if (qtn.getDef().isMandatory())
						bindNode.setAttribute(null, "required", "true()");
					if (!qtn.getDef().isEnabled())
						bindNode.setAttribute(null, "readonly", "true()");
					//TODO: Add support for optional jr namespace options here
					modelNode.addChild(Element.ELEMENT, bindNode);
				}
		
				
				Element inputNode =  getXformInputElementName(doc,qtn.getDef());
				bodyNode.addChild(Element.ELEMENT,inputNode);
				
				Element labelNode =  doc.createElement("http://www.w3.org/2002/xforms", null);
				labelNode.setName("label");
				labelNode.addChild(Element.TEXT,qtn.getDef().getLongText());
				inputNode.addChild(Element.ELEMENT,labelNode);
				
				String helpText = qtn.getDef().getHelpText();
				if(helpText != null && helpText.length() > 0){
					Element hintNode =  doc.createElement("http://www.w3.org/2002/xforms", null);
					hintNode.setName("hint");
					hintNode.addChild(Element.TEXT,helpText);
					inputNode.addChild(Element.ELEMENT,hintNode);
				}
				
				Vector options = qtn.getDef().getOptions();
				if(options != null && options.size() > 0){
					for(int j=0; j<options.size(); j++){
						OptionDef optionDef = (OptionDef)options.elementAt(j);
						
						Element itemNode =  doc.createElement("http://www.w3.org/2002/xforms", null);
						itemNode.setName("item");
						
						Element node =  doc.createElement("http://www.w3.org/2002/xforms", null);
						node.setName("label");
						node.addChild(Element.TEXT,optionDef.getText());
						itemNode.addChild(Element.ELEMENT,node);
						
						node =  doc.createElement("http://www.w3.org/2002/xforms", null);
						node.setName("value");
						node.addChild(Element.TEXT,optionDef.getVariableName());
						itemNode.addChild(Element.ELEMENT,node);
						
						inputNode.addChild(Element.ELEMENT,itemNode);
					}
				}
			}
		}
		
		return fromDoc2String(doc);
	}
	
	public static FormDef fromXform2FormDef(Reader reader){
		Document doc = getDocument(reader);
		try {
			return getFormDef(doc);
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Document getDocument(Reader reader){
		Document doc = new Document();
		
		try{
			KXmlParser parser = new KXmlParser();
			parser.setInput(reader);
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
			
			doc.parse(parser);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		return doc;
	}
	
	public static FormDef getFormDef(Document doc){
		Element rootNode = doc.getRootElement();
		FormDef formDef = new FormDef();
		Hashtable id2VarNameMap = new Hashtable();
		Hashtable relevants = new Hashtable();
		parseElement(formDef,rootNode,id2VarNameMap,null,relevants);
		if(formDef.getName() == null || formDef.getName().length() == 0)
			formDef.setName(formDef.getVariableName());
		setFormDefDataModel(getInstanceNode(doc),formDef);
		setDefaultValues(getInstanceDataNode(doc),formDef,id2VarNameMap);
		addSkipRules(formDef,id2VarNameMap,relevants);
		return formDef;
	}
	
	private static String getNodeTextValue(Element dataNode,String name){
		Element node = getNode(dataNode,name);
		return getTextValue(node);
	}
	
	private static void setDefaultValues(Element dataNode,FormDef formDef,Hashtable id2VarNameMap){
		String id, val;
		Enumeration keys = id2VarNameMap.keys();
		while(keys.hasMoreElements()){
			id = (String)keys.nextElement();
			val = getNodeTextValue(dataNode,id);
			if(val == null || val.trim().length() == 0) //we are not allowing empty strings for now.
				continue;
			QuestionDef def = formDef.getQuestion((String)id2VarNameMap.get(id));
			if(def != null)
				def.setDefaultValue(val);
		}
	}
	
	private static String getTextValue(Element node){
		int numOfEntries = node.getChildCount();
		for (int i = 0; i < numOfEntries; i++) {
			if (node.isText(i))
				return node.getText(i);
			
			if(node.getType(i) == Element.ELEMENT){
				String val = getTextValue(node.getElement(i));
				if(val != null)
					return val;
			}
		}
		
		return null;
	}
	
	/**
	 * Gets a child element of a parent node with a given name.
	 * 
	 * @param parent - the parent element
	 * @param name - the name of the child.
	 * @return - the child element.
	 */
	private static Element getNode(Element parent, String name){
		for(int i=0; i<parent.getChildCount(); i++){
			if(parent.getType(i) != Element.ELEMENT)
				continue;
			
			Element child = (Element)parent.getChild(i);
			if(child.getName().equals(name))
				return child;
			
			child = getNode(child,name);
			if(child != null)
				return child;
		}
		
		return null;
	}
	
	public static FormData fromXform2FormData(Reader reader){			
		Document doc = getDocument(reader);		
		FormData formData = new FormData(getFormDef(doc));
		
		Element dataNode = doc.getRootElement().getElement(0).getElement(1).getElement(0).getElement(0);
		for(int i=0; i<dataNode.getChildCount(); i++){
			Element node = dataNode.getElement(i);
			if(node != null && node.getChildCount() > 0)
				formData.setValue(node.getName(), node.getText(0));
		}
			
		return formData;
	}
	
	private static QuestionDef parseElement(FormDef formDef, Element element, Hashtable map,QuestionDef questionDef,Hashtable relevants){
		String label = ""; //$NON-NLS-1$
		String value = ""; //$NON-NLS-1$
		
		int numOfEntries = element.getChildCount();
		for (int i = 0; i < numOfEntries; i++) {
			if (!element.isText(i) && element.getType(i) == Element.ELEMENT) {
				Element child = element.getElement(i);
				String tagname = child.getName();
				if(tagname.equals("submit"))
					continue;
				else if (tagname.equals("head"))
					parseElement(formDef,child,map,questionDef,relevants);
				else if (tagname.equals("body"))
					parseElement(formDef, child,map,questionDef,relevants);
				else if (tagname.equals("title"))
					formDef.setName(child.getText(0).trim());
				else if (tagname.equals("model"))
					parseElement(formDef, child,map,questionDef,relevants);
				else if (tagname.equals("instance")) {
					Element dataNode = null;
					for(int k=0; k<child.getChildCount(); k++){
						if(!child.isText(k))
							dataNode = (Element)child.getChild(k);
					}
					formDef.setVariableName(dataNode.getName());
					if(dataNode.getAttributeValue(null, "description-template") != null)
						formDef.setDescriptionTemplate(dataNode.getAttributeValue(null, "description-template"));
					if(dataNode.getAttributeValue(null, "id") != null)
						formDef.setId(Byte.parseByte(dataNode.getAttributeValue(null, "id")));
					if(dataNode.getAttributeValue(null, "name") != null)
						formDef.setName(dataNode.getAttributeValue(null, "name"));
				} else if (tagname.equals("bind") || tagname.equals("ref")) {
					DataBinding binding  = new DataBinding();
					//binding
					binding.setId(child.getAttributeValue("", "id"));
					String nodeset = child.getAttributeValue(null, "nodeset");
					XPathReference reference = new XPathReference(nodeset);
					binding.setReference(reference);
					setDataType(binding, child.getAttributeValue(null, "type")); 
					//setQuestionType(qtn,type);
					if(child.getAttributeValue(null, "required") != null && child.getAttributeValue(null, "required").equals("true()")) {
						binding.setRequired(true);
					}
					
					//formDef.addQuestion(qtn);
					map.put(binding.getId(), nodeset);
					if(child.getAttributeValue(null, "relevant") != null) {
						//#if debug.output==verbose
						System.out.println("Relevant!" + child.getAttributeValue(null,"relevant"));
						//#endif
						String relevancy = child.getAttributeValue(null, "relevant");
						relevants.put(relevancy, binding);
						//We're 
					}
					binding.setPreload(child.getAttributeValue("http://openrosa.org/javarosa", "preload"));
					binding.setPreloadParams(child.getAttributeValue("http://openrosa.org/javarosa", "preloadParams"));
					//It's very possible that we should actually be adding these to the Map, and that the 
					//Questions should be what are added to the form
					//TODO: update with reference
					//formDef.addBinding(binding);
					
				} else if (tagname.equals("input") || tagname.equals("select1") || tagname.equals("select")) {
					String ref = child.getAttributeValue(null, "ref");
					String bind = child.getAttributeValue(null, "bind");
					QuestionDef qtn = new QuestionDef();
					attachBind(formDef, qtn, ref, bind);
					String varName = (String)map.get(((ref != null) ? ref : bind));
					//If the bind existed before, it's an xforms bind. Otherwise it's just a reference.
					if(varName != null || qtn.getBind() != null){
						if(tagname.equals("select1") || tagname.equals("select")){
							//TODO: update with reference
							//qtn.getBinding().setDataType((tagname.equals("select1")) ? Constants.QTN_TYPE_LIST_EXCLUSIVE : Constants.QTN_TYPE_LIST_MULTIPLE);
							qtn.setOptions(new Vector());
						}
						if(child.getAttributeValue(null, "relevant") != null) {
							String relevancy = child.getAttributeValue(null, "relevant");
							relevants.put(relevancy, qtn.getBind());
							//TODO: update with reference
							//((XPathBinding)qtn.getBind()).setRelevancy(relevancy);
						}
						questionDef = qtn;
						// Call parseElement to attach any of the internal elements. Then add it to the form 
						formDef.addQuestion((parseElement(formDef, child, map,questionDef,relevants)));
						
					}
				} else if (tagname.equals("label")) {
					label = child.getText(0).trim();
				}
				else if (tagname.equals("hint")){
					if(questionDef != null)
						questionDef.setHelpText(child.getText(0).trim());
				}
				else if (tagname.equals("item"))
					parseElement(formDef, child,map,questionDef,relevants);
			    else if (tagname.equals("value"))
					value = child.getText(0).trim();
			    else
			    	parseElement(formDef, child,map,questionDef,relevants);
				// TODO - how are other elements like html:p or br handled?
			}
		}

		if (!label.equals("") && !value.equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
			if (questionDef != null && questionDef.getOptions() != null){
				questionDef.addOption(new OptionDef(Byte.parseByte(String.valueOf(questionDef.getOptions().size())),label, value));
			}
		} else if (!label.equals("") && questionDef != null) {
			questionDef.setLongText(label);
			questionDef.setShortText(label);
		}
		
		return questionDef;
	}
	
	private static void setFormDefDataModel(Element instanceNode, FormDef form) {
		TreeElement root = parseInstanceElement(instanceNode, "/").getRoot();
		DataModelTree instanceModel = new DataModelTree(root);
		//form.setDataModel(instaneModel);
	}
	
	private static TreeElement parseInstanceElement(Element node, String currentPath) {
		int childNum = node.getChildCount();
		
		TreeElement element;
		if(childNum == 0) {
			XPathReference reference = new XPathReference(currentPath + node.getName());
			//TODO: Figure out how to make the nodeset here.
			element = new QuestionDataElement(node.getName(), reference);
		} else {
			element = new QuestionDataGroup(node.getName());
			for(int i = 0 ; i  < childNum ; ++i ) {
				String newPath = currentPath + node.getName() + "/";
				((QuestionDataGroup)element).addChild(parseInstanceElement((Element)node.getChild(i), newPath));
			}
		}
		return element;
	}
	
	private static void setQuestionType(QuestionDef def, String type){
		//TODO: update with reference
		//setDataType(def.getBinding(), type);
	}
	
	private static void setDataType(DataBinding bind, String type) {
		if(type.equals("xsd:string") || type.indexOf("string") != -1 )
			bind.setDataType(Constants.QTN_TYPE_TEXT);
		else if((type.equals("xsd:integer") || type.equals("xsd:int")) || (type.indexOf("integer") != -1 || type.indexOf("int") != -1))
			bind.setDataType(Constants.QTN_TYPE_NUMERIC);
		else if(type.equals("xsd:decimal") || type.indexOf("decimal") != -1 )
			bind.setDataType(Constants.QTN_TYPE_DECIMAL);
		else if(type.equals("xsd:dateTime") || type.indexOf("dateTime") != -1 )
			bind.setDataType(Constants.QTN_TYPE_DATE_TIME);
		else if(type.equals("xsd:time") || type.indexOf("time") != -1 )
			bind.setDataType(Constants.QTN_TYPE_TIME);
		else if(type.equals("xsd:date") || type.indexOf("date") != -1 )
			bind.setDataType(Constants.QTN_TYPE_DATE);
		else if(type.equals("xsd:boolean") || type.indexOf("boolean") != -1 )
			bind.setDataType(Constants.QTN_TYPE_BOOLEAN);
	}
	
	private static void addSkipRules(FormDef formDef, Hashtable map, Hashtable relevants){
		Vector rules = new Vector();
		//Rules is vector of strings that contain the "relevant=" thing
		Enumeration en = relevants.keys();
		byte ruleId = 0;
		
		while(en.hasMoreElements()) {
			String relevant = (String)en.nextElement();
			DataBinding bind = (DataBinding)relevants.get(relevant);
			int split = relevant.indexOf("=");
			if(split != -1) {
				//TODO: Consolidate these by the relevant element: Many questionss depend on the same condition
				String relevantQuestionPath = relevant.substring(0, split);
				String relevantAnswer = relevant.substring(split+1, relevant.length()-1);
				QuestionDef thetarget = (QuestionDef) formDef
						.getQuestion(relevantQuestionPath);

				if (thetarget != null) {
					Vector conditions = new Vector();
					Vector actionTargets = new Vector();

					Condition condition = new Condition(ruleId, thetarget
							.getId(), Constants.OPERATOR_EQUAL, relevantAnswer);
					conditions.addElement(condition);
					XPathReference reference = (XPathReference)bind.getReference();
					actionTargets.addElement(reference.getReference());

					SkipRule rule = new SkipRule(ruleId, conditions,
							Constants.ACTION_ENABLE, actionTargets, relevant);

					rules.addElement(rule);
					ruleId++;
					//#if debug.output==verbose
					System.out.println("New rule added: id: " + ruleId
							+ " Conditions: " + conditions.toString()
							+ " actionTargets : " + actionTargets.toString());
					//#endif
				}
			}
			else {
				//Is there a form of relevancy that isn't an equality?
			}
		}
		
		
		formDef.setRules(rules);
	}
	
	private static void attachBind(FormDef form, QuestionDef qtn, String ref,
			String bind) {
		if (bind != null) {
			//TODO: update with reference
			DataBinding b = null;//form.getBinding(bind);
			if (b != null) {
				//Potential default here?
				qtn.setId(b.getId());
				//TODO: update with reference
				//qtn.setBinding(b);
				XPathReference reference = (XPathReference)b.getReference();
				qtn.setVariableName((String)reference.getReference());
				
				if (b.getPreload() != null) {
					qtn.setDefaultValue(getPreloadValue(b.getPreload(), (b.getPreloadParams() == null ? "" : b.getPreloadParams())));
				}

			}
			else {
				//LOG
				//#if debug.output==verbose
				System.out.println("MATCHING BIND not found");
				//#endif
			}
		}
		else if (ref != null) {
			DataBinding b = new DataBinding();
			//Empty ID bindings are references, not bind elements
			b.setId("");
			XPathReference reference = new XPathReference(ref);
			b.setReference(reference);
			//This is an assumption, is it neccesarily valid?
			b.setDataType(Constants.QTN_TYPE_TEXT);
			qtn.setId(b.getId());
			qtn.setVariableName(ref);
			//TODO: update with reference
			//qtn.setBinding(b);
		}
	}
	
	/**
	 * Loads JavaRosa pre-loadable values into defaults for questions. A lot of this needs serious refactoring so
	 * we're going to rely on it not functioning properly until it's been integrated better.
	 * 
	 * @param loadMode
	 * @param loadParams
	 * @return
	 */
	public static Object getPreloadValue (String loadMode, String loadParams) {
		Object preloadVal = null;
		
		if (loadMode.equals("date")) {
			Date d = null;
			
			if (loadParams.equals("today")) {
				d = new Date();
			} else if (loadParams.substring(0, 11).equals("prevperiod-")) {
				//String[] params = J2MEUtil.split(loadParams.substring(11), "-");
				
				//try {
				//	String type = params[0];
				//	String start = params[1];
				//	
				//	boolean beginning;
				//	if (params[2].equals("head")) beginning = true;
				//	else if (params[2].equals("tail")) beginning = false;
				//	else throw new RuntimeException();					
				//	
				//	boolean includeToday;
				//	if (params.length >= 4) {
				//		if (params[3].equals("x")) includeToday = true;
				//		else if (params[3].equals("")) includeToday = false;
				//		else throw new RuntimeException();											
				//	} else {
				//		includeToday = false;
				//	}
				//	
				//	int nAgo;
				//	if (params.length >= 5) {
				//		nAgo = Integer.parseInt(params[4]);
				//	} else {
				//		nAgo = 1;
				//	}

				//	d = getPastPeriodDate(new Date(), type, start, beginning, includeToday, nAgo);
				//} catch (Exception e) {
				//	throw new IllegalArgumentException("invalid preload params for preload mode 'date'");
				//}	
			}
			
			//preloadVal = d;
		} else if (loadMode.equals("property")) {
			String propname = loadParams;
			//String propval = PropertyManager.instance().getSingularProperty(propname);
			//if (propval != null && propval.length() > 0)
			//	preloadVal = propval;
		} else if (loadMode.equals("timestamp")) {
			//if (loadParams.equals("start"))
			//	preloadVal = J2MEUtil.formatDateToTimeStamp(new Date());
		} else {
			throw new IllegalArgumentException();
		}

		return preloadVal;
	}

}
