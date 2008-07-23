package org.javarosa.xform.util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;

import org.kxml2.io.KXmlSerializer;
import org.kxml2.kdom.Document;

/* this is just a big dump of serialization-related code */

/* basically, anything that didn't belong in XFormParser */

public class XFormSerializer {
	
	public static ByteArrayOutputStream getStream(Document doc) {
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
//	
	///* SERIALIZATION CODE */
	//
	//public static String fromFormDef2Xform(FormDef formDef){
//		Document doc = new Document();
//		doc.setEncoding("UTF-8");
//		Element htmlNode = doc.createElement("http://www.w3.org/1999/xhtml", null);
//		htmlNode.setName("html");
//		htmlNode.setPrefix(null, "http://www.w3.org/1999/xhtml");
//		htmlNode.setPrefix("xf", "http://www.w3.org/2002/xforms");
//		htmlNode.setPrefix("xsd", "http://www.w3.org/2001/XMLSchema");
//		htmlNode.setPrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");
//		doc.addChild(org.kxml2.kdom.Element.ELEMENT, htmlNode);
	//	
//		Element headNode = doc.createElement("http://www.w3.org/1999/xhtml", null);
//		headNode.setName("head");
//		htmlNode.addChild(org.kxml2.kdom.Element.ELEMENT, headNode);
	//	
//		Element titleNode =  doc.createElement("http://www.w3.org/1999/xhtml", null);
//		titleNode.setName("title");
//		titleNode.addChild(Element.TEXT,formDef.getName());
//		headNode.addChild(Element.ELEMENT,titleNode);
	//	
//		Element modelNode =  doc.createElement("http://www.w3.org/2002/xforms", null);
//		modelNode.setName("model");
//		headNode.addChild(Element.ELEMENT,modelNode);
	//	
//		Element instanceNode =  doc.createElement("http://www.w3.org/2002/xforms", null);
//		instanceNode.setName("instance");
//		modelNode.addChild(Element.ELEMENT,instanceNode);
	//	
//		Element formNode =  doc.createElement("http://www.w3.org/1999/xhtml", null);
//		formNode.setName(formDef.getVariableName());
//		//formNode.setAttribute(null, "id", Integer.toString(formDef.getId()));
//		instanceNode.addChild(Element.ELEMENT,formNode);
	//	
//		Element bodyNode =  doc.createElement("http://www.w3.org/1999/xhtml", null);
//		bodyNode.setName("body");
//		htmlNode.addChild(Element.ELEMENT,bodyNode);
	//	
//		for(int pageNo=0; pageNo<formDef.getGroups().size(); pageNo++){
//			GroupDef page = (GroupDef)formDef.getGroups().elementAt(pageNo);
//			Vector questions = page.getQuestions();
//			for(int i=0; i<questions.size(); i++){
//				QuestionDef qtn = (QuestionDef)questions.elementAt(i);
//				DataBinding bind = (DataBinding) qtn.getBind();
//				XPathReference reference  = (XPathReference)bind.getReference();
//				Element dataNode =  doc.createElement("http://www.w3.org/1999/xhtml", null);
//				dataNode.setName((String)reference.getReference());
//				//dataNode.addChild(Element.TEXT,"data");
//				formNode.addChild(Element.ELEMENT,dataNode);
//				
//				//If the bind ID is "", it's a ref, not a bind
//				if (bind.getId() != "") {
//					Element bindNode = doc.createElement(
//							"http://www.w3.org/2002/xforms", null);
//					bindNode.setName("bind");
//					bindNode.setAttribute(null, "id", bind.getId());
//					bindNode.setAttribute(null, "nodeset", (String)reference.getReference());
//					bindNode.setAttribute(null, "type", getXmlType(bind.getDataType()));
//					if (qtn.isMandatory())
//						bindNode.setAttribute(null, "required", "true()");
//					if (!qtn.isEnabled())
//						bindNode.setAttribute(null, "readonly", "true()");
//					modelNode.addChild(Element.ELEMENT, bindNode);
//				}
	//	
//				
//				Element inputNode =  getXformInputElementName(doc,qtn);
//				bodyNode.addChild(Element.ELEMENT,inputNode);
//				
//				//TODO Relevancy, update with conditions
//				//if(bind.getRelevancy() != null && bind.getRelevancy() != "") {
//					// TODO should this be in the xforms namespace?
//				//	inputNode.setAttribute(null, "relevant", bind.getRelevancy());
//				//}
//				
//				Element labelNode =  doc.createElement("http://www.w3.org/2002/xforms", null);
//				labelNode.setName("label");
//				labelNode.addChild(Element.TEXT,qtn.getLongText());
//				inputNode.addChild(Element.ELEMENT,labelNode);
//				
//				String helpText = qtn.getHelpText();
//				if(helpText != null && helpText.length() > 0){
//					Element hintNode =  doc.createElement("http://www.w3.org/2002/xforms", null);
//					hintNode.setName("hint");
//					hintNode.addChild(Element.TEXT,helpText);
//					inputNode.addChild(Element.ELEMENT,hintNode);
//				}
//				
//				Vector options = qtn.getOptions();
//				if(options != null && options.size() > 0){
//					for(int j=0; j<options.size(); j++){
//						OptionDef optionDef = (OptionDef)options.elementAt(j);
//						
//						Element itemNode =  doc.createElement("http://www.w3.org/2002/xforms", null);
//						itemNode.setName("item");
//						
//						Element node =  doc.createElement("http://www.w3.org/2002/xforms", null);
//						node.setName("label");
//						node.addChild(Element.TEXT,optionDef.getText());
//						itemNode.addChild(Element.ELEMENT,node);
//						
//						node =  doc.createElement("http://www.w3.org/2002/xforms", null);
//						node.setName("value");
//						node.addChild(Element.TEXT,optionDef.getVariableName());
//						itemNode.addChild(Element.ELEMENT,node);
//						
//						inputNode.addChild(Element.ELEMENT,itemNode);
//					}
//				}
//			}
//		}
	//	
//		return fromDoc2String(doc);
	//}
	//
//		private static Element getXformInputElementName(Document doc, QuestionDef qtnDef){
//			Element node = doc.createElement("http://www.w3.org/2002/xforms", null);
//			//TODO update with reference
//			DataBinding bind = null;// = qtnDef.getBinding();
//			if(bind.getId() != "") {
//				node.setAttribute(null, "bind", bind.getId());
//			}
//			else {
//			    XPathReference reference = (XPathReference)bind.getReference();
//				node.setAttribute(null, "ref", (String)reference.getReference());
//			}
	//
//			switch(qtnDef.getType()){
//				case Constants.QTN_TYPE_LIST_EXCLUSIVE:
//					node.setName("select1");
//					node.setAttribute(null, "selection", "closed");
//					break;
//				case Constants.QTN_TYPE_LIST_MULTIPLE:
//					node.setName("select");
//					node.setAttribute(null, "selection", "closed");
//					break;
//				default:
//					node.setName("input");
//			}
//			
//			return node;
//		}
	//	
	
//		public static String fromFormData2Xform(FormData formData){
//			FormDef formDef = formData.getDef();
//			Document doc = new Document();
//			//TODO: Shouldn't namespaces be handled more cleanly?
//			doc.setEncoding("UTF-8");
//			Element htmlNode = doc.createElement("http://www.w3.org/1999/xhtml", null);
//			htmlNode.setName("html");
//			htmlNode.setPrefix(null, "http://www.w3.org/1999/xhtml");
//			htmlNode.setPrefix("xf", "http://www.w3.org/2002/xforms");
//			htmlNode.setPrefix("xsd", "http://www.w3.org/2001/XMLSchema");
//			htmlNode.setPrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");
//			doc.addChild(org.kxml2.kdom.Element.ELEMENT, htmlNode);
//			
//			Element headNode = doc.createElement("http://www.w3.org/1999/xhtml", null);
//			headNode.setName("head");
//			htmlNode.addChild(org.kxml2.kdom.Element.ELEMENT, headNode);
//			
//			Element titleNode =  doc.createElement("http://www.w3.org/1999/xhtml", null);
//			titleNode.setName("title");
//			titleNode.addChild(Element.TEXT,formDef.getName());
//			headNode.addChild(Element.ELEMENT,titleNode);
//			
//			Element modelNode =  doc.createElement("http://www.w3.org/2002/xforms", null);
//			modelNode.setName("model");
//			headNode.addChild(Element.ELEMENT,modelNode);
//			
//			Element instanceNode =  doc.createElement("http://www.w3.org/2002/xforms", null);
//			instanceNode.setName("instance");
//			modelNode.addChild(Element.ELEMENT,instanceNode);
//			
//			Element formNode =  doc.createElement("http://www.w3.org/1999/xhtml", null);
//			formNode.setName(formDef.getVariableName());
//			//formNode.setAttribute(null, "id", Integer.toString(formDef.getId()));
//			instanceNode.addChild(Element.ELEMENT,formNode);
//			
//			Element bodyNode =  doc.createElement("http://www.w3.org/1999/xhtml", null);
//			bodyNode.setName("body");
//			htmlNode.addChild(Element.ELEMENT,bodyNode);
//			
//			for(int groupNo=0; groupNo<formData.getGroups().size(); groupNo++){
//				GroupData group = (GroupData)formData.getGroups().elementAt(groupNo);
//				Vector questions = group.getQuestions();
//				for(int i=0; i<questions.size(); i++){
//					QuestionData qtn = (QuestionData)questions.elementAt(i);
//					Element dataNode =  doc.createElement("http://www.w3.org/1999/xhtml", null);
//					dataNode.setName(qtn.getDef().getVariableName());
//					if(qtn.getValueAnswer() != null)
//						dataNode.addChild(Element.TEXT,qtn.getValueAnswer());
//					formNode.addChild(Element.ELEMENT,dataNode);
//					
//					//TODO: Update with reference
//					DataBinding bind = null;//qtn.getDef().getBinding();
//					XPathReference reference = (XPathReference)bind.getReference();
//					// An empty string bind ID is actually a ref
//					if(!bind.getId().equals("")) {
//					Element bindNode = doc.createElement(
//								"http://www.w3.org/2002/xforms", null);
//						bindNode.setName("bind");
//						bindNode.setAttribute(null, "id", bind.getId());
//						bindNode.setAttribute(null, "nodeset", (String)reference.getReference());
//						bindNode.setAttribute(null, "type", getXmlType(bind.getDataType()));
//						if (qtn.getDef().isMandatory())
//							bindNode.setAttribute(null, "required", "true()");
//						if (!qtn.getDef().isEnabled())
//							bindNode.setAttribute(null, "readonly", "true()");
//						//TODO: Add support for optional jr namespace options here
//						modelNode.addChild(Element.ELEMENT, bindNode);
//					}
//			
//					
//					Element inputNode =  getXformInputElementName(doc,qtn.getDef());
//					bodyNode.addChild(Element.ELEMENT,inputNode);
//					
//					Element labelNode =  doc.createElement("http://www.w3.org/2002/xforms", null);
//					labelNode.setName("label");
//					labelNode.addChild(Element.TEXT,qtn.getDef().getLongText());
//					inputNode.addChild(Element.ELEMENT,labelNode);
//					
//					String helpText = qtn.getDef().getHelpText();
//					if(helpText != null && helpText.length() > 0){
//						Element hintNode =  doc.createElement("http://www.w3.org/2002/xforms", null);
//						hintNode.setName("hint");
//						hintNode.addChild(Element.TEXT,helpText);
//						inputNode.addChild(Element.ELEMENT,hintNode);
//					}
//					
//					Vector options = qtn.getDef().getOptions();
//					if(options != null && options.size() > 0){
//						for(int j=0; j<options.size(); j++){
//							OptionDef optionDef = (OptionDef)options.elementAt(j);
//							
//							Element itemNode =  doc.createElement("http://www.w3.org/2002/xforms", null);
//							itemNode.setName("item");
//							
//							Element node =  doc.createElement("http://www.w3.org/2002/xforms", null);
//							node.setName("label");
//							node.addChild(Element.TEXT,optionDef.getText());
//							itemNode.addChild(Element.ELEMENT,node);
//							
//							node =  doc.createElement("http://www.w3.org/2002/xforms", null);
//							node.setName("value");
//							node.addChild(Element.TEXT,optionDef.getVariableName());
//							itemNode.addChild(Element.ELEMENT,node);
//							
//							inputNode.addChild(Element.ELEMENT,itemNode);
//						}
//					}
//				}
//			}
//			
//			return fromDoc2String(doc);
//		}
	//
//		public static String fromFormData2XformModel(FormData formData){
//			FormDef formDef = formData.getDef();
//			Document doc = new Document();
//			doc.setEncoding("UTF-8");
//			Element rootNode = doc.createElement(null, null);
//			rootNode.setName(formDef.getVariableName());
//			//rootNode.setAttribute(null, "id", Integer.toString(formDef.getId()));
//			doc.addChild(org.kxml2.kdom.Element.ELEMENT, rootNode);
	//
//			for(int pageNo=0; pageNo<formData.getGroups().size(); pageNo++){
//				GroupData page = (GroupData)formData.getGroups().elementAt(pageNo);
//				Vector questions = page.getQuestions();
//				for(int i=0; i<questions.size(); i++){
//					QuestionData qtnData = (QuestionData)questions.elementAt(i);
//					//we dot spit out answers for invisible and disabled questions since
//					//they are considered non-relevant.
//					if(qtnData.getDef().isVisible() && qtnData.getDef().isEnabled()){
//						Element node =  doc.createElement(null, null);
//						//TODO update with reference
//						//node.setName(((XPathBinding)qtnData.getDef().getBind()).getNodeset());
//						if(qtnData.getValueAnswer() != null)
//							node.addChild(Element.TEXT,qtnData.getValueAnswer());
//						rootNode.addChild(Element.ELEMENT,node);
//					}
//				}		
//			}
//			
//			return fromDoc2String(doc);
//		}
	//	
//		public static Document getDocumentFromNode(Element element){
//			Document doc = new Document();
//			doc.setEncoding("UTF-8");
//			/*Element rootNode = doc.createElement(null, null);
//			rootNode.setName(element.getName());
//			doc.addChild(org.kxml2.kdom.Element.ELEMENT, rootNode);
//			
//			updateDocumentFromNode(doc,rootNode,element);*/
//			
//			doc.addChild(org.kxml2.kdom.Element.ELEMENT, element);
//			
//			element.setPrefix("xf", "http://www.w3.org/2002/xforms");
//			element.setPrefix("xsd", "http://www.w3.org/2001/XMLSchema");
//			element.setPrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");
//					
//			return doc;
//		}
	//	
	
//	private static String getXmlType(int type){
//		switch(type){
//			case Constants.QTN_TYPE_BOOLEAN:
//				return "xsd:boolean";
//			case Constants.QTN_TYPE_DATE:
//				return "xsd:date";
//			case Constants.QTN_TYPE_DATE_TIME:
//				return "xsd:dateTime";
//			case Constants.QTN_TYPE_TIME:
//				return "xsd:time";
//			case Constants.QTN_TYPE_DECIMAL:
//				return "xsd:decimal";
//			case Constants.QTN_TYPE_NUMERIC:
//				return "xsd:int";
//			case Constants.QTN_TYPE_TEXT:
//			case Constants.QTN_TYPE_LIST_EXCLUSIVE:
//			case Constants.QTN_TYPE_LIST_MULTIPLE:
//				return "xsd:string";
//		}
//		
//		return "";
//	}
//	
//	
//
//	
//	
//	/**
//	 * Updates the XForm model with the answers.
//	 * 
//	 * @param doc -  the XForm document having the model.
//	 * @param formData - the form data having the answers.
//	 * @return - a string representing the xml of the updated model only.
//	 */
//	public static String updateXformModel(Document doc, FormData formData){
//		for(int pageNo=0; pageNo<formData.getGroups().size(); pageNo++){
//			GroupData page = (GroupData)formData.getGroups().elementAt(pageNo);
//			Vector questions = page.getQuestions();
//			for(int i=0; i<questions.size(); i++)
//				updateModel(doc,(QuestionData)questions.elementAt(i));
//		}
//				
//		Element instanceDataNode = getInstanceDataNode(doc);
//		return fromDoc2String(getDocumentFromNode(instanceDataNode));
//	}
//	
//
//	
//	private static void updateDocumentFromNode(Document doc,Element copyTo,Element copyFrom){
//		int numOfEntries = copyFrom.getChildCount();
//		for (int i = 0; i < numOfEntries; i++) {
//			Element oldNode = copyFrom.getElement(i);
//			Element newNode = doc.createElement(null, null);
//			if(!copyFrom.isText(i)){
//				newNode.setName(oldNode.getName());
//				copyTo.addChild(Element.ELEMENT, newNode);
//				updateDocumentFromNode(doc,newNode,oldNode);
//			}
//			else
//				copyTo.addChild(Element.TEXT, copyFrom.getText(i));
//		}
//	}
//	
//	private static void updateModel(Document doc, QuestionData qtnData){
//		
//		//we dot spit out answers for invisible and disabled questions since
//		//they are considered non-relevant.
//		if(qtnData.getDef().isVisible() && qtnData.getDef().isEnabled()){
//			
//			String xpath = qtnData.getDef().getVariableName();
//			String value = qtnData.getValueAnswer();
//			
//			if (value != null) {
//				Element elem = getInstanceNode(doc);
//				xpath = new String(xpath.toCharArray(), 1, xpath.length()-1);
//				XPathExpression xpls = new XPathExpression(elem, xpath);
//				Vector result = xpls.getResult();
//				
//				for (Enumeration e = result.elements(); e.hasMoreElements();) {
//					Object obj = e.nextElement();
//					if (obj instanceof Element)
//						((Element) obj).addChild(Node.TEXT, value);
//				}
//			}
//		}
//	}
//	

//	

//	
//
//	
//	public static ByteArrayOutputStream fromDoc2Stream(Document doc) {
//		KXmlSerializer serializer = new KXmlSerializer();
//		ByteArrayOutputStream bos = new ByteArrayOutputStream();
//		DataOutputStream dos = new DataOutputStream(bos);
//		
//		try{
//			serializer.setOutput(dos,null);
//			doc.write(serializer);
//			serializer.flush();
//			return bos;
//		}
//		catch(Exception e){
//			e.printStackTrace();
//			return null;
//		}
//	}
//	
//	public static String fromDoc2String(Document doc){
//		ByteArrayOutputStream bos = fromDoc2Stream(doc);		
//		
//		byte[] byteArr = bos.toByteArray();
//		char[]charArray = new char[byteArr.length];
//		for(int i=0; i<byteArr.length; i++)
//			charArray[i] = (char)byteArr[i];
//		
//		return String.valueOf(charArray);
//	}
//	
//	

//	

//	
//	

//	

//	
//	public static FormData fromXform2FormData(Reader reader){			
//		Document doc = getDocument(reader);		
//		FormData formData = new FormData(getFormDef(doc));
//		
//		Element dataNode = doc.getRootElement().getElement(0).getElement(1).getElement(0).getElement(0);
//		for(int i=0; i<dataNode.getChildCount(); i++){
//			Element node = dataNode.getElement(i);
//			if(node != null && node.getChildCount() > 0)
//				formData.setValue(node.getName(), node.getText(0));
//		}
//			
//		return formData;
//	}
//	
//	

}
