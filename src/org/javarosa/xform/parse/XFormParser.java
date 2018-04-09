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

import org.javarosa.core.model.Action;
import org.javarosa.core.model.DataBinding;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.ItemsetBinding;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.RangeQuestion;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.SubmissionProfile;
import org.javarosa.core.model.actions.SetValueAction;
import org.javarosa.core.model.instance.AbstractTreeElement;
import org.javarosa.core.model.instance.DataInstance;
import org.javarosa.core.model.instance.ExternalDataInstance;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.model.instance.utils.IAnswerResolver;
import org.javarosa.core.model.osm.OSMTag;
import org.javarosa.core.model.osm.OSMTagItem;
import org.javarosa.core.model.util.restorable.Restorable;
import org.javarosa.core.model.util.restorable.RestoreUtils;
import org.javarosa.core.services.locale.Localizer;
import org.javarosa.core.services.locale.TableLocaleSource;
import org.javarosa.core.util.CacheTable;
import org.javarosa.core.util.StopWatch;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.core.util.externalizable.PrototypeFactoryDeprecated;
import org.javarosa.model.xform.XPathReference;
import org.javarosa.xform.util.InterningKXmlParser;
import org.javarosa.xform.util.XFormAnswerDataParser;
import org.javarosa.xform.util.XFormSerializer;
import org.javarosa.xform.util.XFormUtils;
import org.javarosa.xml.util.InvalidStructureException;
import org.javarosa.xml.util.UnfullfilledRequirementsException;
import org.javarosa.xpath.XPathConditional;
import org.javarosa.xpath.XPathParseTool;
import org.javarosa.xpath.expr.XPathPathExpr;
import org.javarosa.xpath.parser.XPathSyntaxException;
import org.kxml2.io.KXmlParser;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static org.javarosa.core.model.Constants.*;
import static org.javarosa.core.model.instance.ExternalDataInstance.getPathIfExternalDataInstance;
import static org.javarosa.core.services.ProgramFlow.die;
import static org.javarosa.xform.parse.Constants.*;
import static org.javarosa.xform.parse.RangeParser.populateQuestionWithRangeAttributes;


/* droos: i think we need to start storing the contents of the <bind>s in the formdef again */

/**
 * Provides conversion from xform to epihandy object model and vice vasa.
 *
 * @author Daniel Kayiwa
 * @author Drew Roos
 *
 */
public class XFormParser implements IXFormParserFunctions {
    private static final Logger logger = LoggerFactory.getLogger(XFormParser.class);

    public static final String NAMESPACE_JAVAROSA = "http://openrosa.org/javarosa";

    //Constants to clean up code and prevent user error
    private static final String FORM_ATTR = "form";
    private static final String APPEARANCE_ATTR = "appearance";
    private static final String LABEL_ELEMENT = "label";
    private static final String VALUE = "value";
    private static final String ITEXT_CLOSE = "')";
    private static final String ITEXT_OPEN = "jr:itext('";
    private static final String DYNAMIC_ITEXT_CLOSE = ")";
    private static final String DYNAMIC_ITEXT_OPEN = "jr:itext(";
    private static final String BIND_ATTR = "bind";
    private static final String REF_ATTR = "ref";

    private static final String NAMESPACE_HTML = "http://www.w3.org/1999/xhtml";

    private static final int CONTAINER_GROUP = 1;
    private static final int CONTAINER_REPEAT = 2;

    private static HashMap<String, IElementHandler> topLevelHandlers;
    private static HashMap<String, IElementHandler> groupLevelHandlers;
    private static final Map<String, Integer> typeMappings = TypeMappings.getMap();
    private static PrototypeFactoryDeprecated modelPrototypes;
    private static List<SubmissionParser> submissionParsers;

    private Reader _reader;
    private Document _xmldoc;
    private FormDef _f;

    private Reader _instReader;
    private Document _instDoc;

    private boolean modelFound;
    private Localizer localizer;
    private HashMap<String, DataBinding> bindingsByID;
    private List<DataBinding> bindings;
    private List<TreeReference> actionTargets;
    private List<TreeReference> repeats;
    private List<ItemsetBinding> itemsets;
    private List<TreeReference> selectOnes;
    private List<TreeReference> multipleItems;
    private Element mainInstanceNode; //top-level data node of the instance; saved off so it can be processed after the <bind>s
    private List<Element> instanceNodes;
    private List<String> instanceNodeIdStrs;
    private List<String> itextKnownForms;
    private List<String> namedActions;
    private HashMap<String, IElementHandler> structuredActions;

    private final List<WarningCallback> warningCallbacks = new ArrayList<>();
    private final List<ErrorCallback> errorCallbacks = new ArrayList<>();

    //incremented to provide unique question ID for each question
    private int serialQuestionID = 1;

    private static IAnswerResolver answerResolver;

    public static IAnswerResolver getAnswerResolver() {
        return answerResolver;
    }

    public static void setAnswerResolver(IAnswerResolver answerResolver) {
        XFormParser.answerResolver = answerResolver;
    }

    static {
        try {
            staticInit();
        } catch (Exception e) {
            die("xfparser-static-init", e);
        }
    }

    private static void staticInit() {
        initProcessingRules();
        modelPrototypes = new PrototypeFactoryDeprecated();
        submissionParsers = new ArrayList<>(1);
    }

    private static void initProcessingRules() {
        groupLevelHandlers = new HashMap<String, IElementHandler>() {{
            put("input", new IElementHandler() {
                @Override public void handle(XFormParser p, Element e, Object parent) {
                    // Attributes that are passed through to additionalAttributes but shouldn't lead to warnings.
                    // These are consistently used by clients but are expected in additionalAttributes for historical
                    // reasons.
                    List<String> passedThroughInputAtts = unmodifiableList(asList("rows", "query"));
                    p.parseControl((IFormElement) parent, e, CONTROL_INPUT, passedThroughInputAtts, passedThroughInputAtts);
                }
            });
            put("range", new IElementHandler() {
                @Override public void handle(XFormParser p, Element e, Object parent) {
                    p.parseControl((IFormElement) parent, e, CONTROL_RANGE,
                        asList("start", "end", "step") // Prevent warning about unexpected attributes
                    );
                }
            });
            put("secret", new IElementHandler() {
                @Override public void handle(XFormParser p, Element e, Object parent) {
                    p.parseControl((IFormElement) parent, e, CONTROL_SECRET);
                }
            });
            put(SELECT, new IElementHandler() {
                @Override public void handle(XFormParser p, Element e, Object parent) {
                    p.parseControl((IFormElement) parent, e, CONTROL_SELECT_MULTI);
                }
            });
            put(RANK, new IElementHandler() {
                @Override public void handle(XFormParser p, Element e, Object parent) {
                    p.parseControl((IFormElement) parent, e, CONTROL_RANK);
                }
            });
            put(SELECTONE, new IElementHandler() {
                @Override public void handle(XFormParser p, Element e, Object parent) {
                    p.parseControl((IFormElement) parent, e, CONTROL_SELECT_ONE);
                }
            });
            put("group", new IElementHandler() {
                @Override public void handle(XFormParser p, Element e, Object parent) {
                    p.parseGroup((IFormElement) parent, e, CONTAINER_GROUP);
                }
            });
            put("repeat", new IElementHandler() {
                @Override public void handle(XFormParser p, Element e, Object parent) {
                    p.parseGroup((IFormElement) parent, e, CONTAINER_REPEAT);
                }
            });
            put("trigger", new IElementHandler() {
                @Override public void handle(XFormParser p, Element e, Object parent) {
                    p.parseControl((IFormElement) parent, e, CONTROL_TRIGGER);
                }
            }); //multi-purpose now; need to dig deeper
            put(XFTAG_UPLOAD, new IElementHandler() {
                @Override public void handle(XFormParser p, Element e, Object parent) {
                    p.parseUpload((IFormElement) parent, e, CONTROL_UPLOAD);
                }
            });
            put(LABEL_ELEMENT, new IElementHandler() {
                @Override public void handle(XFormParser p, Element e, Object parent) {
                    if (parent instanceof GroupDef) {
                        p.parseGroupLabel((GroupDef) parent, e);
                    } else throw new XFormParseException("parent of element is not a group", e);
                }
            });
        }};

        topLevelHandlers = new HashMap<String, IElementHandler>() {{
            put("model", new IElementHandler() {
                @Override public void handle(XFormParser p, Element e, Object parent) {
                    p.parseModel(e);
                }
            });
            put("title", new IElementHandler() {
                @Override public void handle(XFormParser p, Element e, Object parent) {
                    p.parseTitle(e);
                }
            });
            put("meta", new IElementHandler() {
                @Override public void handle(XFormParser p, Element e, Object parent) {
                    p.parseMeta(e);
                }
            });
        }};
        topLevelHandlers.putAll(groupLevelHandlers);
    }

