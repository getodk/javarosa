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

package org.javarosa.xform.parse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.annotation.ElementType;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.DataBinding;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.ItemsetBinding;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.condition.Condition;
import org.javarosa.core.model.condition.Constraint;
import org.javarosa.core.model.condition.Recalculate;
import org.javarosa.core.model.condition.Triggerable;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.InvalidReferenceException;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.model.util.restorable.Restorable;
import org.javarosa.core.model.util.restorable.RestoreUtils;
import org.javarosa.core.services.locale.Localizer;
import org.javarosa.core.services.locale.TableLocaleSource;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.core.util.externalizable.PrototypeFactoryDeprecated;
import org.javarosa.model.xform.XPathReference;
import org.javarosa.xform.util.IXFormBindHandler;
import org.javarosa.xform.util.XFormAnswerDataParser;
import org.javarosa.xform.util.XFormSerializer;
import org.javarosa.xform.util.XFormUtils;
import org.javarosa.xpath.XPathConditional;
import org.javarosa.xpath.expr.XPathPathExpr;
import org.javarosa.xpath.parser.XPathSyntaxException;
import org.kxml2.io.KXmlParser;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/* droos: i think we need to start storing the contents of the <bind>s in the formdef again */

/**
 * Provides conversion from xform to epihandy object model and vice vasa.
 *
 * @author Daniel Kayiwa
 * @author Drew Roos
 *
 */
public class XFormParser {
	public static final String NAMESPACE_JAVAROSA = "http://openrosa.org/javarosa";

	private static Hashtable topLevelHandlers;
	private static Hashtable groupLevelHandlers;
	private static Hashtable typeMappings;
	private static PrototypeFactoryDeprecated modelPrototypes;

	/** IXFormBindHandler */
	private static Vector bindHandlers;

	/* THIS CLASS IS NOT THREAD-SAFE */
	//state variables -- not a good idea since this class is static, but that's not really a good idea either, now is it
	private static boolean modelFound;
	private static Hashtable bindingsByID;
	private static Vector bindings; //DataBinding
	private static Vector repeats; //TreeReference
	private static Vector itemsets; //ItemsetBinding
	private static Vector selectOnes; //TreeReference
	private static Vector selectMultis; //TreeReference
	private static Element instanceNode; //top-level data node of the instance; saved off so it can be processed after the <bind>s
	private static String defaultNamespace;
	
	private static FormInstance repeatTree; //pseudo-data model tree that describes the repeat structure of the instance;
										     //useful during instance processing and validation
	
	static {
		initProcessingRules();
		initTypeMappings();
		modelPrototypes = new PrototypeFactoryDeprecated();
		bindHandlers = new Vector();
	}

	/**
	 * Default Constructor
	 *
	 */
	public XFormParser(){

	}

	private static void initProcessingRules () {
		IElementHandler title = new IElementHandler () {
			public void handle (FormDef f, Element e, Object parent) { parseTitle(f, e); } };
		IElementHandler meta = new IElementHandler () {
			public void handle (FormDef f, Element e, Object parent) { parseMeta(f, e); } };
		IElementHandler model = new IElementHandler () {
			public void handle (FormDef f, Element e, Object parent) { parseModel(f, e); } };
		IElementHandler input = new IElementHandler () {
			public void handle (FormDef f, Element e, Object parent) { parseControl((IFormElement)parent, e, f, Constants.CONTROL_INPUT); } };
		IElementHandler select = new IElementHandler () {
			public void handle (FormDef f, Element e, Object parent) { parseControl((IFormElement)parent, e, f, Constants.CONTROL_SELECT_MULTI); } };
		IElementHandler select1 = new IElementHandler () {
			public void handle (FormDef f, Element e, Object parent) { parseControl((IFormElement)parent, e, f, Constants.CONTROL_SELECT_ONE); } };
		IElementHandler group = new IElementHandler () {
			public void handle (FormDef f, Element e, Object parent) { parseGroup((IFormElement)parent, e, f, CONTAINER_GROUP); } };
		IElementHandler repeat = new IElementHandler () {
			public void handle (FormDef f, Element e, Object parent) { parseGroup((IFormElement)parent, e, f, CONTAINER_REPEAT); } };
		IElementHandler groupLabel = new IElementHandler () {
			public void handle (FormDef f, Element e, Object parent) { parseGroupLabel(f, (GroupDef)parent, e); } };
		IElementHandler trigger = new IElementHandler () {
			public void handle (FormDef f, Element e, Object parent) { parseControl((IFormElement)parent, e, f, Constants.CONTROL_TRIGGER); } };
		IElementHandler upload = new IElementHandler () {
			public void handle (FormDef f, Element e, Object parent) { parseUpload((IFormElement)parent, e, f, Constants.CONTROL_UPLOAD); } };

		groupLevelHandlers = new Hashtable();
		groupLevelHandlers.put("input", input);
		groupLevelHandlers.put("select", select);
		groupLevelHandlers.put("select1", select1);
		groupLevelHandlers.put("group", group);
		groupLevelHandlers.put("repeat", repeat);
		groupLevelHandlers.put("trigger", trigger); //multi-purpose now; need to dig deeper
		groupLevelHandlers.put(Constants.XFTAG_UPLOAD, upload);

		topLevelHandlers = new Hashtable();
		for (Enumeration en = groupLevelHandlers.keys(); en.hasMoreElements(); ) {
			String key = (String)en.nextElement();
			topLevelHandlers.put(key, groupLevelHandlers.get(key));
		}
		topLevelHandlers.put("model", model);
		topLevelHandlers.put("title", title);
		topLevelHandlers.put("meta", meta);

		groupLevelHandlers.put("label", groupLabel);
	}

	private static void initTypeMappings () {
		typeMappings = new Hashtable();
		typeMappings.put("string", new Integer(Constants.DATATYPE_TEXT));               //xsd:
		typeMappings.put("integer", new Integer(Constants.DATATYPE_INTEGER));           //xsd:
		typeMappings.put("int", new Integer(Constants.DATATYPE_INTEGER));               //xsd:
		typeMappings.put("decimal", new Integer(Constants.DATATYPE_DECIMAL));           //xsd:
		typeMappings.put("double", new Integer(Constants.DATATYPE_DECIMAL));            //xsd:
		typeMappings.put("float", new Integer(Constants.DATATYPE_DECIMAL));             //xsd:
		typeMappings.put("dateTime", new Integer(Constants.DATATYPE_DATE_TIME));        //xsd:
		typeMappings.put("date", new Integer(Constants.DATATYPE_DATE));                 //xsd:
		typeMappings.put("time", new Integer(Constants.DATATYPE_TIME));                 //xsd:
		typeMappings.put("gYear", new Integer(Constants.DATATYPE_UNSUPPORTED));         //xsd:
		typeMappings.put("gMonth", new Integer(Constants.DATATYPE_UNSUPPORTED));        //xsd:
		typeMappings.put("gDay", new Integer(Constants.DATATYPE_UNSUPPORTED));          //xsd:
		typeMappings.put("gYearMonth", new Integer(Constants.DATATYPE_UNSUPPORTED));    //xsd:
		typeMappings.put("gMonthDay", new Integer(Constants.DATATYPE_UNSUPPORTED));     //xsd:
		typeMappings.put("boolean", new Integer(Constants.DATATYPE_BOOLEAN));           //xsd:
		typeMappings.put("base64Binary", new Integer(Constants.DATATYPE_UNSUPPORTED));  //xsd:
		typeMappings.put("hexBinary", new Integer(Constants.DATATYPE_UNSUPPORTED));     //xsd:
		typeMappings.put("anyURI", new Integer(Constants.DATATYPE_UNSUPPORTED));        //xsd:
		typeMappings.put("listItem", new Integer(Constants.DATATYPE_CHOICE));           //xforms:
		typeMappings.put("listItems", new Integer(Constants.DATATYPE_CHOICE_LIST));	    //xforms:	
		typeMappings.put("select1", new Integer(Constants.DATATYPE_CHOICE));	        //non-standard	
		typeMappings.put("select", new Integer(Constants.DATATYPE_CHOICE_LIST));        //non-standard
		typeMappings.put("geopoint", new Integer(Constants.DATATYPE_GEOPOINT));         //non-standard
		typeMappings.put("barcode", new Integer(Constants.DATATYPE_BARCODE));           //non-standard
        typeMappings.put("binary", new Integer(Constants.DATATYPE_BINARY));             //non-standard
	    
	}
	
	private static void initBindHandlers() {
		Enumeration en = bindHandlers.elements();
		while(en.hasMoreElements()) {
			IXFormBindHandler handler = (IXFormBindHandler)en.nextElement();
			handler.init();
			
		}
	}
	private static void processBindHandlers(FormDef formDef) {
		Enumeration en = bindHandlers.elements();
		while(en.hasMoreElements()) {
			IXFormBindHandler handler = (IXFormBindHandler)en.nextElement();
			handler.postProcess(formDef);
			
		}
	}

	private static void initStateVars () {
		modelFound = false;
		bindingsByID = new Hashtable();
		bindings = new Vector();
		repeats = new Vector();
		itemsets = new Vector();
		selectOnes = new Vector();
		selectMultis = new Vector();
		instanceNode = null;
		repeatTree = null;
		defaultNamespace = null;
	}

