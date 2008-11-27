package org.javarosa.xform.parse;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.DataBinding;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.condition.Condition;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.instance.QuestionDataElement;
import org.javarosa.core.model.instance.QuestionDataGroup;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.utils.Localizer;
import org.javarosa.core.util.externalizable.PrototypeFactoryDeprecated;
import org.javarosa.model.xform.XPathReference;
import org.javarosa.xform.util.IXFormBindHandler;
import org.javarosa.xform.util.XFormAnswerDataParser;
import org.javarosa.xpath.XPathConditional;
import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.parser.XPathSyntaxException;
import org.kxml2.io.KXmlParser;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;
import org.xmlpull.v1.XmlPullParser;


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

	/** IXFormBindHaandler */
	private static Vector bindHandlers;

	/* THIS CLASS IS NOT THREAD-SAFE */
	//state variables -- not a good idea since this class is static, but that's not really a good idea either, now is it
	private static boolean modelFound;
	private static Hashtable bindingsByID;
	private static Hashtable bindingsByRef; //key is the xpath ref string, not the IDataReference object
	private static Element instanceNode;

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
		groupLevelHandlers.put("trigger", trigger);
		groupLevelHandlers.put(Constants.XFTAG_UPLOAD, upload);

		topLevelHandlers = new Hashtable();
		for (Enumeration en = groupLevelHandlers.keys(); en.hasMoreElements(); ) {
			String key = (String)en.nextElement();
			topLevelHandlers.put(key, groupLevelHandlers.get(key));
		}
		topLevelHandlers.put("model", model);
		topLevelHandlers.put("title", title);

		groupLevelHandlers.put("label", groupLabel);
	}

	private static void initTypeMappings () {
		typeMappings = new Hashtable();
		typeMappings.put("xsd:string", new Integer(Constants.DATATYPE_TEXT));
		typeMappings.put("xsd:integer", new Integer(Constants.DATATYPE_INTEGER));
		typeMappings.put("xsd:int", new Integer(Constants.DATATYPE_INTEGER));
		typeMappings.put("xsd:decimal", new Integer(Constants.DATATYPE_DECIMAL));
		typeMappings.put("xsd:double", new Integer(Constants.DATATYPE_DECIMAL));
		typeMappings.put("xsd:float", new Integer(Constants.DATATYPE_DECIMAL));
		typeMappings.put("xsd:dateTime", new Integer(Constants.DATATYPE_DATE_TIME));
		typeMappings.put("xsd:date", new Integer(Constants.DATATYPE_DATE));
		typeMappings.put("xsd:time", new Integer(Constants.DATATYPE_TIME));
		typeMappings.put("xsd:gYear", new Integer(Constants.DATATYPE_UNSUPPORTED));
		typeMappings.put("xsd:gMonth", new Integer(Constants.DATATYPE_UNSUPPORTED));
		typeMappings.put("xsd:gDay", new Integer(Constants.DATATYPE_UNSUPPORTED));
		typeMappings.put("xsd:gYearMonth", new Integer(Constants.DATATYPE_UNSUPPORTED));
		typeMappings.put("xsd:gMonthDay", new Integer(Constants.DATATYPE_UNSUPPORTED));
		typeMappings.put("xsd:boolean", new Integer(Constants.DATATYPE_BOOLEAN));
		typeMappings.put("xsd:base64Binary", new Integer(Constants.DATATYPE_UNSUPPORTED));
		typeMappings.put("xsd:hexBinary", new Integer(Constants.DATATYPE_UNSUPPORTED));
		typeMappings.put("xsd:anyURI", new Integer(Constants.DATATYPE_UNSUPPORTED));
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
		bindingsByRef = new Hashtable();
		instanceNode = null;
	}

	public static FormDef getFormDef(Reader reader) {
		Document doc = getXMLDocument(reader);
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

//		Hashtable id2VarNameMap = new Hashtable();
//		Hashtable relevants = new Hashtable();

		parseElement(formDef, doc.getRootElement(), formDef, topLevelHandlers);

//		addSkipRules(formDef,id2VarNameMap,relevants);

		if(instanceNode != null) {
			parseInstance(formDef, instanceNode);
		}

		verifyBindings(formDef);

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
			//#if debug.output==verbose
			System.err.println("XForm Parse: Unrecognized element [" + name + "]. Ignoring and processing children...");
			//#endif
			for (int i = 0; i < e.getChildCount(); i++) {
				if (e.getType(i) == Element.ELEMENT) {
					parseElement(f, e.getElement(i), parent, handlers);
				}
			}
		}
	}

	private static void parseTitle (FormDef f, Element e) {
		//Removed a line here about the form title not being null. Couldn't possibly think
		//of why that would make sense. CTS - 7/21/2008
		f.setName(getXMLText(e, true));
	}

	//for ease of parsing, we assume a model comes before the controls, which isn't necessarily mandated by the xforms spec
	private static void parseModel (FormDef f, Element e) {
		if (modelFound) {
			//#if debug.output==verbose
			System.err.println("Multiple models not supported. Ignoring subsequent models.");
			//#endif
			return;
		}
		modelFound = true;

		for (int i = 0; i < e.getChildCount(); i++) {
			int type = e.getType(i);
			Element child = (type == Node.ELEMENT ? e.getElement(i) : null);
			String childName = (child != null ? child.getName() : null);

			if ("itext".equals(childName)) {
				parseIText(f, child);
			} else if ("instance".equals(childName)) {
				//we save parsing the instance node until the end, giving us the information we need about
				//binds and data types and such
				instanceNode = child;
			} else if ("bind".equals(childName)) {
				parseBind(f, child);
			} else {
				if (type == Node.ELEMENT || type == Node.TEXT && getXMLText(e, i, true).length() != 0) {
					throw new XFormParseException("Unrecognized context found within <model>");
				}
			}
		}
	}

	private static int serialQuestionID = 1;

	
	protected static QuestionDef parseUpload(IFormElement parent, Element e, FormDef f,
			int controlUpload) {
		QuestionDef question = parseControl(parent, e, f, controlUpload);
		String mediaType = e.getAttributeValue(null, "mediatype");
		if ("image/*".equals(mediaType)) {
			// NOTE: this could be further expanded. 
			question.setControlType(Constants.CONTROL_IMAGE_CHOOSE);
		}
		return question;
	}
	
	protected static QuestionDef parseControl (IFormElement parent, Element e, FormDef f, int controlType) {
		QuestionDef question = new QuestionDef();
		DataBinding binding = null;
		question.setID(serialQuestionID++); //until we come up with a better scheme

		String ref = e.getAttributeValue(null, "ref");
		String bind = e.getAttributeValue(null, "bind");
		
		//for now we assume that all <bind>s and <ref>s specify one and only one node in their nodeset,
		//and that a given data node is referenced by at most one <bind>
		if (bind != null) {
			binding = (DataBinding)bindingsByID.get(bind);
			if (binding == null) {
				throw new XFormParseException("XForm Parse: invalid binding ID '" + bind + "'");
			}
		} else if (ref != null) {
			question.setBind(new XPathReference(ref));
			binding = (DataBinding)bindingsByRef.get(ref);
			//in the future, we may have multiple <bind>s that must be applied in succession
		} else {
			if (controlType != Constants.CONTROL_TRIGGER) {
				throw new XFormParseException("XForm Parse: input control with neither 'ref' nor 'bind'");
			} else {
				question.setDataType(Constants.DATATYPE_NULL);
			}
		}

		if (binding != null) {
			attachBind(f, question, binding);
		}

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
			} else if ((controlType == Constants.CONTROL_SELECT_MULTI ||
					    controlType == Constants.CONTROL_SELECT_ONE) && "item".equals(childName)) {
				parseItem(f, question, child);
			}
			// @czue - do we need to add parsers for filename and mediatype here for upload?
		}

		parent.addChild(question);
		return question;
	}

	private static void parseQuestionLabel (FormDef f, QuestionDef q, Element e) {
		String label = getXMLText(e, true);
		String ref = e.getAttributeValue("", "ref");

		if (ref != null) {
			if (ref.startsWith("jr:itext('") && ref.endsWith("')")) {
				String textRef = ref.substring("jr:itext('".length(), ref.indexOf("')"));

				if (!(hasITextMapping(f, textRef) ||
						(hasITextMapping(f, textRef + ";long") && hasITextMapping(f, textRef + ";short"))))
					throw new XFormParseException("<label> '" + textRef + "': text is not localizable for all locales");
				q.setLongTextID(textRef + ";long", null);
				q.setShortTextID(textRef + ";short", null);
			} else {
				throw new RuntimeException("malformed ref for <label>");
			}
		} else {
			q.setLongText(label);
			q.setShortText(label);
		}
	}

	private static void parseGroupLabel (FormDef f, GroupDef g, Element e) {
		String label = getXMLText(e, true);
		String ref = e.getAttributeValue("", "ref");

		if (ref != null) {
			if (ref.startsWith("jr:itext('") && ref.endsWith("')")) {
				String textRef = ref.substring("jr:itext('".length(), ref.indexOf("')"));

				if (!(hasITextMapping(f, textRef) ||
						(hasITextMapping(f, textRef + ";long") && hasITextMapping(f, textRef + ";short"))))
					throw new XFormParseException("<label> '" + textRef + "': text is not localizable for all locales");
				g.setLongTextID(textRef + ";long", null);
				g.setShortTextID(textRef + ";short", null);
			} else {
				throw new RuntimeException("malformed ref for <label>");
			}
		} else {
			g.setLongText(label);
			g.setShortText(label);
		}
	}

	private static void parseHint (FormDef f, QuestionDef q, Element e) {
		String hint = getXMLText(e, true);
		String ref = e.getAttributeValue("", "ref");

		if (ref != null) {
			if (ref.startsWith("jr:itext('") && ref.endsWith("')")) {
				String textRef = ref.substring("jr:itext('".length(), ref.indexOf("')"));

				if (!hasITextMapping(f, textRef))
					throw new XFormParseException("<hint> text is not localizable for all locales for reference " + textRef);
				q.setHelpTextID(textRef, null);
			} else {
				throw new RuntimeException("malformed ref for <hint>");
			}
		} else {
			q.setHelpText(hint);
		}
	}

	private static void parseItem (FormDef f, QuestionDef q, Element e) {
		String label = null;
		String textRef = null;
		String value = null;

		for (int i = 0; i < e.getChildCount(); i++) {
			int type = e.getType(i);
			Element child = (type == Node.ELEMENT ? e.getElement(i) : null);
			String childName = (child != null ? child.getName() : null);

			if ("label".equals(childName)) {
				label = getXMLText(child, true);
				String ref = child.getAttributeValue("", "ref");

				if (ref != null) {
					if (ref.startsWith("jr:itext('") && ref.endsWith("')")) {
						textRef = ref.substring("jr:itext('".length(), ref.indexOf("')"));

						if (!hasITextMapping(f, textRef))
							throw new XFormParseException("<label> '" + textRef + "': text is not localizable for all locales");
					} else {
						throw new XFormParseException("malformed ref for <item>");
					}
				}
			} else if ("value".equals(childName)) {
				value = getXMLText(child, true);
			}
		}

		if ((textRef == null && label == null) || value == null) {
			throw new XFormParseException("<item> without proper <label> or <value>");
		}

		if (textRef != null) {
			q.addSelectItemID(textRef, true, value);
		} else {
			q.addSelectItemID(label, false, value);
		}
	}

	private static void parseGroup (IFormElement parent, Element e, FormDef f, int groupType) {
		GroupDef group = new GroupDef();

		if (groupType == CONTAINER_REPEAT) {
			group.setRepeat(true);
		}

		//binding

		parseElement(f, e, group, groupLevelHandlers);

		parent.addChild(group);
	}

	/**
	 * KNOWN ISSUES WITH ITEXT
	 *
	 * 'long' and 'short' forms of text are only supported for input control labels at this time. all other
	 * situations (<hint> tags, <label>s within <item>s, etc.) should only reference text handles that have
	 * only the single, default form.
	 */

	private static void parseIText (FormDef f, Element itext) {
		Localizer l = new Localizer(true, true);
		f.setLocalizer(l);
		l.registerLocalizable(f);

		for (int i = 0; i < itext.getChildCount(); i++) {
			Element trans = itext.getElement(i);
			if (trans == null || !trans.getName().equals("translation"))
				continue;

			parseTranslation(l, trans);
		}

		if (l.getAvailableLocales().length == 0)
			throw new XFormParseException("no <translation>s defined");

		if (l.getDefaultLocale() == null)
			l.setDefaultLocale(l.getAvailableLocales()[0]);
	}

	private static void parseTranslation (Localizer l, Element trans) {
		String lang = trans.getAttributeValue("", "lang");
		if (lang == null || lang.length() == 0)
			throw new XFormParseException("no language specified for <translation>");
		String isDefault = trans.getAttributeValue("", "default");

		if (!l.addAvailableLocale(lang))
			throw new XFormParseException("duplicate <translation> for same language");

		if (isDefault != null) {
			if (l.getDefaultLocale() != null)
				throw new XFormParseException("more than one <translation> set as default");
			l.setDefaultLocale(lang);
		}

		for (int j = 0; j < trans.getChildCount(); j++) {
			Element text = trans.getElement(j);
			if (text == null || !text.getName().equals("text"))
				continue;

			parseTextHandle(l, lang, text);
		}

	}

	private static void parseTextHandle (Localizer l, String locale, Element text) {
		String id = text.getAttributeValue("", "id");
		if (id == null || id.length() == 0)
			throw new XFormParseException("no id defined for <text>");

		for (int k = 0; k < text.getChildCount(); k++) {
			Element value = text.getElement(k);
			if (value == null || !value.getName().equals("value"))
				continue;

			String form = value.getAttributeValue("", "form");
			if (form != null && form.length() == 0)
				form = null;
			String data = getXMLText(value, 0, true);
			if (data == null)
				data = "";

			String textID = (form == null ? id : id + ";" + form);  //kind of a hack
			if (l.hasMapping(locale, textID))
				throw new XFormParseException("duplicate definition for text ID and form");
			l.setLocaleMapping(locale, textID, data);
		}
	}

	private static boolean hasITextMapping (FormDef form, String textID) {
		Localizer l = form.getLocalizer();
		return l.hasMapping(l.getDefaultLocale(), textID);
	}

	private static void parseBind (FormDef f, Element e) {
		DataBinding binding  = new DataBinding();

		binding.setId(e.getAttributeValue("", "id"));

		String nodeset = e.getAttributeValue(null, "nodeset");
		if (nodeset == null) {
			throw new XFormParseException("XForm Parse: <bind> without nodeset");
		}
		binding.setReference(new XPathReference(nodeset));

		binding.setDataType(getDataType(e.getAttributeValue(null, "type")));

		//constraints

		String xpathRel = e.getAttributeValue(null, "relevant");
		if (xpathRel != null) {
			if ("true()".equals(xpathRel)) {
				//do nothing
			} else if ("false()".equals(xpathRel)) {
				//#if debug.output==verbose
				System.err.println("Warning: <bind> never relevant. Ignoring relevancy...");
				//#endif
			} else {
				Condition c = buildCondition(xpathRel, "relevant");
				c = f.addCondition(c);
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
				Condition c = buildCondition(xpathReq, "required");
				c = f.addCondition(c);
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
				Condition c = buildCondition(xpathRO, "readonly");
				c = f.addCondition(c);
				binding.readonlyCondition = c;
			}
		}

		binding.setPreload(e.getAttributeValue(NAMESPACE_JAVAROSA, "preload"));
		binding.setPreloadParams(e.getAttributeValue(NAMESPACE_JAVAROSA, "preloadParams"));

		Enumeration en = bindHandlers.elements();
		while(en.hasMoreElements()) {
			IXFormBindHandler handler = (IXFormBindHandler)en.nextElement();
			handler.handle(e, binding);
		}

		addBinding(f, binding);
	}

	private static Condition buildCondition (String xpath, String type) {
		XPathExpression expr;
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

		Condition c = new Condition(cond, trueAction, falseAction);
		return c;
	}

	private static void addBinding (FormDef f, DataBinding binding) {
		f.addBinding(binding);
		addBinding(binding);
	}
	
	private static void addBinding (DataBinding binding) {
		if (binding.getId() != null) {
			if (bindingsByID.put(binding.getId(), binding) != null) {
				throw new XFormParseException("XForm Parse: <bind>s with duplicate ID");
			}
		}
		bindingsByRef.put((String)binding.getReference().getReference(), binding);
	}

	private static void attachBind(FormDef f, QuestionDef q, DataBinding binding) {
		if (q.getBind() == null) {
			q.setBind(binding.getReference());
		}

		q.setDataType(binding.getDataType());
		//constraints?

		if (binding.relevancyCondition != null) {
			binding.relevancyCondition.addAffectedQuestion(q);
		}

		if (binding.requiredCondition != null) {
			binding.requiredCondition.addAffectedQuestion(q);
		} else {
			q.setRequired(binding.requiredAbsolute);
		}

		if (binding.readonlyCondition != null) {
			binding.readonlyCondition.addAffectedQuestion(q);
		} else {
			q.setEnabled(!binding.readonlyAbsolute);
		}
	}

	//will check that all <bind>s and refs refer to nodes that actually exist in the instance
	//not complete
	private static void verifyBindings (FormDef f) {
//		Vector bindings = f.getBindings();
//		for (Enumeration e = bindings.elements(); e.hasMoreElements(); ) {
//			IDataReference ref = ((DataBinding)e.nextElement()).getReference();
//			f.getDataModel().
//
//		}
//
//		f.getBindings();
//
	}

	private static void parseInstance (FormDef f, Element e) {
		Element dataElement = null;
		for (int i = 0; i < e.getChildCount(); i++) {
			if (e.getType(i) == Node.ELEMENT) {
				if (dataElement != null) {
					throw new XFormParseException("XForm Parse: <instance> has more than one child element");
				} else {
					dataElement = e.getElement(i);
				}
			}
		}

		TreeElement root = parseInstanceNodes(f, dataElement, "/").getRoot();
		DataModelTree instanceModel = new DataModelTree(root);
		instanceModel.setName(f.getName());
		f.setDataModel(instanceModel);
	}

	private static TreeElement parseInstanceNodes (FormDef formDef, Element node, String currentPath) {
		int childNum = node.getChildCount();

		TreeElement element = null;
		String refStr = currentPath + node.getName();
		XPathReference reference = new XPathReference(refStr);

		if (bindingsByRef.containsKey(refStr)) {
			DataBinding binding = (DataBinding) bindingsByRef.get(refStr);
			// This node, and its children represent some sort of Question

			element = (TreeElement)modelPrototypes.getNewInstance(String.valueOf(binding.getDataType()));
			if(element != null) {
				element.setReference(reference);
				element.setName(node.getName());
			}
			if (element == null) {
				
				// Nothing special happened when we tried to instantiate. This
				// is just a normal question
				
				element = new QuestionDataElement(node.getName(), reference);
				
				Vector formElements = new Vector();
				formDef.getChild(reference, formElements);
				((QuestionDataElement) element).setValue(XFormAnswerDataParser
						.getAnswerData(formElements, binding, node));	
			}
		}
		if (childNum == 0 && element == null) {
			// This is a Question Data
			element = new QuestionDataElement(node.getName(), reference);
		} else if (childNum == 1) {
			// This is potentially a data element, depending on the child's type
			if (node.getType(0) == Node.TEXT) {
				if (element == null) {
					// Data Element
					element = new QuestionDataElement(node.getName(), reference);
					
					Vector formElements = new Vector();
					formDef.getChild(reference, formElements);
					if(!formElements.isEmpty()){
						((QuestionDataElement)element).setValue(XFormAnswerDataParser.getAnswerData(formElements, null, node));
					} else {
						((QuestionDataElement)element).setValue(new StringData((String)node.getChild(0)));
					}
				}
			} else {
				if(element == null ) {
					element = new QuestionDataGroup(node.getName());
				}
				String newPath = currentPath + node.getName() + "/";
				((QuestionDataGroup) element).addChild(parseInstanceNodes(formDef, node
						.getElement(0), newPath));
			}

		} else {
			// This is a Group of elements, and has no bindings, so its
			// children are responsible for the rest.

			if(element == null) {
				element = new QuestionDataGroup(node.getName());
			}

			for (int i = 0; i < childNum; ++i) {
				if (node.getType(i) != Node.ELEMENT) {
					continue;
				} else {
					String newPath = currentPath + node.getName() + "/";
					((QuestionDataGroup) element).addChild(parseInstanceNodes(formDef, 
							node.getElement(i), newPath));
				}
			}
		}

		// Does it have any attributes??
		if(node.getAttributeCount()>0){
			for(int i=0; i<node.getAttributeCount();i++){
				element.setAttribute(node.getAttributeNamespace(i), node.getAttributeName(i), node.getAttributeValue(i));
				//#if debug.output==verbose
				System.out.println(element.getName()+ " has added ATT: "+element.getAttributeName(i)+"="+element.getAttributeValue(i));
				//#endif
			}
		}
		return element;
	}
	
	public static DataModelTree parseDataModelTree(String xml, FormDef formDef) throws Exception {
		// init variables
		initStateVars();
		
//		// add form def bindings
		DataBinding binding;
		for (int i = 0; i < formDef.getBindings().size(); i++) {
			binding = (DataBinding)formDef.getBindings().elementAt(i);
			addBinding(binding);
		}
		
		// parse xml to DOM
		Reader reader = new InputStreamReader(new ByteArrayInputStream(xml.getBytes("UTF-8")));
		XmlPullParser xmlPullParser = new KXmlParser();
		xmlPullParser.setInput(reader);
				
		Document doc = new Document();
		doc.parse(xmlPullParser);
		
		// parse DOM to model
		Element dataElement = null;
		for (int i = 0; i < doc.getChildCount(); i++) {
			if (doc.getType(i) == Node.ELEMENT) {
				if (dataElement != null) {
					throw new XFormParseException("XForm Parse: <instance> has more than one child element");
				} else {
					dataElement = doc.getElement(i);
				}
			}
		}

		TreeElement root = parseInstanceNodes(formDef, dataElement, "/").getRoot();
		DataModelTree instanceModel = new DataModelTree(root);

		instanceModel.setName(formDef.getName());
		instanceModel.setFormReferenceId(formDef.getRecordId());
		return instanceModel;
	}

	private static int getDataType(String type) {
		int dataType = -1;

		if (type != null && typeMappings.containsKey(type)) {
			dataType = ((Integer)typeMappings.get(type)).intValue();
		}

		if (dataType <= 0) {
			if (type != null) {
				//#if debug.output==verbose
				System.err.println("XForm Parse: unrecognized data type [" + type + "]; default to string");
				//#endif
			}
			dataType = Constants.DATATYPE_TEXT;
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

	//reads all subsequent text nodes and returns the combined string
	//needed because escape sequences are parsed into consecutive text nodes
	//e.g. "abc&amp;123" --> (abc)(&)(123)
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
//	private static void addSkipRules(FormDef formDef, Hashtable map, Hashtable relevants){
//		Vector rules = new Vector();
//		//Rules is vector of strings that contain the "relevant=" thing
//		Enumeration en = relevants.keys();
//		byte ruleId = 0;
//
//		while(en.hasMoreElements()) {
//			String relevant = (String)en.nextElement();
//			DataBinding bind = (DataBinding)relevants.get(relevant);
//			int split = relevant.indexOf("=");
//			if(split != -1) {
//				//TODO: Consolidate these by the relevant element: Many questionss depend on the same condition
//				String relevantQuestionPath = relevant.substring(0, split);
//				String relevantAnswer = relevant.substring(split+1, relevant.length()-1);
//				QuestionDef thetarget = (QuestionDef) formDef.getQuestion(relevantQuestionPath);
//
//				if (thetarget != null) {
//					Vector conditions = new Vector();
//					Vector actionTargets = new Vector();
//
//					Condition condition = new Condition(ruleId, thetarget.getId(), Constants.OPERATOR_EQUAL, relevantAnswer);
//					conditions.addElement(condition);
//					XPathReference reference = (XPathReference)bind.getReference();
//					actionTargets.addElement(reference.getReference());
//
//					SkipRule rule = new SkipRule(ruleId, conditions,
//							Constants.ACTION_ENABLE, actionTargets, relevant);
//
//					rules.addElement(rule);
//					ruleId++;
//					//#if debug.output==verbose
//					System.out.println("New rule added: id: " + ruleId
//							+ " Conditions: " + conditions.toString()
//							+ " actionTargets : " + actionTargets.toString());
//					//#endif
//				}
//			}
//			else {
//				//Is there a form of relevancy that isn't an equality?
//			}
//		}
//
//
//		formDef.setRules(rules);
//	}

//	private static void setDefaultValues(Element dataNode,FormDef formDef,Hashtable id2VarNameMap){
//		String id, val;
//		Enumeration keys = id2VarNameMap.keys();
//		while(keys.hasMoreElements()){
//			id = (String)keys.nextElement();
//			val = getNodeTextValue(dataNode,id);
//			if(val == null || val.trim().length() == 0) //we are not allowing empty strings for now.
//				continue;
//			QuestionDef def = formDef.getQuestion((String)id2VarNameMap.get(id));
//			if(def != null)
//				def.setDefaultValue(val);
//		}
//	}

//	private static String getNodeTextValue(Element dataNode,String name){
//		Element node = getNode(dataNode,name);
//		return getTextValue(node);
//	}
//
//	private static String getTextValue(Element node){
//		int numOfEntries = node.getChildCount();
//		for (int i = 0; i < numOfEntries; i++) {
//			if (node.isText(i))
//				return node.getText(i);
//
//			if(node.getType(i) == Element.ELEMENT){
//				String val = getTextValue(node.getElement(i));
//				if(val != null)
//					return val;
//			}
//		}
//
//		return null;
//	}

//	/**
//	 * Gets a child element of a parent node with a given name.
//	 *
//	 * @param parent - the parent element
//	 * @param name - the name of the child.
//	 * @return - the child element.
//	 */
//	private static Element getNode(Element parent, String name){
//		for(int i=0; i<parent.getChildCount(); i++){
//			if(parent.getType(i) != Element.ELEMENT)
//				continue;
//
//			Element child = (Element)parent.getChild(i);
//			if(child.getName().equals(name))
//				return child;
//
//			child = getNode(child,name);
//			if(child != null)
//				return child;
//		}
//
//		return null;
//	}

}