    private void initState () {
        modelFound = false;
        bindingsByID = new HashMap<>();
        bindings = new ArrayList<>();
        actionTargets = new ArrayList<>();
        repeats = new ArrayList<>();
        itemsets = new ArrayList<>();
        selectOnes = new ArrayList<>();
        multipleItems = new ArrayList<>();
        mainInstanceNode = null;
        instanceNodes = new ArrayList<>();
        instanceNodeIdStrs = new ArrayList<>();

        itextKnownForms = new ArrayList<>(4);
        itextKnownForms.add("long");
        itextKnownForms.add("short");
        itextKnownForms.add("image");
        itextKnownForms.add("audio");

        namedActions = new ArrayList<>(6);
        namedActions.add("rebuild");
        namedActions.add("recalculate");
        namedActions.add("revalidate");
        namedActions.add("refresh");
        namedActions.add("setfocus");
        namedActions.add("reset");


        structuredActions = new HashMap<>();
        structuredActions.put("setvalue", new IElementHandler() {
            public void handle (XFormParser p, Element e, Object parent) { p.parseSetValueAction((FormDef)parent, e);}
        });
    }

    CacheTable<String> stringCache;

    public XFormParser(Reader reader) {
        _reader = reader;
    }

    public XFormParser(Document doc) {
        _xmldoc = doc;
    }

    public XFormParser(Reader form, Reader instance) {
        _reader = form;
        _instReader = instance;
    }

    public XFormParser(Document form, Document instance) {
        _xmldoc = form;
        _instDoc = instance;
    }

    public FormDef parse() throws IOException {
        if (_f == null) {
            logger.info("Parsing form...");

            if (_xmldoc == null) {
                _xmldoc = getXMLDocument(_reader, stringCache);
            }

            parseDoc(buildNamespacesMap(_xmldoc.getRootElement()));

            //load in a custom xml instance, if applicable
            if (_instReader != null) {
                loadXmlInstance(_f, _instReader);
            } else if (_instDoc != null) {
                loadXmlInstance(_f, _instDoc);
            }
        }
        return _f;
    }

    /** Extracts the namespaces from the given element and creates a map of URI to prefix */
    private static Map<String, String> buildNamespacesMap(Element el) {
        final Map<String, String> namespacePrefixesByURI = new HashMap<>();

        for (int i = 0; i < el.getNamespaceCount(); i++) {
            namespacePrefixesByURI.put(el.getNamespaceUri(i), el.getNamespacePrefix(i));
        }

        return namespacePrefixesByURI;
    }

    public static Document getXMLDocument(Reader reader) throws IOException  {
        return getXMLDocument(reader, null);
    }

    /**
     * Uses xkml to parse the provided XML content, and then consolidates text elements.
     *
     * @param reader      the XML content provider
     * @param stringCache an optional string cache, whose presence will cause the use of
     *                    {@link InterningKXmlParser} rather than {@link KXmlParser}.
     * @return the parsed document
     * @throws IOException
     * @deprecated The InterningKXmlParser is not used.
     */
    @Deprecated public static Document getXMLDocument(Reader reader, CacheTable<String> stringCache)
        throws IOException {
        final StopWatch ctParse = StopWatch.start();
        Document doc = new Document();

        try{
            KXmlParser parser;

            if(stringCache != null) {
                parser = new InterningKXmlParser(stringCache);
            } else {
                parser = new KXmlParser();
            }

            parser.setInput(reader);
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
            doc.parse(parser);
        }  catch (XmlPullParserException e) {
            String errorMsg = "XML Syntax Error at Line: " + e.getLineNumber() +", Column: "+ e.getColumnNumber()+ "!";
            logger.error(errorMsg, e);
            throw new XFormParseException(errorMsg);
        } catch(IOException e){
            //CTS - 12/09/2012 - Stop swallowing IO Exceptions
            throw e;
        } catch(Exception e){
            logger.error("Unhandled Exception while Parsing XForm", e);
            throw new XFormParseException("Unhandled Exception while Parsing XForm");
        }

        try {
            reader.close();
        } catch (IOException e) {
            logger.error("Error closing reader", e);
        }
        logger.info(ctParse.logLine("Reading XML and parsing with kXML2"));

        StopWatch ctConsolidate = StopWatch.start();
        XmlTextConsolidator.consolidateText(stringCache, doc.getRootElement());
        logger.info(ctConsolidate.logLine("Consolidating text"));

        return doc;
    }

    private void parseDoc(Map<String, String> namespacePrefixesByUri) {
        final StopWatch codeTimer = StopWatch.start();
        _f = new FormDef();

        initState();
        final String defaultNamespace = _xmldoc.getRootElement().getNamespaceUri(null);
        parseElement(_xmldoc.getRootElement(), _f, topLevelHandlers);
        collapseRepeatGroups(_f);

        final FormInstanceParser instanceParser = new FormInstanceParser(_f, defaultNamespace,
            bindings, repeats, itemsets, selectOnes, multipleItems, actionTargets);

        //parse the non-main instance nodes first
        //we assume that the non-main instances won't
        //reference the main node, so we do them first.
        //if this assumption is wrong, well, then we're screwed.
        if (instanceNodes.size() > 1) {
            for (int instanceIndex = 1; instanceIndex < instanceNodes.size(); instanceIndex++) {
                final Element instance = instanceNodes.get(instanceIndex);
                final String instanceId = instanceNodeIdStrs.get(instanceIndex);
                final String ediPath = getPathIfExternalDataInstance(instance.getAttributeValue(null, "src"));

                if (ediPath != null) {
                    try { /* todo implement better error handling */
                        _f.addNonMainInstance(ExternalDataInstance.buildFromPath(ediPath, instanceId));
                    } catch (IOException | UnfullfilledRequirementsException | InvalidStructureException | XmlPullParserException e) {
                        e.printStackTrace();
                    }
                } else {
                    FormInstance fi = instanceParser.parseInstance(instance, false,
                        instanceNodeIdStrs.get(instanceNodes.indexOf(instance)), namespacePrefixesByUri);
                    loadNamespaces(_xmldoc.getRootElement(), fi); // same situation as below
                    loadInstanceData(instance, fi.getRoot(), _f);
                    _f.addNonMainInstance(fi);
                }
            }
        }
        //now parse the main instance
        if(mainInstanceNode != null) {
            FormInstance fi = instanceParser.parseInstance(mainInstanceNode, true,
                instanceNodeIdStrs.get(instanceNodes.indexOf(mainInstanceNode)), namespacePrefixesByUri);
            /*
             Load namespaces definition (map of prefixes -> URIs) into a form instance so later it can be used
             during the form instance serialization (XFormSerializingVisitor#visit). If the map is not present, then
             serializer will provide own prefixes for the namespaces present in the nodes.
             This will lead to inconsistency between prefixes used in the form definition (bindings)
             and prefixes in the form instance after the instance is restored and inserted into the form definition.
             */
            loadNamespaces(_xmldoc.getRootElement(), fi);
            addMainInstanceToFormDef(mainInstanceNode, fi);
        }

        // Clear the caches, as these may not have been initialized
        // entirely correctly during the validation steps.
        Enumeration<DataInstance> e = _f.getNonMainInstances();
        while ( e.hasMoreElements() ) {
            DataInstance instance = e.nextElement();
            final AbstractTreeElement treeElement = instance.getRoot();
            if (treeElement instanceof TreeElement) {
                ((TreeElement) treeElement).clearChildrenCaches();
            }
            treeElement.clearCaches();
        }
        _f.getMainInstance().getRoot().clearChildrenCaches();
        _f.getMainInstance().getRoot().clearCaches();

        logger.info(codeTimer.logLine("Creating FormDef from parsed XML"));
    }

    private final Set<String> validElementNames = unmodifiableSet(new HashSet<>(asList(
        "html",
        "head",
        "body",
        "xform",
        "chooseCaption",
        "addCaption",
        "addEmptyCaption",
        "delCaption",
        "doneCaption",
        "doneEmptyCaption",
        "mainHeader",
        "entryHeader",
        "delHeader"
    )));

    private void parseElement (Element e, Object parent, HashMap<String, IElementHandler> handlers) {
        String name = e.getName();

        IElementHandler eh = handlers.get(name);
        if (eh != null) {
            eh.handle(this, e, parent);
        } else {
            if (!validElementNames.contains(name)) {
                triggerWarning(
                    "Unrecognized element [" + name    + "]. Ignoring and processing children...",
                    getVagueLocation(e));
            }
            for (int i = 0; i < e.getChildCount(); i++) {
                if (e.getType(i) == Element.ELEMENT) {
                    parseElement(e.getElement(i), parent, handlers);
                }
            }
        }
    }

    private void parseTitle (Element e) {
        List<String> usedAtts = new ArrayList<>(); //no attributes parsed in title.
        String title = getXMLText(e, true);
        logger.info("Title: \"" + title + "\"");
        _f.setTitle(title);
        if(_f.getName() == null) {
            //Jan 9, 2009 - ctsims
            //We don't really want to allow for forms without
            //some unique ID, so if a title is available, use
            //that.
            _f.setName(title);
        }


        if(XFormUtils.showUnusedAttributeWarning(e, usedAtts)){
            triggerWarning( XFormUtils.unusedAttWarning(e, usedAtts), getVagueLocation(e));
        }
    }