	public static FormDef getFormDef(Reader reader) {
		System.out.println("Parsing form...");
		
		Document doc = getXMLDocument(reader);
		try {
			reader.close();
		} catch (IOException e) {
			System.out.println("Error closing reader");
			e.printStackTrace();
		}
		
		if (doc != null) {
			try {
				return getFormDef(doc);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} else {
			return null;
		}
	}

	public static Document getXMLDocument(Reader reader){
		Document doc = new Document();

		try{
			KXmlParser parser = new KXmlParser();
			parser.setInput(reader);
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
			
			doc.parse(parser);
		}  catch (XmlPullParserException e) {
			System.err.println("XML Syntax Error at Line: " + e.getLineNumber() +", Column: "+ e.getColumnNumber()+ "!");
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(Exception e){
			//#if debug.output==verbose || debug.output==exception
			System.err.println("XML Syntax Error!");
			e.printStackTrace();
			//#endif

			return null;
		}

		return doc;
	}

	public static FormDef getFormDef(Document doc){
		FormDef formDef = new FormDef();
		
		initBindHandlers();
		initStateVars();

		defaultNamespace = doc.getRootElement().getNamespaceUri(null);
		
		parseElement(formDef, doc.getRootElement(), formDef, topLevelHandlers);
			
		collapseRepeatGroups(formDef);
		
		if(instanceNode != null) {
			parseInstance(formDef, instanceNode);
		}

		initStateVars();

		processBindHandlers(formDef);
		return formDef;
	}

	private static final int CONTAINER_GROUP = 1;
	private static final int CONTAINER_REPEAT = 2;

	private static void parseElement (FormDef f, Element e, Object parent, Hashtable handlers) { //,
//			boolean allowUnknownElements, boolean allowText, boolean recurseUnknown) {
		String name = e.getName();

		IElementHandler eh = (IElementHandler)handlers.get(name);
		if (eh != null) {
			eh.handle(f, e, parent);
		} else {
			if (!name.equals("html") && !name.equals("head") && !name.equals("body")) {
				//#if debug.output==verbose
				System.err.println("XForm Parse: Unrecognized element [" + name	+ "]. Ignoring and processing children..." + getVagueLocation(e));
				//#endif
			}
			for (int i = 0; i < e.getChildCount(); i++) {
				if (e.getType(i) == Element.ELEMENT) {
					parseElement(f, e.getElement(i), parent, handlers);
				}
			}
		}
	}

	private static void parseTitle (FormDef f, Element e) {
		Vector usedAtts = new Vector(); //no attributes parsed in title.
		String title = getXMLText(e, true);
		System.out.println("Title: \"" + title + "\"");
		f.setTitle(title);
		if(f.getName() == null) {
			//Jan 9, 2009 - ctsims
			//We don't really want to allow for forms without
			//some unique ID, so if a title is available, use
			//that.
			f.setName(title);
		}
		
		
		if(XFormUtils.showUnusedAttributeWarning(e, usedAtts)){
			//System.out.println("Debug:parseTitle()");
			System.out.println(XFormUtils.unusedAttWarning(e, usedAtts));
		}
	}
	
	private static void parseMeta (FormDef f, Element e) {
		Vector usedAtts = new Vector();
		int attributes = e.getAttributeCount();
		for(int i = 0 ; i < attributes ; ++i) {
			String name = e.getAttributeName(i);
			String value = e.getAttributeValue(i);
			if("name".equals(name)) {
				f.setName(value);
			}
		}
		

		usedAtts.addElement("name");
		if(XFormUtils.showUnusedAttributeWarning(e, usedAtts)){
			//System.out.println("Debug:parseTitle()2");
			System.out.println(XFormUtils.unusedAttWarning(e, usedAtts));
		}
	}

	//for ease of parsing, we assume a model comes before the controls, which isn't necessarily mandated by the xforms spec
	private static void parseModel (FormDef f, Element e) {
		Vector usedAtts = new Vector(); //no attributes parsed in title.
		
		
		if (modelFound) {
			//#if debug.output==verbose
			System.err.println("Multiple models not supported. Ignoring subsequent models." + getVagueLocation(e));
			//#endif
			return;
		}
		modelFound = true;
		
		if(XFormUtils.showUnusedAttributeWarning(e, usedAtts)){
			//System.out.println("Debug:parseModel()");
			System.out.println(XFormUtils.unusedAttWarning(e, usedAtts));
		}
		
		for (int i = 0; i < e.getChildCount(); i++) {
			
			int type = e.getType(i);
			Element child = (type == Node.ELEMENT ? e.getElement(i) : null);
			String childName = (child != null ? child.getName() : null);
			
			if ("itext".equals(childName)) {
				parseIText(f, child);
			} else if ("instance".equals(childName)) {
				//we save parsing the instance node until the end, giving us the information we need about
				//binds and data types and such
				saveInstanceNode(child);
			} else if ("bind".equals(childName)) { //<instance> must come before <bind>s
				parseBind(f, child);
			} else { //invalid model content
				if (type == Node.ELEMENT) {
					throw new XFormParseException("Unrecognized top-level tag [" + childName + "] found within <model>",child);
				} else if (type == Node.TEXT && getXMLText(e, i, true).length() != 0) {
					throw new XFormParseException("Unrecognized text content found within <model>: \"" + getXMLText(e, i, true) + "\"",child);					
				}
			}
			
			if(child == null || "bind".equals(childName) || "itext".equals(childName)) {
				//Clayton Sims - Jun 17, 2009 - This code is used when the stinginess flag
				//is set for the build. It dynamically wipes out old model nodes once they're
				//used. This is sketchy if anything else plans on touching the nodes.
				//This code can be removed once we're pull-parsing
				//#if org.javarosa.xform.stingy
				//# e.removeChild(i);
				//# --i;
				//#endif
			}
		}
		
		
	}

	private static void saveInstanceNode (Element instance) {
		if (instanceNode != null) {
			System.err.println("Multiple instances not supported. Ignoring subsequent instances." + getVagueLocation(instance));
			return;
		}
			
		for (int i = 0; i < instance.getChildCount(); i++) {
			if (instance.getType(i) == Node.ELEMENT) {
				if (instanceNode != null) {
					throw new XFormParseException("XForm Parse: <instance> has more than one child element", instance);
				} else {
					instanceNode = instance.getElement(i);
				}
			}
		}
	}
	
	private static int serialQuestionID = 1;
	
	protected static QuestionDef parseUpload(IFormElement parent, Element e, FormDef f,
			int controlUpload) {
		Vector usedAtts = new Vector();
		QuestionDef question = parseControl(parent, e, f, controlUpload);
		String mediaType = e.getAttributeValue(null, "mediatype");
		if ("image/*".equals(mediaType)) {
			// NOTE: this could be further expanded. 
			question.setControlType(Constants.CONTROL_IMAGE_CHOOSE);
		}
		else if("audio/*".equals(mediaType))
		{
            question.setControlType(Constants.CONTROL_AUDIO_CAPTURE);
        } 
		else if ("video/*".equals(mediaType)) 
		{
            question.setControlType(Constants.CONTROL_VIDEO_CAPTURE);
        }
		
		usedAtts.addElement("mediatype");
		if(XFormUtils.showUnusedAttributeWarning(e, usedAtts)){
			//System.out.println("Debug:parseUpload()");
			System.out.println(XFormUtils.unusedAttWarning(e, usedAtts));
		}
		
        return question;
    }
	
	protected static QuestionDef parseControl (IFormElement parent, Element e, FormDef f, int controlType) {
		QuestionDef question = new QuestionDef();
		question.setID(serialQuestionID++); //until we come up with a better scheme
		
		Vector usedAtts = new Vector();
		usedAtts.addElement("ref");
		usedAtts.addElement("bind");
		usedAtts.addElement("appearance");
		
		IDataReference dataRef = null;
		boolean refFromBind = false;
		
		String ref = e.getAttributeValue(null, "ref");
		String bind = e.getAttributeValue(null, "bind");
		
		if (bind != null) {
			DataBinding binding = (DataBinding)bindingsByID.get(bind);
			if (binding == null) {
				throw new XFormParseException("XForm Parse: invalid binding ID '" + bind + "'", e);
			}
			dataRef = binding.getReference();
			refFromBind = true;
		} else if (ref != null) {
			dataRef = new XPathReference(ref);
		} else {
			if (controlType == Constants.CONTROL_TRIGGER) {
				//TODO: special handling for triggers? also, not all triggers created equal
			} else {
				throw new XFormParseException("XForm Parse: input control with neither 'ref' nor 'bind'",e);
			}
		}

		if (dataRef != null) {
			if (!refFromBind) {
				dataRef = getAbsRef(dataRef, parent);
			}
			question.setBind(dataRef);
			
			if (controlType == Constants.CONTROL_SELECT_ONE) {
				selectOnes.addElement((TreeReference)dataRef.getReference());
			} else if (controlType == Constants.CONTROL_SELECT_MULTI) {
				selectMultis.addElement((TreeReference)dataRef.getReference());
			}
		}		

		boolean isSelect = (controlType == Constants.CONTROL_SELECT_MULTI || controlType == Constants.CONTROL_SELECT_ONE);
		question.setControlType(controlType);
		question.setAppearanceAttr(e.getAttributeValue(null, "appearance"));

		for (int i = 0; i < e.getChildCount(); i++) {
			int type = e.getType(i);
			Element child = (type == Node.ELEMENT ? e.getElement(i) : null);
			String childName = (child != null ? child.getName() : null);

			if ("label".equals(childName)) {
				parseQuestionLabel(f, question, child);
			} else if ("hint".equals(childName)) {
				parseHint(f, question, child);
			} else if (isSelect && "item".equals(childName)) {
				parseItem(f, question, child);
			} else if (isSelect && "itemset".equals(childName)) {
				parseItemset(f, question, child, parent);
			}
		}
		if (isSelect) {
			if (question.getNumChoices() > 0 && question.getDynamicChoices() != null) {
				throw new XFormParseException("Select question contains both literal choices and <itemset>");
			} else if (question.getNumChoices() == 0 && question.getDynamicChoices() == null) {
				throw new XFormParseException("Select question has no choices");
			}
		}
			
		parent.addChild(question);
		

		if(XFormUtils.showUnusedAttributeWarning(e, usedAtts)){
			//System.out.println("Debug:parseControl()");
			System.out.println(XFormUtils.unusedAttWarning(e, usedAtts));
		}
		
		
		return question;
	}

	private static void parseQuestionLabel (FormDef f, QuestionDef q, Element e) {
		String label = getLabel(e, f);
		String ref = e.getAttributeValue("", "ref");
		
		Vector usedAtts = new Vector();
		usedAtts.addElement("ref");
		
		if (ref != null) {
			if (ref.startsWith("jr:itext('") && ref.endsWith("')")) {
				String textRef = ref.substring("jr:itext('".length(), ref.indexOf("')"));

				verifyTextMappings(f, textRef, "Question <label>", true);
				q.setTextID(textRef);
			} else {
				throw new RuntimeException("malformed ref [" + ref + "] for <label>");
			}
		} else {
			q.setLabelInnerText(label);
		}
		

		if(XFormUtils.showUnusedAttributeWarning(e, usedAtts)){
			//System.out.println("Debug:parseQuestionLabel()");
			System.out.println(XFormUtils.unusedAttWarning(e, usedAtts));
		}
	}

	private static void parseGroupLabel (FormDef f, GroupDef g, Element e) {
		if (g.getRepeat())
			return; //ignore child <label>s for <repeat>; the appropriate <label> must be in the wrapping <group>
		
		Vector usedAtts = new Vector();
		usedAtts.addElement("ref");
		
		
		String label = getLabel(e, f);
		String ref = e.getAttributeValue("", "ref");

		if (ref != null) {
			if (ref.startsWith("jr:itext('") && ref.endsWith("')")) {
				String textRef = ref.substring("jr:itext('".length(), ref.indexOf("')"));

				verifyTextMappings(f, textRef, "Group <label>", true);
				g.setTextID(textRef);
			} else {
				throw new RuntimeException("malformed ref [" + ref + "] for <label>");
			}
		} else {
			g.setLabelInnerText(label);
		}
		

		if(XFormUtils.showUnusedAttributeWarning(e, usedAtts)){
			//System.out.println("Debug:parseGroupHandle()");
			System.out.println(XFormUtils.unusedAttWarning(e, usedAtts));
		}
	}
	
	private static String getLabel (Element e, FormDef f){
		if(e.getChildCount() == 0) return null;
		
		recurseForOutput(e,f);
		
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i<e.getChildCount();i++){
			if(e.getType(i)!=Node.TEXT && !(e.getChild(i) instanceof String)){
				Object b = e.getChild(i);
				if(b instanceof String)System.out.println("WTF?!?!");
				sb.append(XFormSerializer.elementToString((Element)b));
			}else{
				sb.append(e.getText(i));
			}
		}
		
		String s = sb.toString().trim();
		
		return s;
	}
	
	private static void recurseForOutput(Element e, FormDef f){
		if(e.getChildCount() == 0) return;
		
		for(int i=0;i<e.getChildCount();i++){
			int kidType = e.getType(i);
			if(kidType == Node.TEXT) continue;
			if(e.getChild(i) instanceof String) continue;
			Element kid = (Element)e.getChild(i);
			
				//is just text
			if(kidType == Node.ELEMENT && XFormUtils.isOutput(kid)){
				String s = "${"+parseOutput(kid, f)+"}";
				e.removeChild(i);
				e.addChild(i, Node.TEXT, s);
				
				//has kids? Recurse through them and swap output tag for parsed version
			}else if(kid.getChildCount() !=0){
				recurseForOutput(kid,f);
				//is something else
			}else{
				continue;
			}
		}
	}
	
	
	
/*	private static String getLabel (Element e, FormDef f) {
		return getLabel2(e,f);
		
		
		boolean outputFound = false;
		boolean otherStuffFound = false;
		Vector outputs = new Vector();
		for (int i = 0; i < e.getChildCount(); i++) {
			if (e.getType(i) != Node.TEXT) {
				if (e.getType(i) == Node.ELEMENT && "output".equals(e.getElement(i).getName())) {
					outputFound = true;
					outputs.addElement(new Integer(i));
				} else {
					otherStuffFound = true;
				}
			}
		}
		

		
		if (outputFound && !otherStuffFound) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < outputs.size() + 1; i++) {
				int ixStart = (i == 0 ? 0 : ((Integer)outputs.elementAt(i - 1)).intValue() + 1);
				if (ixStart < e.getChildCount()) {
					String piece = getXMLText(e, ixStart, false);
					if (piece != null) {
						sb.append(piece);
					}
				}
				
				if (i < outputs.size()) {
					int ix = ((Integer)outputs.elementAt(i)).intValue();
					sb.append("${" + parseOutput(e.getElement(ix), f) + "}");
				}
			}
			return sb.toString().trim();
		} else {
			return getXMLText(e, true);
		}
		
	}	
	*/
	
	private static String parseOutput (Element e, FormDef f) {
		Vector usedAtts = new Vector();
		usedAtts.addElement("ref");
		usedAtts.addElement("value");
		
		String xpath = e.getAttributeValue(null, "ref");
		if (xpath == null) {
			xpath = e.getAttributeValue(null, "value");
		}
		if (xpath == null) {
			throw new XFormParseException("XForm Parse: <output> without 'ref' or 'value'",e);	
		}
		
		XPathConditional expr = null;
		try {
			expr = new XPathConditional(xpath);
		} catch (XPathSyntaxException xse) {
			//#if debug.output==verbose
			System.err.println("Invalid XPath expression [" + xpath + "]!");
			return "";
			//#endif
		}

		int index = -1;
		if (f.getOutputFragments().contains(expr)) {
			index = f.getOutputFragments().indexOf(expr);
		} else {
			index = f.getOutputFragments().size();
			f.getOutputFragments().addElement(expr);
		}
		
		if(XFormUtils.showUnusedAttributeWarning(e, usedAtts)){
			//System.out.println("Debug:parseOutput()");
			System.out.println(XFormUtils.unusedAttWarning(e, usedAtts));
		}
		
		return String.valueOf(index);
	}
	
	private static void parseHint (FormDef f, QuestionDef q, Element e) {
		Vector usedAtts = new Vector();
		usedAtts.addElement("ref");
		String hint = getXMLText(e, true);
		String ref = e.getAttributeValue("", "ref");

		if (ref != null) {
			if (ref.startsWith("jr:itext('") && ref.endsWith("')")) {
				String textRef = ref.substring("jr:itext('".length(), ref.indexOf("')"));

				verifyTextMappings(f, textRef, "<hint>", false);
				q.setHelpTextID(textRef);
			} else {
				throw new RuntimeException("malformed ref [" + ref + "] for <hint>");
			}
		} else {
			q.setHelpText(hint);
		}
		
		if(XFormUtils.showUnusedAttributeWarning(e, usedAtts)){
			//System.out.println("Debug:parseHint()");
			System.out.println(XFormUtils.unusedAttWarning(e, usedAtts));
		}
	}

	private static void parseItem (FormDef f, QuestionDef q, Element e) {
		final int MAX_VALUE_LEN = 32;
		
		//catalogue of used attributes in this method/element
		Vector usedAtts = new Vector();
		Vector labelUA = new Vector();
		Vector valueUA = new Vector();
		labelUA.addElement("ref");
		valueUA.addElement("form");
		
		
		String label = null;
		String textRef = null;
		String value = null;

		for (int i = 0; i < e.getChildCount(); i++) {
			int type = e.getType(i);
			Element child = (type == Node.ELEMENT ? e.getElement(i) : null);
			String childName = (child != null ? child.getName() : null);

			if ("label".equals(childName)) {
				
				//print attribute warning for child element
				if(XFormUtils.showUnusedAttributeWarning(child, labelUA)){
					//System.out.println("Debug:parseItem()");
					//System.out.println("Debug: "+XFormUtils.getAttributeList(child));
					System.out.println(XFormUtils.unusedAttWarning(child, labelUA));
				}
				label = getXMLText(child, true);
				String ref = child.getAttributeValue("", "ref");

				if (ref != null) {
					if (ref.startsWith("jr:itext('") && ref.endsWith("')")) {
						textRef = ref.substring("jr:itext('".length(), ref.indexOf("')"));

						verifyTextMappings(f, textRef, "Item <label>", true);
					} else {
						throw new XFormParseException("malformed ref [" + ref + "] for <item>",child);
					}
				}
			} else if ("value".equals(childName)) {
				value = getXMLText(child, true);
				
				//print attribute warning for child element
				if(XFormUtils.showUnusedAttributeWarning(child, valueUA)){
					//System.out.println("Debug:parseItem2()");
					System.out.println(XFormUtils.unusedAttWarning(child, valueUA));
				}
				
				if (value.length() > MAX_VALUE_LEN) {
					System.err.println("WARNING: choice value [" + value + "] is too long; max. suggested length " + MAX_VALUE_LEN + " chars" + getVagueLocation(child));
				}
				
				//validate
				for (int k = 0; k < value.length(); k++) {
					char c = value.charAt(k);
									
					if (" \n\t\f\r\'\"`".indexOf(c) >= 0) {
						boolean isMultiSelect = (q.getControlType() == Constants.CONTROL_SELECT_MULTI);
						System.err.println("XForm Parse WARNING: " + (isMultiSelect ? "select" : "select1") + " question <value>s [" + value + "] " +
								(isMultiSelect ? "cannot" : "should not") + " contain spaces, and are recommended not to contain apostraphes/quotation marks" + getVagueLocation(child));
						break;
					}
				}
			}
		}
		
		if (textRef == null && label == null) {
			throw new XFormParseException("<item> without proper <label>",e);
		}
		if (value == null) {
			throw new XFormParseException("<item> without proper <value>",e);
		}

		if (textRef != null) {
			q.addSelectChoice(new SelectChoice(textRef, value));
		} else {
			q.addSelectChoice(new SelectChoice(null,label, value, false));
		}
		
		//print unused attribute warning message for parent element
		if(XFormUtils.showUnusedAttributeWarning(e, usedAtts)){
			//System.out.println("Debug:parseItem3()");
			System.out.println(XFormUtils.unusedAttWarning(e, usedAtts));
		}
	}

	private static void parseItemset (FormDef f, QuestionDef q, Element e, IFormElement qparent) {
		ItemsetBinding itemset = new ItemsetBinding();
		
		//catalogue of used attributes in this method/element
		Vector usedAtts = new Vector();
		Vector labelUA = new Vector(); //for child with name 'label'
		Vector valueUA = new Vector(); //for child with name 'value'
		Vector copyUA = new Vector(); //for child with name 'copy'
		usedAtts.addElement("nodeset");
		labelUA.addElement("ref");
		valueUA.addElement("ref");
		valueUA.addElement("form");
		copyUA.addElement("ref");
		
		
		String nodesetStr = e.getAttributeValue("", "nodeset");
		XPathPathExpr path = XPathReference.getPathExpr(nodesetStr);
		itemset.nodesetExpr = new XPathConditional(path);
		itemset.contextRef = getFormElementRef(qparent);
		itemset.nodesetRef = FormInstance.unpackReference(getAbsRef(new XPathReference(path.getReference(true)), itemset.contextRef));
		
		for (int i = 0; i < e.getChildCount(); i++) {
			int type = e.getType(i);
			Element child = (type == Node.ELEMENT ? e.getElement(i) : null);
			String childName = (child != null ? child.getName() : null);

			if ("label".equals(childName)) {
				String labelXpath = child.getAttributeValue("", "ref");
				boolean labelItext = false;
				
				//print unused attribute warning message for child element
				if(XFormUtils.showUnusedAttributeWarning(child, labelUA)){
					//System.out.println("Debug:parseItemset()");
					System.out.println(XFormUtils.unusedAttWarning(child, labelUA));
				}
				
				if (labelXpath != null) {
					if (labelXpath.startsWith("jr:itext(") && labelXpath.endsWith(")")) {
						labelXpath = labelXpath.substring("jr:itext(".length(), labelXpath.indexOf(")"));
						labelItext = true;
					}
				} else {
					throw new XFormParseException("<label> in <itemset> requires 'ref'");
				}
				
				XPathPathExpr labelPath = XPathReference.getPathExpr(labelXpath);
				itemset.labelRef = FormInstance.unpackReference(getAbsRef(new XPathReference(labelPath), itemset.nodesetRef));
				itemset.labelExpr = new XPathConditional(labelPath);
				itemset.labelIsItext = labelItext;
			} else if ("copy".equals(childName)) {
				String copyRef = child.getAttributeValue("", "ref");

				//print unused attribute warning message for child element
				if(XFormUtils.showUnusedAttributeWarning(child, copyUA)){
					//System.out.println("Debug:parseItemset()2");
					System.out.println(XFormUtils.unusedAttWarning(child, copyUA));
				}
				
				if (copyRef == null) {
					throw new XFormParseException("<copy> in <itemset> requires 'ref'");
				}
				
				itemset.copyRef = FormInstance.unpackReference(getAbsRef(new XPathReference(copyRef), itemset.nodesetRef));
				itemset.copyMode = true;
			} else if ("value".equals(childName)) {
				String valueXpath = child.getAttributeValue("", "ref");
				
				//print unused attribute warning message for child element
				if(XFormUtils.showUnusedAttributeWarning(child, valueUA)){
					//System.out.println("Debug:parseItemset()3");
					System.out.println(XFormUtils.unusedAttWarning(child, valueUA));
				}
				
				if (valueXpath == null) {
					throw new XFormParseException("<value> in <itemset> requires 'ref'");
				}
				
				XPathPathExpr valuePath = XPathReference.getPathExpr(valueXpath);
				itemset.valueRef = FormInstance.unpackReference(getAbsRef(new XPathReference(valuePath), itemset.nodesetRef));
				itemset.valueExpr = new XPathConditional(valuePath);
				itemset.copyMode = false;
			}
		}
		
		if (itemset.labelRef == null) {
			throw new XFormParseException("<itemset> requires <label>");
		} else if (itemset.copyRef == null && itemset.valueRef == null) {			
			throw new XFormParseException("<itemset> requires <copy> or <value>");
		}
		
		if (itemset.copyRef != null) {
			if (itemset.valueRef == null) {
				System.err.println("WARNING: <itemset>s with <copy> are STRONGLY recommended to have <value> as well; pre-selecting, default answers, and display of answers will not work properly otherwise");
			} else if (!itemset.copyRef.isParentOf(itemset.valueRef, false)) {
				throw new XFormParseException("<value> is outside <copy>");					
			}
		}
		
		q.setDynamicChoices(itemset);
		itemsets.addElement(itemset);
		
		//print unused attribute warning message for parent element
		if(XFormUtils.showUnusedAttributeWarning(e, usedAtts)){
			//System.out.println("Debug:parseItemset()4");
			System.out.println(XFormUtils.unusedAttWarning(e, usedAtts));
		}
	}
	
	private static void parseGroup (IFormElement parent, Element e, FormDef f, int groupType) {
		GroupDef group = new GroupDef();
		group.setID(serialQuestionID++); //until we come up with a better scheme
		IDataReference dataRef = null;
		boolean refFromBind = false;
		
		Vector usedAtts = new Vector();
		usedAtts.addElement("ref");
		usedAtts.addElement("nodeset");
		usedAtts.addElement("bind");
		usedAtts.addElement("appearance");
		usedAtts.addElement("count");
		usedAtts.addElement("noAddRemove");
		
		if (groupType == CONTAINER_REPEAT) {
			group.setRepeat(true);
		}
		
		String ref = e.getAttributeValue(null, "ref");
		String nodeset = e.getAttributeValue(null, "nodeset");
		String bind = e.getAttributeValue(null, "bind");
		group.setAppearanceAttr(e.getAttributeValue(null, "appearance"));
		
		if (bind != null) {
			DataBinding binding = (DataBinding)bindingsByID.get(bind);
			if (binding == null) {
				throw new XFormParseException("XForm Parse: invalid binding ID [" + bind + "]",e);
			}
			dataRef = binding.getReference();
			refFromBind = true;
		} else {
			if (group.getRepeat()) {
				if (nodeset != null) {
					dataRef = new XPathReference(nodeset);
				} else {
					throw new XFormParseException("XForm Parse: <repeat> with no binding ('bind' or 'nodeset')",e);
				}
			} else {
				if (ref != null) {
					dataRef = new XPathReference(ref);
				} //<group> not required to have a binding
			}
		}

		if (!refFromBind) {
			dataRef = getAbsRef(dataRef, parent);
		}
		group.setBind(dataRef);
		
		if (group.getRepeat()) {
			repeats.addElement((TreeReference)dataRef.getReference());

			String countRef = e.getAttributeValue(NAMESPACE_JAVAROSA, "count");
			if (countRef != null) {
				group.count = getAbsRef(new XPathReference(countRef), parent);
				group.noAddRemove = true;
			} else {
				group.noAddRemove = (e.getAttributeValue(NAMESPACE_JAVAROSA, "noAddRemove") != null);				
			}
		}

		//the case of a group wrapping a repeat is cleaned up in a post-processing step (collapseRepeatGroups)
		
		for (int i = 0; i < e.getChildCount(); i++) {
			if (e.getType(i) == Element.ELEMENT) {
				parseElement(f, e.getElement(i), group, groupLevelHandlers);
			}
		}
		
		//print unused attribute warning message for parent element
		if(XFormUtils.showUnusedAttributeWarning(e, usedAtts)){
			//System.out.println("Debug:parseGroup()");
			System.out.println(XFormUtils.unusedAttWarning(e, usedAtts));
		}

		parent.addChild(group);
	}

	private static TreeReference getFormElementRef (IFormElement fe) {
		if (fe instanceof FormDef) {
			TreeReference ref = TreeReference.rootRef();
			ref.add(instanceNode.getName(), 0);
			return ref;
		} else {
			return (TreeReference)fe.getBind().getReference();	
		}
	}
	
	private static IDataReference getAbsRef (IDataReference ref, IFormElement parent) {
		return getAbsRef(ref, getFormElementRef(parent));
	}
	
	//take a (possibly relative) reference, and make it absolute based on its parent
	private static IDataReference getAbsRef (IDataReference ref, TreeReference parentRef) {
		TreeReference tref;
		
		if (!parentRef.isAbsolute()) {
			throw new RuntimeException("XFormParser.getAbsRef: parentRef must be absolute");
		}
		
		if (ref != null) {
			tref = (TreeReference)ref.getReference();
		} else {
			tref = TreeReference.selfRef(); //only happens for <group>s with no binding
		}		
		
		tref = tref.parent(parentRef);
		if (tref == null) {
			throw new XFormParseException("Binding path [" + tref + "] not allowed with parent binding of [" + parentRef + "]");
		}
		
		return new XPathReference(tref);
	}
	
	//collapse groups whose only child is a repeat into a single repeat that uses the label of the wrapping group
	private static void collapseRepeatGroups (IFormElement fe) {
		if (fe.getChildren() == null)
			return;
		
		for (int i = 0; i < fe.getChildren().size(); i++) {
			IFormElement child = fe.getChild(i);
			GroupDef group = null;
			if (child instanceof GroupDef)
				group = (GroupDef)child;

			if (group != null) {
				if (!group.getRepeat() && group.getChildren().size() == 1) {
					IFormElement grandchild = (IFormElement)group.getChildren().elementAt(0);
					GroupDef repeat = null;
					if (grandchild instanceof GroupDef)
						repeat = (GroupDef)grandchild;
					
					if (repeat != null && repeat.getRepeat()) {
						//collapse the wrapping group
						
						//merge group into repeat
						//id - later
						//name - later
						repeat.setLabelInnerText(group.getLabelInnerText());
						repeat.setTextID(group.getTextID());
//						repeat.setLongText(group.getLongText());
//						repeat.setShortText(group.getShortText());
//						repeat.setLongTextID(group.getLongTextID(), null);
//						repeat.setShortTextID(group.getShortTextID(), null);						
						//don't merge binding; repeat will always already have one
						
						//replace group with repeat
						fe.getChildren().setElementAt(repeat, i);
						group = repeat;
					}
				}
				
				collapseRepeatGroups(group);
			}
		}
	}
	
	private static void parseIText (FormDef f, Element itext) {
		Localizer l = new Localizer(true, true);
		f.setLocalizer(l);
		l.registerLocalizable(f);

		Vector usedAtts = new Vector(); //used for warning message
		
		for (int i = 0; i < itext.getChildCount(); i++) {
			Element trans = itext.getElement(i);
			if (trans == null || !trans.getName().equals("translation"))
				continue;

			parseTranslation(l, trans, f);
		}

		if (l.getAvailableLocales().length == 0)
			throw new XFormParseException("no <translation>s defined",itext);

		if (l.getDefaultLocale() == null)
			l.setDefaultLocale(l.getAvailableLocales()[0]);
		
		//print unused attribute warning message for parent element
		if(XFormUtils.showUnusedAttributeWarning(itext, usedAtts)){
			//System.out.println("Debug:parseItext()");
			System.out.println(XFormUtils.unusedAttWarning(itext, usedAtts));
		}
	}

	private static void parseTranslation (Localizer l, Element trans, FormDef f) {
		/////for warning message
		Vector usedAtts = new Vector();
		usedAtts.addElement("lang");
		usedAtts.addElement("default");
		/////////////////////////
		
		String lang = trans.getAttributeValue("", "lang");
		if (lang == null || lang.length() == 0)
			throw new XFormParseException("no language specified for <translation>",trans);
		String isDefault = trans.getAttributeValue("", "default");

		if (!l.addAvailableLocale(lang))
			throw new XFormParseException("duplicate <translation> for language '" + lang + "'",trans);

		if (isDefault != null) {
			if (l.getDefaultLocale() != null)
				throw new XFormParseException("more than one <translation> set as default",trans);
			l.setDefaultLocale(lang);
		}
		
		TableLocaleSource source = new TableLocaleSource();

		for (int j = 0; j < trans.getChildCount(); j++) {
			Element text = trans.getElement(j);
			if (text == null || !text.getName().equals("text"))
				continue;

			parseTextHandle(source, text, f);
			//Clayton Sims - Jun 17, 2009 - This code is used when the stinginess flag
			//is set for the build. It dynamically wipes out old model nodes once they're
			//used. This is sketchy if anything else plans on touching the nodes.
			//This code can be removed once we're pull-parsing
			//#if org.javarosa.xform.stingy
			//# trans.removeChild(j);
			//# --j;
			//#endif
		}
		
		//print unused attribute warning message for parent element
		if(XFormUtils.showUnusedAttributeWarning(trans, usedAtts)){
			//System.out.println("Debug:parseTranslation()");
			System.out.println(XFormUtils.unusedAttWarning(trans, usedAtts));
		}
		
		l.registerLocaleResource(lang, source);
	}

	private static void parseTextHandle (TableLocaleSource l, Element text, FormDef f) {
		String id = text.getAttributeValue("", "id");
		
		//used for parser warnings...
		Vector usedAtts = new Vector();
		Vector childUsedAtts = new Vector();
		usedAtts.addElement("id");
		usedAtts.addElement("form");
		childUsedAtts.addElement("form");
		childUsedAtts.addElement("id");
		//////////
		
		if (id == null || id.length() == 0)
			throw new XFormParseException("no id defined for <text>",text);

		for (int k = 0; k < text.getChildCount(); k++) {
			Element value = text.getElement(k);
			if (value == null) continue;
			if(!value.getName().equals("value")){
				throw new XFormParseException("Unrecognized element ["+value.getName()+"] in Itext->translation->text");
			}

			String form = value.getAttributeValue("", "form");
			if (form != null && form.length() == 0)
				form = null;
			String data = getLabel(value, f);
			if (data == null)
				data = "";

			String textID = (form == null ? id : id + ";" + form);  //kind of a hack
			if (l.hasMapping(textID))
				throw new XFormParseException("duplicate definition for text ID \"" + id + "\" and form \"" + form + "\""+". Can only have one definition for each text form.",text);
			l.setLocaleMapping(textID, data);
			
			//print unused attribute warning message for child element
			if(XFormUtils.showUnusedAttributeWarning(value, childUsedAtts)){
				System.out.println(XFormUtils.unusedAttWarning(value, childUsedAtts));
				//System.out.println("Debug:parseTextHandle()");
			}
		}
		
		//print unused attribute warning message for parent element
		if(XFormUtils.showUnusedAttributeWarning(text, usedAtts)){
			//System.out.println("Debug:parseTextHandle()2");
			System.out.println(XFormUtils.unusedAttWarning(text, usedAtts));
		}
	}

	private static boolean hasITextMapping (FormDef form, String textID, String locale) {
		Localizer l = form.getLocalizer();
		return l.hasMapping(locale == null ? l.getDefaultLocale() : locale, textID);
	}
	
	private static void verifyTextMappings (FormDef form, String textID, String type, boolean allowSubforms) {
		Localizer l = form.getLocalizer();
		String[] locales = l.getAvailableLocales();
		
		for (int i = 0; i < locales.length; i++) {
			if (!(hasITextMapping(form, textID, locales[i]) ||
					(allowSubforms && hasITextMapping(form, textID + ";long", locales[i]) && hasITextMapping(form, textID + ";short", locales[i])))) {
				if (locales[i].equals(l.getDefaultLocale())) {
					throw new XFormParseException(type + " '" + textID + "': text is not localizable for default locale [" + l.getDefaultLocale() + "]!");
				} else {
					System.err.println("Warning: " + type + " '" + textID + "': text is not localizable for locale " + locales[i] + ".");
				}
			}
		}
	}

	private static void parseBind (FormDef f, Element e) {
		DataBinding binding  = new DataBinding();
		
		Vector usedAtts = new Vector();
		usedAtts.addElement("id");
		usedAtts.addElement("nodeset");
		usedAtts.addElement("type");
		usedAtts.addElement("relevant");
		usedAtts.addElement("required");
		usedAtts.addElement("readonly");
		usedAtts.addElement("constraint");
		usedAtts.addElement("constraintMsg");
		usedAtts.addElement("calculate");
		usedAtts.addElement("preload");
		usedAtts.addElement("preloadParams");
		
		binding.setId(e.getAttributeValue("", "id"));

		String nodeset = e.getAttributeValue(null, "nodeset");
		if (nodeset == null) {
			throw new XFormParseException("XForm Parse: <bind> without nodeset",e);
		}
		IDataReference ref = new XPathReference(nodeset);
		ref = getAbsRef(ref, f);
		binding.setReference(ref);

		binding.setDataType(getDataType(e.getAttributeValue(null, "type")));

		String xpathRel = e.getAttributeValue(null, "relevant");
		if (xpathRel != null) {
			if ("true()".equals(xpathRel)) {
				binding.relevantAbsolute = true;
			} else if ("false()".equals(xpathRel)) {
				binding.relevantAbsolute = false;
			} else {
				Condition c = buildCondition(xpathRel, "relevant", ref);
				c = (Condition)f.addTriggerable(c);
				binding.relevancyCondition = c;
			}
		}

		String xpathReq = e.getAttributeValue(null, "required");
		if (xpathReq != null) {
			if ("true()".equals(xpathReq)) {
				binding.requiredAbsolute = true;
			} else if ("false()".equals(xpathReq)) {
				binding.requiredAbsolute = false;
			} else {
				Condition c = buildCondition(xpathReq, "required", ref);
				c = (Condition)f.addTriggerable(c);
				binding.requiredCondition = c;
			}
		}

		String xpathRO = e.getAttributeValue(null, "readonly");
		if (xpathRO != null) {
			if ("true()".equals(xpathRO)) {
				binding.readonlyAbsolute = true;
			} else if ("false()".equals(xpathRO)) {
				binding.readonlyAbsolute = false;
			} else {
				Condition c = buildCondition(xpathRO, "readonly", ref);
				c = (Condition)f.addTriggerable(c);
				binding.readonlyCondition = c;
			}
		}

		String xpathConstr = e.getAttributeValue(null, "constraint");
		if (xpathConstr != null) {
			try {
				binding.constraint = new XPathConditional(xpathConstr);
			} catch (XPathSyntaxException xse) {
				//#if debug.output==verbose
				System.err.println("Invalid XPath expression [" + xpathConstr + "]!" + getVagueLocation(e));
				//#endif
			}
			binding.constraintMessage = e.getAttributeValue(NAMESPACE_JAVAROSA, "constraintMsg");
		}
		
		String xpathCalc = e.getAttributeValue(null, "calculate");
		if (xpathCalc != null) {
			Recalculate r = buildCalculate(xpathCalc, ref);
			r = (Recalculate)f.addTriggerable(r);
			binding.calculate = r;
		}

		binding.setPreload(e.getAttributeValue(NAMESPACE_JAVAROSA, "preload"));
		binding.setPreloadParams(e.getAttributeValue(NAMESPACE_JAVAROSA, "preloadParams"));

		//custom bind handlers
		Enumeration en = bindHandlers.elements();
		while(en.hasMoreElements()) {
			((IXFormBindHandler)en.nextElement()).handle(e, binding);
		}
		
		//print unused attribute warning message for parent element
		if(XFormUtils.showUnusedAttributeWarning(e, usedAtts)){
			//System.out.println("Debug:parseBind()");
			System.out.println(XFormUtils.unusedAttWarning(e, usedAtts));
		}

		addBinding(binding);
	}

	private static Condition buildCondition (String xpath, String type, IDataReference contextRef) {
		XPathConditional cond;
		int trueAction = -1, falseAction = -1;

		if ("relevant".equals(type)) {
			trueAction = Condition.ACTION_SHOW;
			falseAction = Condition.ACTION_HIDE;
		} else if ("required".equals(type)) {
			trueAction = Condition.ACTION_REQUIRE;
			falseAction = Condition.ACTION_DONT_REQUIRE;
		} else if ("readonly".equals(type)) {
			trueAction = Condition.ACTION_DISABLE;
			falseAction = Condition.ACTION_ENABLE;
		}

		try {
			cond = new XPathConditional(xpath);
		} catch (XPathSyntaxException xse) {
			//#if debug.output==verbose
			System.err.println("Invalid XPath expression [" + xpath + "]!");
			//#endif
			return null;
		}
				
		Condition c = new Condition(cond, trueAction, falseAction, FormInstance.unpackReference(contextRef));
		return c;
	}
	
	private static Recalculate buildCalculate (String xpath, IDataReference contextRef) {
		XPathConditional calc;

		try {
			calc = new XPathConditional(xpath);
		} catch (XPathSyntaxException xse) {
			//#if debug.output==verbose
			System.err.println("Invalid XPath expression [" + xpath + "]!");
			//#endif
			return null;
		}
				
		Recalculate r = new Recalculate(calc, FormInstance.unpackReference(contextRef));
		return r;
	}
	
	private static void addBinding (DataBinding binding) {
		bindings.addElement(binding);
		
		if (binding.getId() != null) {
			if (bindingsByID.put(binding.getId(), binding) != null) {
				throw new XFormParseException("XForm Parse: <bind>s with duplicate ID: '" + binding.getId() + "'");
			}
		}
	}
		
	//e is the top-level _data_ node of the instance (immediate (and only) child of <instance>)
	private static void parseInstance (FormDef f, Element e) {
		TreeElement root = buildInstanceStructure(e, null);
		FormInstance instanceModel = new FormInstance(root);
		instanceModel.setName(f.getTitle());
		
		Vector usedAtts = new Vector();
		usedAtts.addElement("version");
		usedAtts.addElement("uiVersion");
		
		String schema = e.getNamespace();
		if (schema != null && schema.length() > 0 && !schema.equals(defaultNamespace)) {
			instanceModel.schema = schema;
		}
		instanceModel.formVersion = e.getAttributeValue(null, "version");
		instanceModel.uiVersion = e.getAttributeValue(null, "uiVersion");
		
		loadNamespaces(e, instanceModel);
			
		processRepeats(instanceModel);
		verifyBindings(f, instanceModel);
		applyInstanceProperties(instanceModel);
		loadInstanceData(e, root, f); //FIXME: FormDef param is temporary
		
		checkDependencyCycles(f);
		f.finalizeTriggerables();
		
		f.setInstance(instanceModel);
		
		//print unused attribute warning message for parent element
		if(XFormUtils.showUnusedAttributeWarning(e, usedAtts)){
			//System.out.println("Debug:parseInstance()");
			System.out.println(XFormUtils.unusedAttWarning(e, usedAtts));
		}
	}
	
	private static Hashtable loadNamespaces(Element e, FormInstance tree) {
		Hashtable prefixes = new Hashtable();
		for(int i = 0 ; i < e.getNamespaceCount(); ++i ) {
			String uri = e.getNamespaceUri(i);
			String prefix = e.getNamespacePrefix(i);
			if(uri != null && prefix != null) {
				tree.addNamespace(prefix, uri);
			}
		}
		return prefixes;
	}
	
	//parse instance hierarchy and turn into a skeleton model; ignoring data content, but respecting repeated nodes and 'template' flags
	public static TreeElement buildInstanceStructure (Element node, TreeElement parent) {
		TreeElement element = null;

		//catch when text content is mixed with children
		int numChildren = node.getChildCount();		
		boolean hasText = false;
		boolean hasElements = false;
		for (int i = 0; i < numChildren; i++) {
			switch (node.getType(i)) {
			case Node.ELEMENT:
				hasElements = true; break;
			case Node.TEXT:
				if (node.getText(i).trim().length() > 0)
					hasText = true;
				break;
			}
		}
		if (hasElements && hasText) {
			System.out.println("Warning: instance node '" + node.getName() + "' contains both elements and text as children; text ignored");
		}
		
		//check for repeat templating
		String name = node.getName();
		int multiplicity;
		if (node.getAttributeValue(NAMESPACE_JAVAROSA, "template") != null) {
			multiplicity = TreeReference.INDEX_TEMPLATE;
			if (parent != null && parent.getChild(name, TreeReference.INDEX_TEMPLATE) != null) {
				throw new XFormParseException("More than one node declared as the template for the same repeated set [" + name + "]",node);
			}
		} else {
			multiplicity = (parent == null ? 0 : parent.getChildMultiplicity(name));
		}
			
		
		String modelType = node.getAttributeValue(NAMESPACE_JAVAROSA, "modeltype");
		//create node; handle children
		if(modelType == null) {
			element = new TreeElement(name, multiplicity);
		} else {
            if( typeMappings.get(modelType) == null ){
                throw new XFormParseException("ModelType " + modelType + " is not recognized.",node);                
            }
            element = (TreeElement)modelPrototypes.getNewInstance(((Integer)typeMappings.get(modelType)).toString());
			if(element == null) {
				element = new TreeElement(name, multiplicity);
				System.out.println("No model type prototype available for " + modelType);
			} else {
				element.setName(name);
				element.setMult(multiplicity);
			}
		}

		if (hasElements) {
			for (int i = 0; i < numChildren; i++) {
				if (node.getType(i) == Node.ELEMENT) {
					element.addChild(buildInstanceStructure(node.getElement(i), element));
				}
			}
		}

		//handle attributes
		if (node.getAttributeCount() > 0) {
			for (int i = 0; i < node.getAttributeCount(); i++) {
				String attrNamespace = node.getAttributeNamespace(i);
				String attrName = node.getAttributeName(i);
				if (attrNamespace.equals(NAMESPACE_JAVAROSA) && attrName.equals("template"))
					continue;
				if (attrNamespace.equals(NAMESPACE_JAVAROSA) && attrName.equals("recordset"))
					continue;
				
				element.setAttribute(attrNamespace, attrName, node.getAttributeValue(i));
			}
		}
		
		return element;
	}
	
	private static Vector<TreeReference> getRepeatableRefs () {
		Vector<TreeReference> refs = new Vector<TreeReference>();

		for (int i = 0; i < repeats.size(); i++) {
			refs.addElement((TreeReference)repeats.elementAt(i));
		}

		for (int i = 0; i < itemsets.size(); i++) {
			ItemsetBinding itemset = (ItemsetBinding)itemsets.elementAt(i);
			TreeReference srcRef = itemset.nodesetRef;
			if (!refs.contains(srcRef)) {
				refs.addElement(srcRef);
			}
			
			if (itemset.copyMode) {
				TreeReference destRef = itemset.getDestRef();
				if (!refs.contains(destRef)) {
					refs.addElement(destRef);
				}				
			}
		}
		
		return refs;
	}
	
	//pre-process and clean up instance regarding repeats; in particular:
	// 1) flag all repeat-related nodes as repeatable
	// 2) catalog which repeat template nodes are explicitly defined, and note which repeats bindings lack templates
	// 3) remove template nodes that are not valid for a repeat binding
	// 4) generate template nodes for repeat bindings that do not have one defined explicitly
	// 5) give a stern warning for any repeated instance nodes that do not correspond to a repeat binding
	// 6) verify that all sets of repeated nodes are homogeneous
	private static void processRepeats (FormInstance instance) {
		flagRepeatables(instance);
		processTemplates(instance);
		checkDuplicateNodesAreRepeatable(instance.getRoot());	
		checkHomogeneity(instance);
	}

	//flag all nodes identified by repeat bindings as repeatable
	private static void flagRepeatables (FormInstance instance) {
		Vector refs = getRepeatableRefs();
		for (int i = 0; i < refs.size(); i++) {
			TreeReference ref = (TreeReference)refs.elementAt(i);
			Vector nodes = instance.expandReference(ref, true);
			for (int j = 0; j < nodes.size(); j++) {
				TreeReference nref = (TreeReference)nodes.elementAt(j);
				TreeElement node = instance.resolveReference(nref);
				if (node != null) //catch '/'
					node.repeatable = true;
			}
		}		
	}
	
	private static void processTemplates (FormInstance instance) {
		repeatTree = buildRepeatTree(getRepeatableRefs(), instance.getRoot().getName());
		
		Vector missingTemplates = new Vector(); //Vector<TreeReference>
		checkRepeatsForTemplate(instance, repeatTree, missingTemplates);

		removeInvalidTemplates(instance, repeatTree);
		createMissingTemplates(instance, missingTemplates);
	}
	
	//build a pseudo-data model tree that describes the repeat structure of the instance
	//result is a FormInstance collapsed where all indexes are 0, and repeatable nodes are flagged as such
	//return null if no repeats
	//ignores (invalid) repeats that bind outside the top-level instance data node
	private static FormInstance buildRepeatTree (Vector repeatRefs, String topLevelName) {
		TreeElement root = new TreeElement(null, 0);
		
		for (int i = 0; i < repeatRefs.size(); i++) {
			TreeReference repeatRef = (TreeReference)repeatRefs.elementAt(i);
			if (repeatRef.size() <= 1) {
				//invalid repeat: binds too high. ignore for now and error will be raised in verifyBindings
				continue;
			}
			
			TreeElement cur = root;
			for (int j = 0; j < repeatRef.size(); j++) {
				String name = repeatRef.getName(j);
				TreeElement child = cur.getChild(name, 0);
				if (child == null) {
					child = new TreeElement(name, 0);
					cur.addChild(child);
				}
				
				cur = child;
			}
			cur.repeatable = true;
		}
		
		if (root.getNumChildren() == 0)
			return null;
		else
			return new FormInstance(root.getChild(topLevelName, TreeReference.DEFAULT_MUTLIPLICITY));
	}

	//checks which repeat bindings have explicit template nodes; returns a vector of the bindings that do not
	private static void checkRepeatsForTemplate (FormInstance instance, FormInstance repeatTree, Vector missingTemplates) {
		if (repeatTree != null)
			checkRepeatsForTemplate(repeatTree.getRoot(), TreeReference.rootRef(), instance, missingTemplates);
	}
	
	//helper function for checkRepeatsForTemplate
	private static void checkRepeatsForTemplate (TreeElement repeatTreeNode, TreeReference ref, FormInstance instance, Vector missing) {
		String name = repeatTreeNode.getName();
		int mult = (repeatTreeNode.repeatable ? TreeReference.INDEX_TEMPLATE : 0);
		ref = ref.extendRef(name, mult);
		
		if (repeatTreeNode.repeatable) {
			TreeElement template = instance.resolveReference(ref);
			if (template == null) {
				missing.addElement(ref);
			}
		}
			
		for (int i = 0; i < repeatTreeNode.getNumChildren(); i++) {
			checkRepeatsForTemplate(repeatTreeNode.getChildAt(i), ref, instance, missing);
		}
	}
	
	//iterates through instance and removes template nodes that are not valid. a template is invalid if:
	//  it is declared for a node that is not repeatable
	//  it is for a repeat that is a child of another repeat and is not located within the parent's template node
	private static void removeInvalidTemplates (FormInstance instance, FormInstance repeatTree) {
		removeInvalidTemplates(instance.getRoot(), (repeatTree == null ? null : repeatTree.getRoot()), true);
	}
	
	//helper function for removeInvalidTemplates
	private static boolean removeInvalidTemplates (TreeElement instanceNode, TreeElement repeatTreeNode, boolean templateAllowed) {
		int mult = instanceNode.getMult();
		boolean repeatable = (repeatTreeNode == null ? false : repeatTreeNode.repeatable);
		
		if (mult == TreeReference.INDEX_TEMPLATE) {
			if (!templateAllowed) {
				System.out.println("Warning: template nodes for sub-repeats must be located within the template node of the parent repeat; ignoring template... [" + instanceNode.getName() + "]");
				return true;
			} else if (!repeatable) {
				System.out.println("Warning: template node found for ref that is not repeatable; ignoring... [" + instanceNode.getName() + "]");
				return true;
			}
		}
		
		if (repeatable && mult != TreeReference.INDEX_TEMPLATE)
			templateAllowed = false;
		
		for (int i = 0; i < instanceNode.getNumChildren(); i++) {
			TreeElement child = instanceNode.getChildAt(i);
			TreeElement rchild = (repeatTreeNode == null ? null : repeatTreeNode.getChild(child.getName(), 0));
			
			if (removeInvalidTemplates(child, rchild, templateAllowed)) {
				instanceNode.removeChildAt(i);
				i--;
			}
		}
		return false;
	}
	
	//if repeatables have no template node, duplicate first as template
	private static void createMissingTemplates (FormInstance instance, Vector missingTemplates) {
		//it is VERY important that the missing template refs are listed in depth-first or breadth-first order... namely, that
		//every ref is listed after a ref that could be its parent. checkRepeatsForTemplate currently behaves this way
		for (int i = 0; i < missingTemplates.size(); i++) {
			TreeReference templRef = (TreeReference)missingTemplates.elementAt(i);
			TreeReference firstMatch;
			
			//make template ref generic and choose first matching node
			TreeReference ref = templRef.clone();
			for (int j = 0; j < ref.size(); j++) {
				ref.setMultiplicity(j, TreeReference.INDEX_UNBOUND);
			}
			Vector nodes = instance.expandReference(ref);
			if (nodes.size() == 0) {
				//binding error; not a single node matches the repeat binding; will be reported later
				continue;
			} else {
				firstMatch = (TreeReference)nodes.elementAt(0);
			}
			
			try {
				instance.copyNode(firstMatch, templRef);
			} catch (InvalidReferenceException e) {
				System.out.println("WARNING! Could not create a default repeat template; this is almost certainly a homogeneity error! Your form will not work! (Failed on " + templRef.toString() + ")");
				e.printStackTrace();
			}
			trimRepeatChildren(instance.resolveReference(templRef)); 
		}
	}
	
	//trim repeatable children of newly created template nodes; we trim because the templates are supposed to be devoid of 'data',
	//  and # of repeats for a given repeat node is a kind of data. trust me
	private static void trimRepeatChildren (TreeElement node) {
		for (int i = 0; i < node.getNumChildren(); i++) {
			TreeElement child = node.getChildAt(i);
			if (child.repeatable) {
				node.removeChildAt(i);
				i--;
			} else {
				trimRepeatChildren(child);
			}
		}
	}
	
	private static void checkDuplicateNodesAreRepeatable (TreeElement node) {
		int mult = node.getMult();
		if (mult > 0) { //repeated node
			if (!node.repeatable) {
				System.out.println("Warning: repeated nodes [" + node.getName() + "] detected that have no repeat binding in the form; DO NOT bind questions to these nodes or their children!");
				//we could do a more comprehensive safety check in the future
			}
		}
		
		for (int i = 0; i < node.getNumChildren(); i++) {
			checkDuplicateNodesAreRepeatable(node.getChildAt(i));
		}
	}
	
	//check repeat sets for homogeneity
	private static void checkHomogeneity (FormInstance instance) {
		Vector refs = getRepeatableRefs();
		for (int i = 0; i < refs.size(); i++) {
			TreeReference ref = (TreeReference)refs.elementAt(i);
			TreeElement template = null;
			Vector nodes = instance.expandReference(ref);
			for (int j = 0; j < nodes.size(); j++) {
				TreeReference nref = (TreeReference)nodes.elementAt(j);
				TreeElement node = instance.resolveReference(nref); 
				if (node == null) //don't crash on '/'... invalid repeat binding will be caught later
					continue;
				
				if (template == null)
					template = instance.getTemplate(nref);
				
				if (!FormInstance.isHomogeneous(template, node)) {
					System.out.println("WARNING! Not all repeated nodes for a given repeat binding [" + nref.toString() + "] are homogeneous! This will cause serious problems!");
				}
			}
		}
	}
	
	private static void verifyBindings (FormDef f, FormInstance instance) {
		//check <bind>s (can't bind to '/', bound nodes actually exist)
		for (int i = 0; i < bindings.size(); i++) {
			DataBinding bind = (DataBinding)bindings.elementAt(i);
			TreeReference ref = FormInstance.unpackReference(bind.getReference());
			
			if (ref.size() == 0) {
				System.out.println("Cannot bind to '/'; ignoring bind...");
				bindings.removeElementAt(i);
				i--;
			} else {
				Vector nodes = instance.expandReference(ref, true);
				if (nodes.size() == 0) {
					System.out.println("WARNING: Bind [" + ref.toString() + "] matches no nodes; ignoring bind...");
				}
			}
		}
				
		//check <repeat>s (can't bind to '/' or '/data')
		Vector refs = getRepeatableRefs();
		for (int i = 0; i < refs.size(); i++) {
			TreeReference ref = (TreeReference)refs.elementAt(i);
			
			if (ref.size() <= 1) {
				throw new XFormParseException("Cannot bind repeat to '/' or '/" + instanceNode.getName() + "'");
			}
		}

		//check control/group/repeat bindings (bound nodes exist, question can't bind to '/')
		verifyControlBindings(f, instance);
		
		//check that repeat members bind to the proper scope (not above the binding of the parent repeat, and not within any sub-repeat (or outside repeat))
		verifyRepeatMemberBindings(f, instance, null);
		
		//check that label/copy/value refs are children of nodeset ref, and exist
		verifyItemsetBindings(instance);
		
		verifyItemsetSrcDstCompatibility(instance);
	}
	
	private static void verifyControlBindings (IFormElement fe, FormInstance instance) {
		if (fe.getChildren() == null)
			return;
		
		for (int i = 0; i < fe.getChildren().size(); i++) {
			IFormElement child = (IFormElement)fe.getChildren().elementAt(i);
			IDataReference ref = null;
			String type = null;
			
			if (child instanceof GroupDef) {
				ref = ((GroupDef)child).getBind();
				type = (((GroupDef)child).getRepeat() ? "Repeat" : "Group");
			} else if (child instanceof QuestionDef) {
				ref = ((QuestionDef)child).getBind();
				type = "Control";
			}
			TreeReference tref = FormInstance.unpackReference(ref);

			if (child instanceof QuestionDef && tref.size() == 0) {
				System.out.println("Warning! Cannot bind control to '/'"); //group can bind to '/'; repeat can't, but that's checked above
			} else {
				Vector nodes = instance.expandReference(tref, true);
				if (nodes.size() == 0) {
					System.out.println("Warning: " + type + " binding [" + tref.toString() + "] matches no nodes");
				}
				//we can't check whether questions map to the right kind of node ('data' node vs. 'sub-tree' node) as that depends
				//on the question's data type, which we don't know yet
			}
			
			verifyControlBindings(child, instance);
		}
	}
	
	private static void verifyRepeatMemberBindings (IFormElement fe, FormInstance instance, GroupDef parentRepeat) {
		if (fe.getChildren() == null)
			return;
		
		for (int i = 0; i < fe.getChildren().size(); i++) {
			IFormElement child = (IFormElement)fe.getChildren().elementAt(i);
			boolean isRepeat = (child instanceof GroupDef && ((GroupDef)child).getRepeat());
			
			//get bindings of current node and nearest enclosing repeat
			TreeReference repeatBind = (parentRepeat == null ? TreeReference.rootRef() : FormInstance.unpackReference(parentRepeat.getBind()));
			TreeReference childBind = FormInstance.unpackReference(child.getBind());
			
			//check if current binding is within scope of repeat binding
			if (!repeatBind.isParentOf(childBind, false)) {
				//catch <repeat nodeset="/a/b"><input ref="/a/c" /></repeat>: repeat question is not a child of the repeated node
				throw new XFormParseException("<repeat> member's binding [" + childBind.toString() + "] is not a descendant of <repeat> binding [" + repeatBind.toString() + "]!");
			} else if (repeatBind.equals(childBind) && isRepeat) {
				//catch <repeat nodeset="/a/b"><repeat nodeset="/a/b">...</repeat></repeat> (<repeat nodeset="/a/b"><input ref="/a/b" /></repeat> is ok)
				throw new XFormParseException("child <repeat>s [" + childBind.toString() + "] cannot bind to the same node as their parent <repeat>; only questions/groups can");
			}
			
			//check that, in the instance, current node is not within the scope of any closer repeat binding
			//build a list of all the node's instance ancestors
			Vector repeatAncestry = new Vector();
			TreeElement repeatNode = (repeatTree == null ? null : repeatTree.getRoot());
			if (repeatNode != null) {
				repeatAncestry.addElement(repeatNode);			
				for (int j = 1; j < childBind.size(); j++) {
					repeatNode = repeatNode.getChild(childBind.getName(j), 0);
					if (repeatNode != null) {
						repeatAncestry.addElement(repeatNode);
					} else {
						break;
					}
				}
			}
			//check that no nodes between the parent repeat and the target are repeatable
			for (int k = repeatBind.size(); k < childBind.size(); k++) {
				TreeElement rChild = (k < repeatAncestry.size() ? (TreeElement)repeatAncestry.elementAt(k) : null);
				boolean repeatable = (rChild == null ? false : rChild.repeatable);
				if (repeatable && !(k == childBind.size() - 1 && isRepeat)) {
					//catch <repeat nodeset="/a/b"><input ref="/a/b/c/d" /></repeat>...<repeat nodeset="/a/b/c">...</repeat>:
					//  question's/group's/repeat's most immediate repeat parent in the instance is not its most immediate repeat parent in the form def
					throw new XFormParseException("<repeat> member's binding [" + childBind.toString() + "] is within the scope of a <repeat> that is not its closest containing <repeat>!");
				}
			}

			verifyRepeatMemberBindings(child, instance, (isRepeat ? (GroupDef)child : parentRepeat));
		}
	}
	
	private static void verifyItemsetBindings (FormInstance instance) {
		for (int i = 0; i < itemsets.size(); i++) {
			ItemsetBinding itemset = (ItemsetBinding)itemsets.elementAt(i);
			
			//check proper parent/child relationship
			if (!itemset.nodesetRef.isParentOf(itemset.labelRef, false)) {
				throw new XFormParseException("itemset nodeset ref is not a parent of label ref");
			} else if (itemset.copyRef != null && !itemset.nodesetRef.isParentOf(itemset.copyRef, false)) {
				throw new XFormParseException("itemset nodeset ref is not a parent of copy ref");
			} else if (itemset.valueRef != null && !itemset.nodesetRef.isParentOf(itemset.valueRef, false)) {
				throw new XFormParseException("itemset nodeset ref is not a parent of value ref");
			}

			//check label/value/copy nodes exist
			if (instance.getTemplatePath(itemset.labelRef) == null) {
				throw new XFormParseException("<label> node for itemset doesn't exist! [" + itemset.labelRef + "]");
			} else if (itemset.copyRef != null && instance.getTemplatePath(itemset.copyRef) == null) {
				throw new XFormParseException("<copy> node for itemset doesn't exist! [" + itemset.copyRef + "]");
			} else if (itemset.valueRef != null && instance.getTemplatePath(itemset.valueRef) == null) {
				throw new XFormParseException("<value> node for itemset doesn't exist! [" + itemset.valueRef + "]");
			}
		}
	}
	
	private static void verifyItemsetSrcDstCompatibility (FormInstance instance) {
		for (int i = 0; i < itemsets.size(); i++) {
			ItemsetBinding itemset = (ItemsetBinding)itemsets.elementAt(i);

			boolean destRepeatable = (instance.getTemplate(itemset.getDestRef()) != null);
			if (itemset.copyMode) {
				if (!destRepeatable) {
					throw new XFormParseException("itemset copies to node(s) which are not repeatable");
				}
				
				//validate homogeneity between src and dst nodes
				TreeElement srcNode = instance.getTemplatePath(itemset.copyRef);
				TreeElement dstNode = instance.getTemplate(itemset.getDestRef());
				
				if (!FormInstance.isHomogeneous(srcNode, dstNode)) {
					System.out.println("WARNING! Source [" + srcNode.getRef().toString() + "] and dest [" + dstNode.getRef().toString() +
							"] of itemset appear to be incompatible!");
				}
				
				//TODO: i feel like, in theory, i should additionally check that the repeatable children of src and dst
				//match up (Achild is repeatable <--> Bchild is repeatable). isHomogeneous doesn't check this. but i'm
				//hard-pressed to think of scenarios where this would actually cause problems
			} else {
				if (destRepeatable) {
					throw new XFormParseException("itemset sets value on repeatable nodes");
				}
			}
		}
	}
	
	private static void applyInstanceProperties (FormInstance instance) {
		for (int i = 0; i < bindings.size(); i++) {
			DataBinding bind = (DataBinding)bindings.elementAt(i);
			TreeReference ref = FormInstance.unpackReference(bind.getReference());
			Vector nodes = instance.expandReference(ref, true);
			
			if (nodes.size() > 0) {
				attachBindGeneral(bind);
			}
			for (int j = 0; j < nodes.size(); j++) {
				TreeReference nref = (TreeReference)nodes.elementAt(j);
				attachBind(instance.resolveReference(nref), bind);	
			}				
		}
		
		applyControlProperties(instance);
	}	
		
	private static void attachBindGeneral (DataBinding bind) {
		TreeReference ref = FormInstance.unpackReference(bind.getReference());
		
		if (bind.relevancyCondition != null) {
			bind.relevancyCondition.addTarget(ref);
		}
		if (bind.requiredCondition != null) {
			bind.requiredCondition.addTarget(ref);
		}
		if (bind.readonlyCondition != null) {
			bind.readonlyCondition.addTarget(ref);
		}
		if (bind.calculate != null) {
			bind.calculate.addTarget(ref);
		}
	}
	
	private static void attachBind(TreeElement node, DataBinding bind) {
		node.dataType = bind.getDataType();
			
		if (bind.relevancyCondition == null) {
			node.setRelevant(bind.relevantAbsolute);
		}
		if (bind.requiredCondition == null) {
			node.setRequired(bind.requiredAbsolute);
		}	
		if (bind.readonlyCondition == null) {
			node.setEnabled(!bind.readonlyAbsolute);
		}
		if (bind.constraint != null) {
			node.setConstraint( new Constraint(bind.constraint, bind.constraintMessage));
		}
			
		node.setPreloadHandler( bind.getPreload());
		node.setPreloadParams(bind.getPreloadParams());
	}
	
	//apply properties to instance nodes that are determined by controls bound to those nodes
	//this should make you feel slightly dirty, but it allows us to be somewhat forgiving with the form
	//(e.g., a select question bound to a 'text' type node) 
	private static void applyControlProperties (FormInstance instance) {
		for (int h = 0; h < 2; h++) {
			Vector selectRefs = (h == 0 ? selectOnes : selectMultis);
			int type = (h == 0 ? Constants.DATATYPE_CHOICE : Constants.DATATYPE_CHOICE_LIST);

			for (int i = 0; i < selectRefs.size(); i++) {
				TreeReference ref = (TreeReference)selectRefs.elementAt(i);
				Vector nodes = instance.expandReference(ref, true);
				for (int j = 0; j < nodes.size(); j++) {
					TreeElement node = instance.resolveReference((TreeReference)nodes.elementAt(j));
					if (node.dataType == Constants.DATATYPE_CHOICE || node.dataType == Constants.DATATYPE_CHOICE_LIST) {
						//do nothing
					} else if (node.dataType == Constants.DATATYPE_NULL || node.dataType == Constants.DATATYPE_TEXT) {
						node.dataType = type;
					} else {
						System.out.println("Warning! Type incompatible with select question node [" + ref.toString() + "] detected!");
					}
				}
			}
		}
	}
	
	//TODO: hook here for turning sub-trees into complex IAnswerData objects (like for immunizations)
	//FIXME: the 'ref' and FormDef parameters (along with the helper function above that initializes them) are only needed so that we
	//can fetch QuestionDefs bound to the given node, as the QuestionDef reference is needed to properly represent answers
	//to select questions. obviously, we want to fix this.
	private static void loadInstanceData (Element node, TreeElement cur, FormDef f) {		
		int numChildren = node.getChildCount();		
		boolean hasElements = false;
		for (int i = 0; i < numChildren; i++) {
			if (node.getType(i) == Node.ELEMENT)
				hasElements = true;
		}

		if (hasElements) {
			Hashtable multiplicities = new Hashtable(); //stores max multiplicity seen for a given node name thus far
			for (int i = 0; i < numChildren; i++) {
				if (node.getType(i) == Node.ELEMENT) {
					Element child = node.getElement(i);
					
					String name = child.getName();
					int index;
					boolean isTemplate = (child.getAttributeValue(NAMESPACE_JAVAROSA, "template") != null);
					
					if (isTemplate) {
						index = TreeReference.INDEX_TEMPLATE;
					} else {
						//update multiplicity counter
						Integer mult = (Integer)multiplicities.get(name);
						index = (mult == null ? 0 : mult.intValue() + 1);
						multiplicities.put(name, new Integer(index));
					}
					
					loadInstanceData(child, cur.getChild(name, index), f);
				}
			}	
		} else {
			String text = getXMLText(node, false);
			if (text != null && text.trim().length() > 0) { //ignore text that is only whitespace
				//TODO: custom data types? modelPrototypes?
				
				cur.setValue(XFormAnswerDataParser.getAnswerData(text, cur.dataType, ghettoGetQuestionDef(cur.dataType, f, cur.getRef())));
			}
		}		
	}
	
	//find a questiondef that binds to ref, if the data type is a 'select' question type
	public static QuestionDef ghettoGetQuestionDef (int dataType, FormDef f, TreeReference ref) {
		if (dataType == Constants.DATATYPE_CHOICE || dataType == Constants.DATATYPE_CHOICE_LIST) {
			return FormDef.findQuestionByRef(ref, f);
		} else {
			return null;
		}
	}
	
	private static void checkDependencyCycles (FormDef f) {
		Vector vertices = new Vector();
		Vector edges = new Vector();
		
		//build graph
		for (Enumeration e = f.triggerIndex.keys(); e.hasMoreElements(); ) {
			TreeReference trigger = (TreeReference)e.nextElement();
			if (!vertices.contains(trigger))
				vertices.addElement(trigger);
			
			Vector triggered = (Vector)f.triggerIndex.get(trigger);
			Vector targets = new Vector();
			for (int i = 0; i < triggered.size(); i++) {
				Triggerable t = (Triggerable)triggered.elementAt(i);
				for (int j = 0; j < t.getTargets().size(); j++) {
					TreeReference target = (TreeReference)t.getTargets().elementAt(j);
					if (!targets.contains(target))
						targets.addElement(target);
				}
			}
			
			for (int i = 0; i < targets.size(); i++) {
				TreeReference target = (TreeReference)targets.elementAt(i);
				if (!vertices.contains(target))
					vertices.addElement(target);
				
				TreeReference[] edge = {trigger, target};
				edges.addElement(edge);
			}
		}
		
		//find cycles
		boolean acyclic = true;
		while (vertices.size() > 0) {
			//determine leaf nodes
			Vector leaves = new Vector();
			for (int i = 0; i < vertices.size(); i++) {
				leaves.addElement(vertices.elementAt(i));
			}
			for (int i = 0; i < edges.size(); i++) {
				TreeReference[] edge = (TreeReference[])edges.elementAt(i);
				leaves.removeElement(edge[0]);
			}
			
			//if no leaf nodes while graph still has nodes, graph has cycles
			if (leaves.size() == 0) {
				acyclic = false;
				break;
			}

			//remove leaf nodes and edges pointing to them
			for (int i = 0; i < leaves.size(); i++) {
				TreeReference leaf = (TreeReference)leaves.elementAt(i);
				vertices.removeElement(leaf);
			}			
			for (int i = edges.size() - 1; i >= 0; i--) {
				TreeReference[] edge = (TreeReference[])edges.elementAt(i);
				if (leaves.contains(edge[1]))
					edges.removeElementAt(i);
			}
		}
		
		if (!acyclic) {
			System.err.println("XPath Dependency Cycle:");
			for (int i = 0; i < edges.size(); i++) {
				TreeReference[] edge = (TreeReference[])edges.elementAt(i);
				System.err.println(edge[0].toString() + " => " + edge[1].toString());
			}
			throw new RuntimeException("Dependency cycles amongst the xpath expressions in relevant/calculate");
		}
	}
	
	//this is terrible
	//not only do we have to re-parse the entire formdef, but it is not guaranteed that you can drop in a submitted instance
	//back into its original form def and it will still parse. in particular, non-relevant nodes will be missing, which will
	//really confuse the binding verifier and repeat homogeneity checker.
	public static FormInstance parseDataModelGhettoooooo (InputStream instanceXMLStream, InputStream formDefXMLStream, String locale) {
		Document formDefXML = getXMLDocument(new InputStreamReader(formDefXMLStream));
		Document instanceXML = getXMLDocument(new InputStreamReader(instanceXMLStream));

		//copied from getFromDef
		FormDef formDef = new FormDef();
		
		initBindHandlers();
		initStateVars();

		parseElement(formDef, formDefXML.getRootElement(), formDef, topLevelHandlers);
		collapseRepeatGroups(formDef);
		
		instanceNode = instanceXML.getRootElement(); //replace default form instance with our new instance
		parseInstance(formDef, instanceNode);

		initStateVars();

		if (locale != null) {
			formDef.getLocalizer().setToDefault();
		} else {
			formDef.getLocalizer().setLocale(locale);
		}
		
		return formDef.getInstance();
	}
		
	//returns data type corresponding to type string; doesn't handle defaulting to 'text' if type unrecognized/unknown
	private static int getDataType(String type) {
		int dataType = Constants.DATATYPE_NULL;
		
		if (type != null) {
			//cheap out and ignore namespace
			if (type.indexOf(":") != -1) {
				type = type.substring(type.indexOf(":") + 1);
			}
			
			if (typeMappings.containsKey(type)) {
				dataType = ((Integer)typeMappings.get(type)).intValue();				
			} else {
				dataType = Constants.DATATYPE_UNSUPPORTED;
				//#if debug.output==verbose
				System.err.println("XForm Parse WARNING: unrecognized data type [" + type + "]");
				//#endif
			}
		}
		
		return dataType;
	}

	public static void addModelPrototype(int type, TreeElement element) {
		modelPrototypes.addNewPrototype(String.valueOf(type), element.getClass());
	}

	public static void addDataType (String type, int dataType) {
		typeMappings.put(type, new Integer(dataType));
	}
	public static void registerControlType(String type, final int typeId) {
		IElementHandler newHandler = new IElementHandler () {
			public void handle (FormDef f, Element e, Object parent) { parseControl((IFormElement)parent, e, f, typeId); } };
		topLevelHandlers.put(type, newHandler);
		groupLevelHandlers.put(type, newHandler);
	}

	public static void registerHandler(String type, IElementHandler handler) {
		topLevelHandlers.put(type, handler);
		groupLevelHandlers.put(type, handler);
	}

	public static void registerBindHandler(IXFormBindHandler handler) {
		bindHandlers.addElement(handler);
	}

	public static String getXMLText (Node n, boolean trim) {
		return (n.getChildCount() == 0 ? null : getXMLText(n, 0, trim));
	}

	/**
	* reads all subsequent text nodes and returns the combined string
	* needed because escape sequences are parsed into consecutive text nodes
	* e.g. "abc&amp;123" --> (abc)(&)(123)
	**/
	public static String getXMLText (Node node, int i, boolean trim) {
		StringBuffer strBuff = null;

		String text = node.getText(i);
		if (text == null)
			return null;

		for (i++; i < node.getChildCount() && node.getType(i) == Node.TEXT; i++) {
			if (strBuff == null)
				strBuff = new StringBuffer(text);

			strBuff.append(node.getText(i));
		}
		if (strBuff != null)
			text = strBuff.toString();

		if (trim)
			text = text.trim();

		return text;
	}
	
	public static FormInstance restoreDataModel (byte[] data, Class restorableType) {
		Restorable r = (restorableType != null ? (Restorable)PrototypeFactory.getInstance(restorableType) : null);
		
		Document doc = getXMLDocument(new InputStreamReader(new ByteArrayInputStream(data)));
		if (doc == null) {
			throw new RuntimeException("syntax error in XML instance; could not parse");
		}
		
		Element e = doc.getRootElement();
		
		TreeElement te = buildInstanceStructure(e, null);
		FormInstance dm = new FormInstance(te);
		loadNamespaces(e, dm);
		if (r != null) {
			RestoreUtils.templateData(r, dm, null);
		}
		loadInstanceData(e, te, null);
		
		return dm;
	}
	
	public static String getVagueLocation(Element e) {
		String path = e.getName();
		Element walker = e;
		while(walker != null) {
			Node n = walker.getParent();
			if(n instanceof Element) {
				walker = (Element)n;
				String step = walker.getName();
				for(int i = 0; i <  walker.getAttributeCount() ; ++i) {
					step += "[@" +walker.getAttributeName(i) + "=";
					step += walker.getAttributeValue(i) + "]";
				}
				path = step + "/" + path;
			} else {
				walker = null;
				path = "/" + path;
			}
		}
		
		String elementString = getVagueElementPrintout(e, 2);
		
		String fullmsg = "\n    Problem found at nodeset: " + path;
		fullmsg += "\n    With element " + elementString + "\n";
		return fullmsg;
	}
	
	public static String getVagueElementPrintout(Element e, int maxDepth) {
		String elementString = "<" + e.getName();
		for(int i = 0; i <  e.getAttributeCount() ; ++i) {
			elementString += " " + e.getAttributeName(i) + "=\"";
			elementString += e.getAttributeValue(i) + "\"";
		}
		if(e.getChildCount() > 0) {
			elementString += ">";
			if(e.getType(0) ==Element.ELEMENT) {
				if(maxDepth > 0) {
					elementString += getVagueElementPrintout((Element)e.getChild(0),maxDepth -1);
				} else {
					elementString += "...";
				}
			}
		} else {
			elementString += "/>";
		}
		return elementString;
	}
}