    private void parseMeta (Element e) {
        List<String> usedAtts = new ArrayList<>();
        int attributes = e.getAttributeCount();
        for(int i = 0 ; i < attributes ; ++i) {
            String name = e.getAttributeName(i);
            String value = e.getAttributeValue(i);
            if("name".equals(name)) {
                _f.setName(value);
            }
        }


        usedAtts.add("name");
        if(XFormUtils.showUnusedAttributeWarning(e, usedAtts)){
            triggerWarning( XFormUtils.unusedAttWarning(e, usedAtts), getVagueLocation(e));
        }
    }

    //for ease of parsing, we assume a model comes before the controls, which isn't necessarily mandated by the xforms spec
    private void parseModel (Element e) {
        List<String> usedAtts = new ArrayList<>(); //no attributes parsed in title.
        List<Element> delayedParseElements = new ArrayList<>();

        if (modelFound) {
            triggerWarning(
                "Multiple models not supported. Ignoring subsequent models.", getVagueLocation(e));
            return;
        }
        modelFound = true;

        if(XFormUtils.showUnusedAttributeWarning(e, usedAtts)){
            triggerWarning( XFormUtils.unusedAttWarning(e, usedAtts), getVagueLocation(e));
        }

        for (int i = 0; i < e.getChildCount(); i++) {

            int type = e.getType(i);
            Element child = (type == Node.ELEMENT ? e.getElement(i) : null);
            String childName = (child != null ? child.getName() : null);

            if ("itext".equals(childName)) {
                parseIText(child);
            } else if ("instance".equals(childName)) {
                //we save parsing the instance node until the end, giving us the information we need about
                //binds and data types and such
                saveInstanceNode(child);
            } else if (BIND_ATTR.equals(childName)) { //<instance> must come before <bind>s
                parseBind(child);
            } else if("submission".equals(childName)) {
                delayedParseElements.add(child);
            } else if(namedActions.contains(childName) || (childName != null && structuredActions.containsKey(childName))) {
                delayedParseElements.add(child);
            } else { //invalid model content
                if (type == Node.ELEMENT) {
                    throw new XFormParseException("Unrecognized top-level tag [" + childName + "] found within <model>",child);
                } else if (type == Node.TEXT && getXMLText(e, i, true).length() != 0) {
                    throw new XFormParseException("Unrecognized text content found within <model>: \"" + getXMLText(e, i, true) + "\"",child == null ? e : child);
                }
            }

            if(child == null || BIND_ATTR.equals(childName) || "itext".equals(childName)) {
                //Clayton Sims - Jun 17, 2009 - This code is used when the stinginess flag
                //is set for the build. It dynamically wipes out old model nodes once they're
                //used. This is sketchy if anything else plans on touching the nodes.
                //This code can be removed once we're pull-parsing
                e.removeChild(i);
                --i;
            }
        }

        //Now parse out the submission/action blocks (we needed the binds to all be set before we could)
        for(Element child : delayedParseElements) {
            String name = child.getName();
            if(name.equals("submission")) {
                parseSubmission(child);
            } else {
                //For now, anything that isn't a submission is an action
                if(namedActions.contains(name)) {
                    parseNamedAction(child);
                } else {
                    structuredActions.get(name).handle(this, child, _f);
                }
            }
        }
    }

    private void parseNamedAction(Element action) {
        //TODO: Anything useful
    }

    private void parseSetValueAction(FormDef form, Element e) {
        String ref = e.getAttributeValue(null, REF_ATTR);
        String bind = e.getAttributeValue(null, BIND_ATTR);

        String event = e.getAttributeValue(null, "event");

        IDataReference dataRef = null;
        boolean refFromBind = false;


        //TODO: There is a _lot_ of duplication of this code, fix that!
        if (bind != null) {
            DataBinding binding = bindingsByID.get(bind);
            if (binding == null) {
                throw new XFormParseException("XForm Parse: invalid binding ID in submit'" + bind + "'", e);
            }
            dataRef = binding.getReference();
            refFromBind = true;
        } else if (ref != null) {
            dataRef = new XPathReference(ref);
        } else {
            throw new XFormParseException("setvalue action with no target!", e);
        }

        if (dataRef != null) {
            if (!refFromBind) {
                dataRef = FormDef.getAbsRef(dataRef, TreeReference.rootRef());
            }
        }

        String valueRef = e.getAttributeValue(null, "value");
        Action action;
        TreeReference treeref = FormInstance.unpackReference(dataRef);

        actionTargets.add(treeref);
        if(valueRef == null) {
            if(e.getChildCount() == 0 || !e.isText(0)) {
                throw new XFormParseException("No 'value' attribute and no inner value set in <setvalue> associated with: " + treeref, e);
            }
            //Set expression
            action = new SetValueAction(treeref, e.getText(0));
        } else {
            try {
                action = new SetValueAction(treeref, XPathParseTool.parseXPath(valueRef));
            } catch (XPathSyntaxException e1) {
                logger.error("Error", e1);
                throw new XFormParseException("Invalid XPath in value set action declaration: '" + valueRef + "'", e);
            }
        }
        form.registerEventListener(event, action);

    }

    private void parseSubmission(Element submission) {
        String id = submission.getAttributeValue(null, ID_ATTR);

        //These two are always required
        String method = submission.getAttributeValue(null, "method");
        String action = submission.getAttributeValue(null, "action");

        SubmissionParser parser = new SubmissionParser();
        for(SubmissionParser p : submissionParsers) {
            if(p.matchesCustomMethod(method)) {
                parser = p;
            }
        }

        //These two might exist, but if neither do, we just assume you want the entire instance.
        String ref = submission.getAttributeValue(null, REF_ATTR);
        String bind = submission.getAttributeValue(null, BIND_ATTR);

        IDataReference dataRef = null;
        boolean refFromBind = false;

        if (bind != null) {
            DataBinding binding = bindingsByID.get(bind);
            if (binding == null) {
                throw new XFormParseException("XForm Parse: invalid binding ID in submit'" + bind + "'", submission);
            }
            dataRef = binding.getReference();
            refFromBind = true;
        } else if (ref != null) {
            dataRef = new XPathReference(ref);
        } else {
            //no reference! No big deal, assume we want the root reference
            dataRef = new XPathReference("/");
        }

        if (dataRef != null) {
            if (!refFromBind) {
                dataRef = FormDef.getAbsRef(dataRef, TreeReference.rootRef());
            }
        }

        SubmissionProfile profile = parser.parseSubmission(method, action, dataRef, submission );

        if(id == null) {
            //default submission profile
            _f.setDefaultSubmission(profile);
        } else {
            //typed submission profile
            _f.addSubmissionProfile(id, profile);
        }
    }

    private void saveInstanceNode (Element instance) {
        Element instanceNode = null;
        String instanceId = instance.getAttributeValue("", "id");

        for (int i = 0; i < instance.getChildCount(); i++) {
            if (instance.getType(i) == Node.ELEMENT) {
                if (instanceNode != null) {
                    throw new XFormParseException("XForm Parse: <instance> has more than one child element", instance);
                } else {
                    instanceNode = instance.getElement(i);
                }
            }
        }

        if(instanceNode == null) {
            //no kids
            instanceNode = instance;
        }

        if (mainInstanceNode == null) {
            mainInstanceNode = instanceNode;
        }

        instanceNodes.add(instanceNode);
        instanceNodeIdStrs.add(instanceId);
    }

    private void processAdditionalAttributes(QuestionDef question, Element e, List<String> usedAtts, List<String> passedThroughAtts) {
        // save all the unused attributes verbatim...
        for (int i = 0; i < e.getAttributeCount(); i++) {
            String name = e.getAttributeName(i);
            if (!usedAtts.contains(name) || passedThroughAtts != null && passedThroughAtts.contains(name)) {
                question.setAdditionalAttribute(e.getAttributeNamespace(i), name, e.getAttributeValue(i));
            }
        }

        if (XFormUtils.showUnusedAttributeWarning(e, usedAtts)) {
            triggerWarning(XFormUtils.unusedAttWarning(e, usedAtts), getVagueLocation(e));
        }
    }

    protected QuestionDef parseUpload(IFormElement parent, Element e, int controlUpload) {
        // get media type value
        String mediaType = e.getAttributeValue(null, "mediatype");
        // parse the control
        QuestionDef question = parseControl(parent, e, controlUpload, asList("mediatype"));

        // apply the media type value to the returned question def.
        if ("image/*".equals(mediaType)) {
            // NOTE: this could be further expanded.
            question.setControlType(CONTROL_IMAGE_CHOOSE);
        } else if("audio/*".equals(mediaType)) {
            question.setControlType(CONTROL_AUDIO_CAPTURE);
        } else if ("video/*".equals(mediaType)) {
            question.setControlType(CONTROL_VIDEO_CAPTURE);
        } else if ("osm/*".equals(mediaType)) {
            question.setControlType(CONTROL_OSM_CAPTURE);
            List<OSMTag> tags = parseOsmTags(e);
            question.setOsmTags(tags);
        } else {
            // everything else.
            // Presumably, the appearance attribute would govern how this is handled.
            question.setControlType(CONTROL_FILE_CAPTURE);
        }
        return question;
    }

    /**
     *  Parses the OSM Tag Elements when we are parsing
     *  an OSM Upload element. 
     */
    private List<OSMTag> parseOsmTags(Element e) {
        List<OSMTag> tags = new ArrayList<>();
        int childCount = e.getChildCount();
        for (int i = 0; i < childCount; ++i) {
            Object child = e.getChild(i);
            if (child instanceof Element) {
                Element childEl = (Element) child;
                String name = childEl.getName();

                // the child elements we are interested in are tags
                if (name.equals("tag")) {
                    OSMTag tag = new OSMTag();
                    tags.add(tag);
                    // parse tag key
                    int attrCount = childEl.getAttributeCount();
                    for (int j = 0; j < attrCount; ++j) {
                        String attrName = childEl.getAttributeName(j);
                        if (attrName.equals("key")) {
                            tag.key = childEl.getAttributeValue(j);

                            // parse tag children
                            int tagChildCount = childEl.getChildCount();
                            for (int k = 0; k < tagChildCount; ++k) {
                                Object child2 = childEl.getChild(k);
                                if (child2 instanceof Element) {
                                    Element tagChildEl = (Element) child2;
                                    String tagChildName = tagChildEl.getName();

                                    // a tag child might be a label
                                    if (tagChildName.equals("label")) {
                                        tag.label = tagChildEl.getText(0);
                                    }

                                    // a tag child might be an item
                                    else if (tagChildName.equals("item")) {
                                        OSMTagItem item = new OSMTagItem();
                                        tag.items.add(item);

                                        // parse item children
                                        int itemChildCount = tagChildEl.getChildCount();
                                        for (int l = 0; l < itemChildCount; ++l) {
                                            Object child3 = tagChildEl.getChild(l);
                                            if (child3 instanceof Element) {
                                                Element itemChildEl = (Element) child3;
                                                String itemChildName = itemChildEl.getName();

                                                // an item child might be a label
                                                if (itemChildName.equals("label")) {
                                                    item.label = itemChildEl.getText(0);
                                                }

                                                // an item child might be a value
                                                else if (itemChildName.equals("value")) {
                                                    item.value = itemChildEl.getText(0);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return tags;
    }

    private QuestionDef parseControl(IFormElement parent, Element e, int controlType) {
        return parseControl(parent, e, controlType, null, null);
    }

    private QuestionDef parseControl(IFormElement parent, Element e, int controlType, List<String> additionalUsedAtts) {
        return parseControl(parent, e, controlType, additionalUsedAtts, null);
    }

    /**
     * Parses a form control element into a {@link org.javarosa.core.model.QuestionDef} and attaches it to its parent.
     *
     * @param parent                the form control element's parent
     * @param e                     the form control element to parse
     * @param controlType           one of the control types defined in {@link org.javarosa.core.model.Constants}
     * @param additionalUsedAtts    attributes specific to the control type
     * @param passedThroughAtts     attributes specific to the control type that should be passed through to
     *                              additionalAttributes for historical reasons
     * @return                      a {@link org.javarosa.core.model.QuestionDef} representing the form control element
     */
    private QuestionDef parseControl(IFormElement parent, Element e, int controlType, List<String> additionalUsedAtts,
                                     List<String> passedThroughAtts) {
        final QuestionDef question = questionForControlType(controlType);
        question.setID(serialQuestionID++); //until we come up with a better scheme

        final List<String> usedAtts = new ArrayList<>(asList(REF_ATTR, BIND_ATTR, APPEARANCE_ATTR));
        if (additionalUsedAtts != null) {
            usedAtts.addAll(additionalUsedAtts);
        }

        IDataReference dataRef = null;
        boolean refFromBind = false;

        String ref = e.getAttributeValue(null, REF_ATTR);
        String bind = e.getAttributeValue(null, BIND_ATTR);

        if (bind != null) {
            DataBinding binding = bindingsByID.get(bind);
            if (binding == null) {
                throw new XFormParseException("XForm Parse: invalid binding ID '" + bind + "'", e);
            }
            dataRef = binding.getReference();
            refFromBind = true;
        } else if (ref != null) {
            try {
                dataRef = new XPathReference(ref);
            } catch(RuntimeException el) {
                logger.info(XFormParser.getVagueLocation(e));
                throw el;
            }
        } else {
            //noinspection StatementWithEmptyBody
            if (controlType == CONTROL_TRIGGER) {
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

            if (controlType == CONTROL_SELECT_ONE) {
                selectOnes.add((TreeReference) dataRef.getReference());
            } else if (controlType == CONTROL_SELECT_MULTI || controlType == CONTROL_RANK) {
                multipleItems.add((TreeReference) dataRef.getReference());
            }
        }

        boolean isItem =
            controlType == CONTROL_SELECT_MULTI
            || controlType == CONTROL_RANK
            || controlType == CONTROL_SELECT_ONE;

        question.setControlType(controlType);
        question.setAppearanceAttr(e.getAttributeValue(null, APPEARANCE_ATTR));

        for (int i = 0; i < e.getChildCount(); i++) {
            int type = e.getType(i);
            Element child = (type == Node.ELEMENT ? e.getElement(i) : null);
            String childName = (child != null ? child.getName() : null);

            if (LABEL_ELEMENT.equals(childName)) {
                parseQuestionLabel(question, child);
            } else if ("hint".equals(childName)) {
                parseHint(question, child);
            } else if (isItem && "item".equals(childName)) {
                parseItem(question, child);
            } else if (isItem && "itemset".equals(childName)) {
                parseItemset(question, child, parent);
            }
        }
        if (isItem) {
            if (question.getNumChoices() > 0 && question.getDynamicChoices() != null) {
                throw new XFormParseException("Select question contains both literal choices and <itemset>");
            } else if (question.getNumChoices() == 0 && question.getDynamicChoices() == null) {
                throw new XFormParseException("Select question has no choices");
            }
        }

        if (question instanceof RangeQuestion) {
            populateQuestionWithRangeAttributes((RangeQuestion) question, e);
        }

        parent.addChild(question);

        processAdditionalAttributes(question, e, usedAtts, passedThroughAtts);

        return question;
    }

    private QuestionDef questionForControlType(int controlType) {
        return controlType == CONTROL_RANGE ? new RangeQuestion() : new QuestionDef();
    }

    private void parseQuestionLabel (QuestionDef q, Element e) {
        String label = getLabel(e);
        String ref = e.getAttributeValue("", REF_ATTR);

        List<String> usedAtts = new ArrayList<>();
        usedAtts.add(REF_ATTR);

        if (ref != null) {
            if (ref.startsWith(ITEXT_OPEN) && ref.endsWith(ITEXT_CLOSE)) {
                String textRef = ref.substring(ITEXT_OPEN.length(), ref.lastIndexOf(ITEXT_CLOSE));

                verifyTextMappings(textRef, "Question <label>", true);
                q.setTextID(textRef);
            } else {
                throw new RuntimeException("malformed ref [" + ref + "] for <label>");
            }
        } else {
            q.setLabelInnerText(label);
        }


        if(XFormUtils.showUnusedAttributeWarning(e, usedAtts)){
            triggerWarning( XFormUtils.unusedAttWarning(e, usedAtts), getVagueLocation(e));
        }
    }

    private void parseGroupLabel (GroupDef g, Element e) {
        if (g.getRepeat())
            return; //ignore child <label>s for <repeat>; the appropriate <label> must be in the wrapping <group>

        List<String> usedAtts = new ArrayList<>();
        usedAtts.add(REF_ATTR);


        String label = getLabel(e);
        String ref = e.getAttributeValue("", REF_ATTR);

        if (ref != null) {
            if (ref.startsWith(ITEXT_OPEN) && ref.endsWith(ITEXT_CLOSE)) {
                String textRef = ref.substring(ITEXT_OPEN.length(), ref.lastIndexOf(ITEXT_CLOSE));

                verifyTextMappings(textRef, "Group <label>", true);
                g.setTextID(textRef);
            } else {
                throw new RuntimeException("malformed ref [" + ref + "] for <label>");
            }
        } else {
            g.setLabelInnerText(label);
        }


        if(XFormUtils.showUnusedAttributeWarning(e, usedAtts)){
            triggerWarning( XFormUtils.unusedAttWarning(e, usedAtts), getVagueLocation(e));
        }
    }

    private String getLabel (Element e){
        if(e.getChildCount() == 0) return null;

        recurseForOutput(e);

        StringBuilder sb = new StringBuilder();
        for(int i = 0; i<e.getChildCount();i++){
            if(e.getType(i)!=Node.TEXT && !(e.getChild(i) instanceof String)){
                Object b = e.getChild(i);
                Element child = (Element)b;

                //If the child is in the HTML namespace, retain it.
                if(NAMESPACE_HTML.equals(child.getNamespace())) {
                    sb.append(XFormSerializer.elementToString(child));
                } else {
                    //Otherwise, ignore it.
                    logger.info("Unrecognized tag inside of text: <{}>. Did you intend to " +
                        "use HTML markup? If so, ensure that the element is defined in " +
                        "the HTML namespace.", child.getName());
                }
            }else{
                sb.append(e.getText(i));
            }
        }

        return sb.toString().trim();
    }

    private void recurseForOutput(Element e){
        if(e.getChildCount() == 0) return;

        for(int i=0;i<e.getChildCount();i++){
            int kidType = e.getType(i);
            if(kidType == Node.TEXT) { continue; }
            if(e.getChild(i) instanceof String) { continue; }
            Element kid = (Element)e.getChild(i);

            //is just text
            if(kidType == Node.ELEMENT && XFormUtils.isOutput(kid)){
                String s = "${"+parseOutput(kid)+"}";
                e.removeChild(i);
                e.addChild(i, Node.TEXT, s);

                //has kids? Recurse through them and swap output tag for parsed version
            }else if(kid.getChildCount() !=0){
                recurseForOutput(kid);
                //is something else
            }
        }
    }

    private String parseOutput (Element e) {
        List<String> usedAtts = new ArrayList<>();
        usedAtts.add(REF_ATTR);
        usedAtts.add(VALUE);

        String xpath = e.getAttributeValue(null, REF_ATTR);
        if (xpath == null) {
            xpath = e.getAttributeValue(null, VALUE);
        }
        if (xpath == null) {
            throw new XFormParseException("XForm Parse: <output> without 'ref' or 'value'",e);
        }

        XPathConditional expr = null;
        try {
            expr = new XPathConditional(xpath);
        } catch (XPathSyntaxException xse) {
            triggerError("Invalid XPath expression in <output> [" + xpath + "]! " + xse.getMessage());
            return "";
        }

        int index = -1;
        if (_f.getOutputFragments().contains(expr)) {
            index = _f.getOutputFragments().indexOf(expr);
        } else {
            index = _f.getOutputFragments().size();
            _f.getOutputFragments().add(expr);
        }

        if(XFormUtils.showUnusedAttributeWarning(e, usedAtts)){
            triggerWarning( XFormUtils.unusedAttWarning(e, usedAtts), getVagueLocation(e));
        }

        return String.valueOf(index);
    }

    private void parseHint (QuestionDef q, Element e) {
        List<String> usedAtts = new ArrayList<>();
        usedAtts.add(REF_ATTR);
        String hint = getXMLText(e, true);
        String hintInnerText = getLabel(e);
        String ref = e.getAttributeValue("", REF_ATTR);

        if (ref != null) {
            if (ref.startsWith(ITEXT_OPEN) && ref.endsWith(ITEXT_CLOSE)) {
                String textRef = ref.substring(ITEXT_OPEN.length(), ref.lastIndexOf(ITEXT_CLOSE));

                verifyTextMappings(textRef, "<hint>", false);
                q.setHelpTextID(textRef);
            } else {
                throw new RuntimeException("malformed ref [" + ref + "] for <hint>");
            }
        } else {
            q.setHelpInnerText(hintInnerText);
            q.setHelpText(hint);
        }

        if(XFormUtils.showUnusedAttributeWarning(e, usedAtts)){
            triggerWarning( XFormUtils.unusedAttWarning(e, usedAtts), getVagueLocation(e));
        }
    }

    private void parseItem (QuestionDef q, Element e) {
        final int MAX_VALUE_LEN = 32;

        //catalogue of used attributes in this method/element
        List<String> usedAtts = new ArrayList<>();
        List<String> labelUA = new ArrayList<>();
        List<String> valueUA = new ArrayList<>();
        labelUA.add(REF_ATTR);
        valueUA.add(FORM_ATTR);

        String labelInnerText = null;
        String textRef = null;
        String value = null;

        for (int i = 0; i < e.getChildCount(); i++) {
            int type = e.getType(i);
            Element child = (type == Node.ELEMENT ? e.getElement(i) : null);
            String childName = (child != null ? child.getName() : null);

            if (LABEL_ELEMENT.equals(childName)) {

                //print attribute warning for child element
                if(XFormUtils.showUnusedAttributeWarning(child, labelUA)){
                    triggerWarning( XFormUtils.unusedAttWarning(child, labelUA), getVagueLocation(child));
                }
                labelInnerText = getLabel(child);
                String ref = child.getAttributeValue("", REF_ATTR);

                if (ref != null) {
                    if (ref.startsWith(ITEXT_OPEN) && ref.endsWith(ITEXT_CLOSE)) {
                        textRef = ref.substring(ITEXT_OPEN.length(), ref.lastIndexOf(ITEXT_CLOSE));

                        verifyTextMappings(textRef, "Item <label>", true);
                    } else {
                        throw new XFormParseException("malformed ref [" + ref + "] for <item>",child);
                    }
                }
            } else if (VALUE.equals(childName)) {
                value = getXMLText(child, true);

                //print attribute warning for child element
                if(XFormUtils.showUnusedAttributeWarning(child, valueUA)){
                    triggerWarning( XFormUtils.unusedAttWarning(child, valueUA), getVagueLocation(child));
                }

                if (value != null)  {
                    if (value.length() > MAX_VALUE_LEN) {
                        triggerWarning(
                            "choice value [" + value + "] is too long; max. suggested length " + MAX_VALUE_LEN + " chars",
                            getVagueLocation(child));
                    }

                    //validate
                    for (int k = 0; k < value.length(); k++) {
                        char c = value.charAt(k);

                        if (" \n\t\f\r\'\"`".indexOf(c) >= 0) {
                            boolean isMultipleItems = q.getControlType() == CONTROL_SELECT_MULTI || q.getControlType() == CONTROL_RANK;
                            String questionType = !isMultipleItems ? SELECTONE :
                                (q.getControlType() == CONTROL_SELECT_MULTI ? SELECT : RANK);
                            triggerWarning(questionType + " question <value>s [" + value + "] " +
                                    (isMultipleItems ? "cannot" : "should not") + " contain spaces, and are recommended not to contain apostraphes/quotation marks",
                                getVagueLocation(child));
                            break;
                        }
                    }
                }
            }
        }

        if (textRef == null && labelInnerText == null) {
            throw new XFormParseException("<item> without proper <label>",e);
        }
        if (value == null) {
            throw new XFormParseException("<item> without proper <value>",e);
        }

        if (textRef != null) {
            q.addSelectChoice(new SelectChoice(textRef, value));
        } else {
            q.addSelectChoice(new SelectChoice(null,labelInnerText, value, false));
        }

        //print unused attribute warning message for parent element
        if(XFormUtils.showUnusedAttributeWarning(e, usedAtts)){
            triggerWarning(XFormUtils.unusedAttWarning(e, usedAtts), getVagueLocation(e));
        }
    }

    private void parseItemset (QuestionDef q, Element e, IFormElement qparent) {
        ItemsetBinding itemset = new ItemsetBinding();

        ////////////////USED FOR PARSER WARNING OUTPUT ONLY
        //catalogue of used attributes in this method/element
        List<String> usedAtts = new ArrayList<>();
        List<String> labelUA = new ArrayList<>(); //for child with name 'label'
        List<String> valueUA = new ArrayList<>(); //for child with name 'value'
        List<String> copyUA = new ArrayList<>(); //for child with name 'copy'
        usedAtts.add(NODESET_ATTR);
        labelUA.add(REF_ATTR);
        valueUA.add(REF_ATTR);
        valueUA.add(FORM_ATTR);
        copyUA.add(REF_ATTR);
        ////////////////////////////////////////////////////

        /*
         * At this point in time, we cannot construct a valid nodesetRef
         *
         * Leave all ...Ref entries as null and test the ...Expr entries for null / non-null values.
         *
         * We will patch this all up in the verifyItemsetBindings() method.
         */
        String nodesetStr = e.getAttributeValue("", NODESET_ATTR);
        if(nodesetStr == null ) throw new RuntimeException("No nodeset attribute in element: ["+e.getName()+"]. This is required. (Element Printout:"+XFormSerializer.elementToString(e)+")");
        XPathPathExpr path = XPathReference.getPathExpr(nodesetStr);
        itemset.nodesetExpr = new XPathConditional(path);
        itemset.contextRef = getFormElementRef(qparent);
        // this is not valid yet...
        itemset.nodesetRef = null;
        // itemset.nodesetRef = FormInstance.unpackReference(getAbsRef(new XPathReference(path.getReference(true)), itemset.contextRef));
        itemset.copyMode = false;

        for (int i = 0; i < e.getChildCount(); i++) {
            int type = e.getType(i);
            Element child = (type == Node.ELEMENT ? e.getElement(i) : null);
            String childName = (child != null ? child.getName() : null);

            if (LABEL_ELEMENT.equals(childName)) {
                String labelXpath = child.getAttributeValue("", REF_ATTR);
                boolean labelItext = false;

                //print unused attribute warning message for child element
                if(XFormUtils.showUnusedAttributeWarning(child, labelUA)){
                    triggerWarning( XFormUtils.unusedAttWarning(child, labelUA), getVagueLocation(child));
                }
                /////////////////////////////////////////////////////////////

                if (labelXpath != null) {
                    if (labelXpath.startsWith(DYNAMIC_ITEXT_OPEN) && labelXpath.endsWith(DYNAMIC_ITEXT_CLOSE)) {
                        labelXpath = labelXpath.substring(DYNAMIC_ITEXT_OPEN.length(), labelXpath.lastIndexOf(DYNAMIC_ITEXT_CLOSE));
                        labelItext = true;
                    }
                } else {
                    throw new XFormParseException("<label> in <itemset> requires 'ref'");
                }

                XPathPathExpr labelPath = XPathReference.getPathExpr(labelXpath);
                itemset.labelRef = null;
                // itemset.labelRef = FormInstance.unpackReference(getAbsRef(new XPathReference(labelPath), itemset.nodesetRef));
                itemset.labelExpr = new XPathConditional(labelPath);
                itemset.labelIsItext = labelItext;
            } else if ("copy".equals(childName)) {
                String copyXpath = child.getAttributeValue("", REF_ATTR);

                //print unused attribute warning message for child element
                if(XFormUtils.showUnusedAttributeWarning(child, copyUA)){
                    triggerWarning( XFormUtils.unusedAttWarning(child, copyUA), getVagueLocation(child));
                }

                if (copyXpath == null) {
                    throw new XFormParseException("<copy> in <itemset> requires 'ref'");
                }

                XPathPathExpr copyPath = XPathReference.getPathExpr(copyXpath);
                itemset.copyRef = null;
                // itemset.copyRef = FormInstance.unpackReference(getAbsRef(new XPathReference(copyPath), itemset.nodesetRef));
                itemset.copyExpr = new XPathConditional(copyPath);
                itemset.copyMode = true;
            } else if (VALUE.equals(childName)) {
                String valueXpath = child.getAttributeValue("", REF_ATTR);

                //print unused attribute warning message for child element
                if(XFormUtils.showUnusedAttributeWarning(child, valueUA)){
                    triggerWarning( XFormUtils.unusedAttWarning(child, valueUA), getVagueLocation(child));
                }

                if (valueXpath == null) {
                    throw new XFormParseException("<value> in <itemset> requires 'ref'");
                }

                XPathPathExpr valuePath = XPathReference.getPathExpr(valueXpath);
                itemset.valueRef = null;
                // itemset.valueRef = FormInstance.unpackReference(getAbsRef(new XPathReference(valuePath), itemset.nodesetRef));
                itemset.valueExpr = new XPathConditional(valuePath);
            }
        }

        if (itemset.labelExpr == null) {
            throw new XFormParseException("<itemset> requires <label>");
        } else if (itemset.copyExpr == null && itemset.valueExpr == null) {
            throw new XFormParseException("<itemset> requires <copy> or <value>");
        }

        if (itemset.copyExpr != null) {
            if (itemset.valueExpr == null) {
                triggerWarning( "<itemset>s with <copy> are STRONGLY recommended to have <value> as well; pre-selecting, default answers, and display of answers will not work properly otherwise",getVagueLocation(e));
            }
        }

        itemsets.add(itemset);
        q.setDynamicChoices(itemset);

        //print unused attribute warning message for parent element
        if(XFormUtils.showUnusedAttributeWarning(e, usedAtts)){
            triggerWarning(XFormUtils.unusedAttWarning(e, usedAtts), getVagueLocation(e));
        }

    }

    private void parseGroup (IFormElement parent, Element e, int groupType) {
        GroupDef group = new GroupDef();
        group.setID(serialQuestionID++); //until we come up with a better scheme
        IDataReference dataRef = null;
        boolean refFromBind = false;

        List<String> usedAtts = new ArrayList<>();
        usedAtts.add(REF_ATTR);
        usedAtts.add(NODESET_ATTR);
        usedAtts.add(BIND_ATTR);
        usedAtts.add(APPEARANCE_ATTR);
        usedAtts.add("count");
        usedAtts.add("noAddRemove");

        if (groupType == CONTAINER_REPEAT) {
            group.setRepeat(true);
        }

        String ref = e.getAttributeValue(null, REF_ATTR);
        String nodeset = e.getAttributeValue(null, NODESET_ATTR);
        String bind = e.getAttributeValue(null, BIND_ATTR);
        group.setAppearanceAttr(e.getAttributeValue(null, APPEARANCE_ATTR));

        if (bind != null) {
            DataBinding binding = bindingsByID.get(bind);
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
                } else if (nodeset != null) {
                    dataRef = new XPathReference(nodeset);
                }
                //<group> not required to have a binding so no exception thrown
            }
        }

        if (!refFromBind) {
            dataRef = getAbsRef(dataRef, parent);
        }
        group.setBind(dataRef);

        if (group.getRepeat()) {
            repeats.add((TreeReference) dataRef.getReference());

            String countRef = e.getAttributeValue(NAMESPACE_JAVAROSA, "count");
            if (countRef != null) {
                group.count = getAbsRef(new XPathReference(countRef), parent);
                group.noAddRemove = true;
            } else {
                group.noAddRemove = (e.getAttributeValue(NAMESPACE_JAVAROSA, "noAddRemove") != null);
            }
        }

        for (int i = 0; i < e.getChildCount(); i++) {
            int type = e.getType(i);
            Element child = (type == Node.ELEMENT ? e.getElement(i) : null);
            String childName = (child != null ? child.getName() : null);
            String childNamespace = (child != null ? child.getNamespace() : null);

            if (group.getRepeat() && NAMESPACE_JAVAROSA.equals(childNamespace)) {
                if ("chooseCaption".equals(childName)) {
                    group.chooseCaption = getLabel(child);
                } else if ("addCaption".equals(childName)) {
                    group.addCaption = getLabel(child);
                } else if ("delCaption".equals(childName)) {
                    group.delCaption = getLabel(child);
                } else if ("doneCaption".equals(childName)) {
                    group.doneCaption = getLabel(child);
                } else if ("addEmptyCaption".equals(childName)) {
                    group.addEmptyCaption = getLabel(child);
                } else if ("doneEmptyCaption".equals(childName)) {
                    group.doneEmptyCaption = getLabel(child);
                } else if ("entryHeader".equals(childName)) {
                    group.entryHeader = getLabel(child);
                } else if ("delHeader".equals(childName)) {
                    group.delHeader = getLabel(child);
                } else if ("mainHeader".equals(childName)) {
                    group.mainHeader = getLabel(child);
                }
            }
        }

        //the case of a group wrapping a repeat is cleaned up in a post-processing step (collapseRepeatGroups)

        for (int i = 0; i < e.getChildCount(); i++) {
            if (e.getType(i) == Element.ELEMENT) {
                parseElement(e.getElement(i), group, groupLevelHandlers);
            }
        }

        // save all the unused attributes verbatim...
        for(int i=0;i<e.getAttributeCount();i++){
            String name = e.getAttributeName(i);
            if ( usedAtts.contains(name) ) continue;
            group.setAdditionalAttribute(e.getAttributeNamespace(i), name, e.getAttributeValue(i));
        }

        //print unused attribute warning message for parent element
        if(XFormUtils.showUnusedAttributeWarning(e, usedAtts)){
            triggerWarning( XFormUtils.unusedAttWarning(e, usedAtts), getVagueLocation(e));
        }

        parent.addChild(group);
    }

    private TreeReference getFormElementRef (IFormElement fe) {
        if (fe instanceof FormDef) {
            TreeReference ref = TreeReference.rootRef();
            ref.add(mainInstanceNode.getName(), 0);
            return ref;
        } else {
            return (TreeReference)fe.getBind().getReference();
        }
    }

    @Override
    public IDataReference getAbsRef(IDataReference ref, IFormElement parent) {
        return FormDef.getAbsRef(ref, getFormElementRef(parent));
    }

    /** Collapses groups whose only child is a repeat into a single repeat that uses the label of the wrapping group */
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
                    IFormElement grandchild = group.getChildren().get(0);
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
                        //don't merge binding; repeat will always already have one

                        //replace group with repeat
                        fe.getChildren().set(i, repeat);
                        group = repeat;
                    }
                }

                collapseRepeatGroups(group);
            }
        }
    }

    private void parseIText (Element itext) {
        Localizer l = new Localizer(true, true);

        ArrayList<String> usedAtts = new ArrayList<>(); //used for warning message

        for (int i = 0; i < itext.getChildCount(); i++) {
            Element trans = itext.getElement(i);
            if (trans == null || !trans.getName().equals("translation"))
                continue;

            parseTranslation(l, trans);
        }

        if (l.getAvailableLocales().length == 0)
            throw new XFormParseException("no <translation>s defined",itext);

        if (l.getDefaultLocale() == null)
            l.setDefaultLocale(l.getAvailableLocales()[0]);

        //print unused attribute warning message for parent element
        if(XFormUtils.showUnusedAttributeWarning(itext, usedAtts)){
            triggerWarning( XFormUtils.unusedAttWarning(itext, usedAtts), getVagueLocation(itext));
        }

        localizer = l;
    }

    private void parseTranslation (Localizer l, Element trans) {
        /////for warning message
        List<String> usedAtts = new ArrayList<>();
        usedAtts.add("lang");
        usedAtts.add("default");
        /////////////////////////

        String lang = trans.getAttributeValue("", "lang");
        if (lang == null || lang.length() == 0) {
            throw new XFormParseException("no language specified for <translation>",trans);
        }
        String isDefault = trans.getAttributeValue("", "default");

        if (!l.addAvailableLocale(lang)) {
            throw new XFormParseException("duplicate <translation> for language '" + lang + "'",trans);
        }

        if (isDefault != null) {
            if (l.getDefaultLocale() != null)
                throw new XFormParseException("more than one <translation> set as default",trans);
            l.setDefaultLocale(lang);
        }

        TableLocaleSource source = new TableLocaleSource();
        Collection<Integer> removeIndexes = new HashSet<>();

        for (int j = 0; j < trans.getChildCount(); j++) {
            Element text = trans.getElement(j);
            if (text == null || !text.getName().equals("text")) {
                continue;
            }

            parseTextHandle(source, text);
            //Clayton Sims - Jun 17, 2009 - This code is used when the stinginess flag
            //is set for the build. It dynamically wipes out old model nodes once they're
            //used. This is sketchy if anything else plans on touching the nodes.
            //This code can be removed once we're pull-parsing
            removeIndexes.add(j);
        }
        ElementChildDeleter.delete(trans, removeIndexes);

        //print unused attribute warning message for parent element
        if(XFormUtils.showUnusedAttributeWarning(trans, usedAtts)){
            triggerWarning( XFormUtils.unusedAttWarning(trans, usedAtts), getVagueLocation(trans));
        }

        l.registerLocaleResource(lang, source);
    }

    private void parseTextHandle (TableLocaleSource l, Element text) {
        String id = text.getAttributeValue("", ID_ATTR);

        //used for parser warnings...
        List<String> usedAtts = new ArrayList<>();
        List<String> childUsedAtts = new ArrayList<>();
        usedAtts.add(ID_ATTR);
        usedAtts.add(FORM_ATTR);
        childUsedAtts.add(FORM_ATTR);
        childUsedAtts.add(ID_ATTR);
        //////////

        if (id == null || id.length() == 0) {
            throw new XFormParseException("no id defined for <text>",text);
        }

        for (int k = 0; k < text.getChildCount(); k++) {
            Element value = text.getElement(k);
            if (value == null) continue;
            if(!value.getName().equals(VALUE)){
                throw new XFormParseException("Unrecognized element ["+value.getName()+"] in Itext->translation->text");
            }

            String form = value.getAttributeValue("", FORM_ATTR);
            if (form != null && form.length() == 0) {
                form = null;
            }
            String data = getLabel(value);
            if (data == null) {
                data = "";
            }

            String textID = (form == null ? id : id + ";" + form);  //kind of a hack
            if (l.hasMapping(textID)) {
                throw new XFormParseException("duplicate definition for text ID \"" + id + "\" and form \"" + form + "\""+". Can only have one definition for each text form.",text);
            }
            l.setLocaleMapping(textID, data);

            //print unused attribute warning message for child element
            if(XFormUtils.showUnusedAttributeWarning(value, childUsedAtts)){
                triggerWarning( XFormUtils.unusedAttWarning(value, childUsedAtts), getVagueLocation(value));
            }
        }

        //print unused attribute warning message for parent element
        if(XFormUtils.showUnusedAttributeWarning(text, usedAtts)){
            triggerWarning( XFormUtils.unusedAttWarning(text, usedAtts), getVagueLocation(text));
        }
    }

    private boolean hasITextMapping (String textID, String locale) {
        return localizer.hasMapping(locale == null ? localizer.getDefaultLocale() : locale, textID);
    }

    private void verifyTextMappings (String textID, String type, boolean allowSubforms) {
        String[] locales = localizer.getAvailableLocales();

        for (String locale : locales) {
            //Test whether there is a default translation, or whether there is any special form available.
            if (!(hasITextMapping(textID, locale) || (allowSubforms && hasSpecialFormMapping(textID, locale)))) {
                if (locale.equals(localizer.getDefaultLocale())) {
                    throw new XFormParseException(type + " '" + textID +
                        "': text is not localizable for default locale [" + localizer.getDefaultLocale() + "]!");
                }

                triggerWarning( type + " '" +
                    textID + "': text is not localizable for locale " + locale + ".", null);
            }
        }
    }

    /**
     * Tests whether or not there is any form (default or special) for the provided
     * text id.
     *
     * @return Whether a translation is present for the given textID in the form
     */
    private boolean hasSpecialFormMapping(String textID, String locale) {
        //First check our guesses
        for(String guess : itextKnownForms) {
            if(hasITextMapping(textID + ";" + guess, locale)) {
                return true;
            }
        }
        //Otherwise this sucks and we have to test the keys
        for (String key : localizer.getLocaleData(locale).keySet()) {
            if(key.startsWith(textID + ";")) {
                //A key is found, pull it out, add it to the list of guesses, and return positive
                String textForm = key.substring(key.indexOf(";") + 1, key.length());
                //Kind of a long story how we can end up getting here. It involves the default locale loading values
                //for the other locale, but isn't super good.
                //TODO: Clean up being able to get here
                if(!itextKnownForms.contains(textForm)) {
                    logger.info("adding unexpected special itext form: {} to list of expected forms", textForm);
                    itextKnownForms.add(textForm);
                }
                return true;
            }
        }
        return false;
    }

    private DataBinding processStandardBindAttributes(List<String> usedAtts, List<String> passedThroughAtts, Element element) {
        return new StandardBindAttributesProcessor(typeMappings).
            createBinding(this, _f, usedAtts, passedThroughAtts, element);
    }

    /** Attributes that are read into DataBinding fields **/
    private final List<String> usedAtts = unmodifiableList(asList(
        ID_ATTR,
        NODESET_ATTR,
        "type",
        "relevant",
        "required",
        "readonly",
        "constraint",
        "constraintMsg",
        "calculate",
        "preload",
        "preloadParams",
        "requiredMsg",
        "saveIncomplete"
    ));

    /**
     * Attributes that are passed through to additionalAttrs but shouldn't lead to warnings.
     * These are consistently used by clients but are expected in additionalAttrs for historical reasons.
     **/
    private final List<String> passedThroughAtts = unmodifiableList(asList(
            "requiredMsg",
            "saveIncomplete"
    ));

    private void parseBind(Element element) {
        final DataBinding binding = processStandardBindAttributes(usedAtts, passedThroughAtts, element);

        // Warn of unused attributes of parent element
        if (XFormUtils.showUnusedAttributeWarning(element, usedAtts)) {
            triggerWarning(
                XFormUtils.unusedAttWarning(element, usedAtts), getVagueLocation(element));
        }

        addBinding(binding);
    }

    private void addBinding(DataBinding binding) {
        bindings.add(binding);

        if (binding.getId() != null) {
            if (bindingsByID.put(binding.getId(), binding) != null) {
                throw new XFormParseException("XForm Parse: <bind>s with duplicate ID: '" + binding.getId() + "'");
            }
        }
    }

    /** e is the top-level _data_ node of the instance (immediate (and only) child of <instance>) */
    private void addMainInstanceToFormDef(Element e, FormInstance instanceModel) {
        loadInstanceData(e, instanceModel.getRoot(), _f);

        checkDependencyCycles();
        _f.setInstance(instanceModel);
        _f.setLocalizer(localizer);

        try {
            _f.finalizeTriggerables();
        } catch(IllegalStateException ise) {
            throw new XFormParseException(ise.getMessage() == null ? "Form has an illegal cycle in its calculate and relevancy expressions!" : ise.getMessage());
        }
    }

    static HashMap<String, String> loadNamespaces(Element e, FormInstance tree) {
        HashMap<String, String> prefixes = new HashMap<>();
        for(int i = 0 ; i < e.getNamespaceCount(); ++i ) {
            String uri = e.getNamespaceUri(i);
            String prefix = e.getNamespacePrefix(i);
            if(uri != null && prefix != null) {
                tree.addNamespace(prefix, uri);
            }
        }
        return prefixes;
    }

    public static TreeElement buildInstanceStructure (Element node, TreeElement parent, Map<String,
        String> namespacePrefixesByUri, Integer multiplicityFromGroup) {
        return buildInstanceStructure(node, parent, null, node.getNamespace(), namespacePrefixesByUri, multiplicityFromGroup);
    }

    /**
     * Parses instance hierarchy and turns into a skeleton model; ignoring data content,
     * but respecting repeated nodes and 'template' flags
     *
     * @param node                   the input node
     * @param parent                 the parent
     * @param instanceName           the name of the instance
     * @param docnamespace
     * @param namespacePrefixesByUri namespace prefixes, by URI
     * @param multiplicityFromGroup  if not null, the multiplicity to use. If present, a potentially
     *                               expensive search can be avoided.
     * @return a new TreeElement
     */
    public static TreeElement buildInstanceStructure (Element node, TreeElement parent,
                                                      String instanceName, String docnamespace, Map<String, String> namespacePrefixesByUri,
                                                      Integer multiplicityFromGroup) {
        TreeElement element;

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
            logger.warn("instance node '{}' contains both elements and text as children; text ignored", node.getName());
        }

        //check for repeat templating
        final String name = node.getName();
        final int multiplicity;
        if (isTemplate(node)) {
            multiplicity = TreeReference.INDEX_TEMPLATE;
            if (parent != null && parent.getChild(name, TreeReference.INDEX_TEMPLATE) != null) {
                throw new XFormParseException("More than one node declared as the template for the same repeated set [" + name + "]",node);
            }
        } else {
            multiplicity = multiplicityFromGroup != null ? multiplicityFromGroup :
                (parent == null ? 0 : parent.getChildMultiplicity(name));
        }


        String modelType = node.getAttributeValue(NAMESPACE_JAVAROSA, "modeltype");
        //create node; handle children
        if(modelType == null) {
            element = new TreeElement(name, multiplicity);
            element.setInstanceName(instanceName);
        } else {
            if( typeMappings.get(modelType) == null ){
                throw new XFormParseException("ModelType " + modelType + " is not recognized.",node);
            }
            element = (TreeElement)modelPrototypes.getNewInstance(typeMappings.get(modelType).toString());
            if(element == null) {
                element = new TreeElement(name, multiplicity);
                logger.info("No model type prototype available for {}", modelType);
            } else {
                element.setName(name);
                element.setMult(multiplicity);
            }
        }
        if(node.getNamespace() != null) {
            if(!node.getNamespace().equals(docnamespace)) {
                element.setNamespace(node.getNamespace());
            }
            if (namespacePrefixesByUri.containsKey(node.getNamespace())) {
                element.setNamespacePrefix(namespacePrefixesByUri.get(node.getNamespace()));
            }
        }


        if (hasElements) {
            Integer newMultiplicityFromGroup = childOptimizationsOk(node) ? 0 : null;
            for (int i = 0; i < numChildren; i++) {
                if (node.getType(i) == Node.ELEMENT) {
                    TreeElement newChild = buildInstanceStructure(node.getElement(i), element,
                        instanceName, docnamespace, namespacePrefixesByUri, newMultiplicityFromGroup);
                    element.addChild(newChild);
                    if (newMultiplicityFromGroup != null) {
                        newMultiplicityFromGroup++;
                    }
                }
            }
        }

        //handle attributes
        if (node.getAttributeCount() > 0) {
            for (int i = 0; i < node.getAttributeCount(); i++) {
                String attrNamespace = node.getAttributeNamespace(i);
                String attrName = node.getAttributeName(i);
                if (attrNamespace.equals(NAMESPACE_JAVAROSA) && attrName.equals("template")) {
                    continue;
                }
                if (attrNamespace.equals(NAMESPACE_JAVAROSA) && attrName.equals("recordset")) {
                    continue;
                }

                element.setAttribute(attrNamespace, attrName, node.getAttributeValue(i));
            }
        }

        return element;
    }

    private static boolean isTemplate(Element node) {
        return node.getAttributeValue(NAMESPACE_JAVAROSA, "template") != null;
    }

    /**
     * If all children of {@code parent} are {@link Element}s ({@link Element#getElement} returns non-null),
     * and the names of the children are all the same, and none of the children contain the template
     * attribute, more efficient methods may be used to build the collection of children. This method makes
     * that determination.
     *
     * @param parent the parent whose children are to be examined
     * @return the determination described above
     */
    static boolean childOptimizationsOk(Element parent) {
        if (parent.getChildCount() == 0) {
            return false;
        }
        final Element firstChild = parent.getElement(0);
        if (firstChild == null || isTemplate(firstChild)) {
            return false;
        }
        final String firstName = firstChild.getName();
        for (int i = 1; i < parent.getChildCount(); i++) {
            Element child = parent.getElement(i);
            if (child == null || isTemplate(child) || !child.getName().equals(firstName)) {
                return false;
            }
        }
        return true;
    }

    //TODO: hook here for turning sub-trees into complex IAnswerData objects (like for immunizations)
    //FIXME: the 'ref' and FormDef parameters (along with the helper function above that initializes them) are only needed so that we
    //can fetch QuestionDefs bound to the given node, as the QuestionDef reference is needed to properly represent answers
    //to select questions. obviously, we want to fix this.
    private static void loadInstanceData (Element node, TreeElement cur, FormDef f) {
        int numChildren = node.getChildCount();
        boolean hasElements = false;
        for (int i = 0; i < numChildren; i++) {
            if (node.getType(i) == Node.ELEMENT) {
                hasElements = true;
                break;
            }
        }

        if (hasElements) {
            HashMap<String, Integer> multiplicities = new HashMap<>(); //stores max multiplicity seen for a given node name thus far
            for (int i = 0; i < numChildren; i++) {
                if (node.getType(i) == Node.ELEMENT) {
                    Element child = node.getElement(i);

                    String name = child.getName();
                    int index;
                    boolean isTemplate = isTemplate(child);

                    if (isTemplate) {
                        index = TreeReference.INDEX_TEMPLATE;
                    } else {
                        //update multiplicity counter
                        Integer mult = multiplicities.get(name);
                        index = (mult == null ? 0 : mult + 1);
                        multiplicities.put(name, index);
                    }

                    loadInstanceData(child, cur.getChild(name, index), f);
                }
            }
        } else {
            String text = getXMLText(node, true);
            if (text != null && text.trim().length() > 0) { //ignore text that is only whitespace
                //TODO: custom data types? modelPrototypes?
                cur.setValue(XFormAnswerDataParser.getAnswerData(text, cur.getDataType(), ghettoGetQuestionDef(cur.getDataType(), f, cur.getRef())));
            }
        }
    }

    /** Finds a questiondef that binds to ref, if the data type is a 'select' question type */
    public static QuestionDef ghettoGetQuestionDef (int dataType, FormDef f, TreeReference ref) {
        if (dataType == DATATYPE_CHOICE || dataType == DATATYPE_MULTIPLE_ITEMS) {
            return FormDef.findQuestionByRef(ref, f);
        } else {
            return null;
        }
    }

    private void checkDependencyCycles () {
        _f.reportDependencyCycles();
    }

    private void loadXmlInstance(FormDef f, Reader xmlReader) throws IOException {
        loadXmlInstance(f, getXMLDocument(xmlReader));
    }

    /**
     * Loads a compatible xml instance into FormDef f
     *
     * call before f.initialize()!
     */
    private static void loadXmlInstance(FormDef f, Document xmlInst) {
        TreeElement savedRoot = XFormParser.restoreDataModel(xmlInst, null).getRoot();
        TreeElement templateRoot = f.getMainInstance().getRoot().deepCopy(true);

        // weak check for matching forms
        // TODO: should check that namespaces match?
        if (!savedRoot.getName().equals(templateRoot.getName()) || savedRoot.getMult() != 0) {
            throw new RuntimeException("Saved form instance does not match template form definition");
        }

        // populate the data model
        TreeReference tr = TreeReference.rootRef();
        tr.add(templateRoot.getName(), TreeReference.INDEX_UNBOUND);
        templateRoot.populate(savedRoot, f);

        // populated model to current form
        f.getMainInstance().setRoot(templateRoot);

        // if the new instance is inserted into the formdef before f.initialize() is called, this
        // locale refresh is unnecessary
        //   Localizer loc = f.getLocalizer();
        //   if (loc != null) {
        //       f.localeChanged(loc.getLocale(), loc);
        //     }
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
        StringBuilder strBuff = null;

        String text = node.getText(i);
        if (text == null)
            return null;

        for (i++; i < node.getChildCount() && node.getType(i) == Node.TEXT; i++) {
            if (strBuff == null)
                strBuff = new StringBuilder(text);

            strBuff.append(node.getText(i));
        }
        if (strBuff != null)
            text = strBuff.toString();

        if (trim)
            text = text.trim();

        return text;
    }

    public static FormInstance restoreDataModel (InputStream input, Class restorableType) throws IOException {
        Document doc = getXMLDocument(new InputStreamReader(input, "UTF-8"));
        if (doc == null) {
            throw new RuntimeException("syntax error in XML instance; could not parse");
        }
        return restoreDataModel(doc, restorableType);
    }

    public static FormInstance restoreDataModel (Document doc, Class restorableType) {
        Restorable r = (restorableType != null ? (Restorable)PrototypeFactory.getInstance(restorableType) : null);

        Element e = doc.getRootElement();

        TreeElement te = buildInstanceStructure(e, null, buildNamespacesMap(e), null);
        FormInstance dm = new FormInstance(te);
        loadNamespaces(e, dm);
        if (r != null) {
            RestoreUtils.templateData(r, dm, null);
        }
        loadInstanceData(e, te, null);

        return dm;
    }

    public static FormInstance restoreDataModel (byte[] data, Class restorableType) {
        try {
            return restoreDataModel(new ByteArrayInputStream(data), restorableType);
        } catch (IOException e) {
            logger.error("Error", e);
            throw new XFormParseException("Bad parsing from byte array " + e.getMessage());
        }
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

    private static String getVagueElementPrintout(Element e, int maxDepth) {
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

    void setStringCache(CacheTable<String> stringCache) {
        this.stringCache = stringCache;
    }

    public void onWarning(WarningCallback callback) {
        this.warningCallbacks.add(callback);
    }

    public void onError(ErrorCallback callback) {
        this.errorCallbacks.add(callback);
    }

    private void triggerWarning(String message, String xmlLocation) {
        logger.warn("XForm Parse Warning: {}{}", message, xmlLocation == null ? "" : xmlLocation);
        for (WarningCallback callback : warningCallbacks)
            callback.accept(message, xmlLocation);
    }

    private void triggerError(String message) {
        logger.error("XForm Parse Error: {}", message);
        for (ErrorCallback callback : errorCallbacks)
            callback.accept(message);
    }

    interface WarningCallback {
        void accept(String message, String xmlLocation);
    }

    interface ErrorCallback {
        void accept(String message);
    }
}
