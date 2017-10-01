package org.javarosa.xform.parse;

import org.javarosa.core.io.Std;
import org.javarosa.core.model.Constants;
import org.javarosa.core.model.DataBinding;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.ItemsetBinding;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.condition.Constraint;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.instance.DataInstance;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.InvalidReferenceException;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.xform.util.XFormUtils;
import org.kxml2.kdom.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.javarosa.xform.parse.XFormParser.buildInstanceStructure;
import static org.javarosa.xform.parse.XFormParser.getVagueLocation;

class FormInstanceParser {
    private final FormDef formDef;
    private final String defaultNamespace;
    private final XFormParserReporter reporter;
    private final List<DataBinding> bindings;
    private final List<TreeReference> repeats;
    private final List<ItemsetBinding> itemsets;
    private final List<TreeReference> selectOnes;
    private final List<TreeReference> selectMultis;
    private final List<TreeReference> actionTargets;
    /** pseudo-data model tree that describes the repeat structure of the instance; useful during instance processing and validation */
    private FormInstance repeatTree;

    FormInstanceParser(FormDef formDef, String defaultNamespace, XFormParserReporter reporter,
                       List<DataBinding> bindings, List<TreeReference> repeats, List<ItemsetBinding> itemsets,
                       List<TreeReference> selectOnes, List<TreeReference> selectMultis, List<TreeReference> actionTargets) {
        // Todo: additional refactoring should shorten this too-long argument list
        this.formDef = formDef;
        this.defaultNamespace = defaultNamespace;
        this.reporter = reporter;
        this.bindings = bindings;
        this.repeats = repeats;
        this.itemsets = itemsets;
        this.selectOnes = selectOnes;
        this.selectMultis = selectMultis;
        this.actionTargets = actionTargets;
    }

    FormInstance parseInstance(Element e, boolean isMainInstance, String name, Map<String, String> namespacePrefixesByUri) {
        TreeElement root = buildInstanceStructure(e, null, !isMainInstance ? name : null, e.getNamespace(),
                namespacePrefixesByUri, null);
        FormInstance instanceModel = new FormInstance(root);
        instanceModel.setName(isMainInstance ? formDef.getTitle() : name);

        final List<String> usedAtts = Collections.unmodifiableList(Arrays.asList("id", "version", "uiVersion", "name"));

        String schema = e.getNamespace();
        if (schema != null && schema.length() > 0 && !schema.equals(defaultNamespace)) {
            instanceModel.schema = schema;
        }
        instanceModel.formVersion = e.getAttributeValue(null, "version");
        instanceModel.uiVersion = e.getAttributeValue(null, "uiVersion");

        XFormParser.loadNamespaces(e, instanceModel);
        if (isMainInstance) {
            // the initialization of the references is done twice.
            // The first time is here because they are needed before these
            // validation steps can be performed.
            // It is then done again during the call to _f.setInstance().
            FormDef.updateItemsetReferences(formDef.getChildren());
            processRepeats(instanceModel);
            verifyBindings(instanceModel, e.getName());
            verifyActions(instanceModel);
        }
        applyInstanceProperties(instanceModel);

        //print unused attribute warning message for parent element
        if (XFormUtils.showUnusedAttributeWarning(e, usedAtts)){
            reporter.warning(XFormParserReporter.TYPE_UNKNOWN_MARKUP, XFormUtils.unusedAttWarning(e, usedAtts), getVagueLocation(e));
        }

        return instanceModel;
    }

    /**
     * Pre-processes and cleans up instance regarding repeats; in particular:
     * 1) flags all repeat-related nodes as repeatable
     * 2) catalogs which repeat template nodes are explicitly defined, and notes which repeats bindings lack templates
     * 3) removes template nodes that are not valid for a repeat binding
     * 4) generates template nodes for repeat bindings that do not have one defined explicitly
     * 5) gives a stern warning for any repeated instance nodes that do not correspond to a repeat binding
     * 6) verifies that all sets of repeated nodes are homogeneous
     */
    private void processRepeats(FormInstance instance) {
        flagRepeatables(instance);
        processTemplates(instance);
        checkDuplicateNodesAreRepeatable(instance.getRoot());
        checkHomogeneity(instance);
    }

    /** Flags all nodes identified by repeat bindings as repeatable */
    private void flagRepeatables(FormInstance instance) {
        for (TreeReference ref : getRepeatableRefs()) {
            for (TreeReference nref : new EvaluationContext(instance).expandReference(ref, true)) {
                TreeElement node = instance.resolveReference(nref);
                if (node != null) { // catch '/'
                    node.setRepeatable(true);
                }
            }
        }
    }

    private void processTemplates (FormInstance instance) {
        repeatTree = buildRepeatTree(getRepeatableRefs(), instance.getRoot().getName());

        List<TreeReference> missingTemplates = new ArrayList<>();
        checkRepeatsForTemplate(instance, repeatTree, missingTemplates);

        removeInvalidTemplates(instance, repeatTree);
        createMissingTemplates(instance, missingTemplates);
    }

    private void verifyBindings(FormInstance instance, String mainInstanceNodeName) {
        //check <bind>s (can't bind to '/', bound nodes actually exist)
        for (int i = 0; i < bindings.size(); i++) {
            DataBinding bind = bindings.get(i);
            TreeReference ref = FormInstance.unpackReference(bind.getReference());

            if (ref.size() == 0) {
                Std.out.println("Cannot bind to '/'; ignoring bind...");
                bindings.remove(i);
                i--;
            } else {
                List<TreeReference> nodes = new EvaluationContext(instance).expandReference(ref, true);
                if (nodes.size() == 0) {
                    reporter.warning(XFormParserReporter.TYPE_ERROR_PRONE, "<bind> defined for a node that doesn't exist [" + ref.toString() + "]. The node's name was probably changed and the bind should be updated. ", null);
                }
            }
        }

        //check <repeat>s (can't bind to '/' or '/data')
        for (TreeReference ref : getRepeatableRefs()) {
            if (ref.size() <= 1) {
                throw new XFormParseException("Cannot bind repeat to '/' or '/" + mainInstanceNodeName + "'");
            }
        }

        //check control/group/repeat bindings (bound nodes exist, question can't bind to '/')
        List<String> bindErrors = new ArrayList<>();
        verifyControlBindings(formDef, instance, bindErrors);
        if (bindErrors.size() > 0) {
            String errorMsg = "";
            for (String bindError : bindErrors) {
                errorMsg += bindError + "\n";
            }
            throw new XFormParseException(errorMsg);
        }

        //check that repeat members bind to the proper scope (not above the binding of the parent repeat, and not within any sub-repeat (or outside repeat))
        verifyRepeatMemberBindings(formDef, instance, null);

        //check that label/copy/value refs are children of nodeset ref, and exist
        verifyItemsetBindings(instance);

        verifyItemsetSrcDstCompatibility(instance);
    }

    private void verifyActions (FormInstance instance) {
        //check the target of actions which are manipulating real values
        for (TreeReference target : actionTargets) {
            List<TreeReference> nodes = new EvaluationContext(instance).expandReference(target, true);
            if (nodes.size() == 0) {
                throw new XFormParseException("Invalid Action - Targets non-existent node: " + target.toString(true));
            }
        }
    }

    private static void checkDuplicateNodesAreRepeatable (TreeElement node) {
        int mult = node.getMult();
        if (mult > 0) { //repeated node
            if (!node.isRepeatable()) {
                Std.out.println("Warning: repeated nodes [" + node.getName() + "] detected that have no repeat binding in the form; DO NOT bind questions to these nodes or their children!");
                //we could do a more comprehensive safety check in the future
            }
        }

        for (int i = 0; i < node.getNumChildren(); i++) {
            checkDuplicateNodesAreRepeatable(node.getChildAt(i));
        }
    }

    /** Checks repeat sets for homogeneity */
    private void checkHomogeneity (FormInstance instance) {
        for (TreeReference ref : getRepeatableRefs()) {
            TreeElement template = null;
            for (TreeReference nref : new EvaluationContext(instance).expandReference(ref)) {
                TreeElement node = instance.resolveReference(nref);
                if (node == null) //don't crash on '/'... invalid repeat binding will be caught later
                    continue;

                if (template == null)
                    template = instance.getTemplate(nref);

                if (!FormInstance.isHomogeneous(template, node)) {
                    reporter.warning(XFormParserReporter.TYPE_INVALID_STRUCTURE, "Not all repeated nodes for a given repeat binding [" + nref.toString() + "] are homogeneous! This will cause serious problems!", null);
                }
            }
        }
    }

    private void verifyControlBindings (IFormElement fe, FormInstance instance, List<String> errors) { //throws XmlPullParserException {
        if (fe.getChildren() == null)
            return;

        for (int i = 0; i < fe.getChildren().size(); i++) {
            IFormElement child = fe.getChildren().get(i);
            IDataReference ref = null;
            String type = null;

            if (child instanceof GroupDef) {
                ref = child.getBind();
                type = (((GroupDef)child).getRepeat() ? "Repeat" : "Group");
            } else if (child instanceof QuestionDef) {
                ref = child.getBind();
                type = "Question";
            }
            TreeReference tref = FormInstance.unpackReference(ref);

            if (child instanceof QuestionDef && tref.size() == 0) {
                //group can bind to '/'; repeat can't, but that's checked above
                reporter.warning(XFormParserReporter.TYPE_INVALID_STRUCTURE, "Cannot bind control to '/'",null);
            } else {
                List<TreeReference> nodes = new EvaluationContext(instance).expandReference(tref, true);
                if (nodes.size() == 0) {
                    String error = type+ " bound to non-existent node: [" + tref.toString() + "]";
                    reporter.error(error);
                    errors.add(error);
                }
                //we can't check whether questions map to the right kind of node ('data' node vs. 'sub-tree' node) as that depends
                //on the question's data type, which we don't know yet
            }

            verifyControlBindings(child, instance, errors);
        }
    }

    private void verifyRepeatMemberBindings (IFormElement fe, FormInstance instance, GroupDef parentRepeat) {
        if (fe.getChildren() == null)
            return;

        for (int i = 0; i < fe.getChildren().size(); i++) {
            IFormElement child = fe.getChildren().get(i);
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
            List<TreeElement> repeatAncestry = new ArrayList<>();
            TreeElement repeatNode = (repeatTree == null ? null : repeatTree.getRoot());
            if (repeatNode != null) {
                repeatAncestry.add(repeatNode);
                for (int j = 1; j < childBind.size(); j++) {
                    repeatNode = repeatNode.getChild(childBind.getName(j), 0);
                    if (repeatNode != null) {
                        repeatAncestry.add(repeatNode);
                    } else {
                        break;
                    }
                }
            }
            //check that no nodes between the parent repeat and the target are repeatable
            for (int k = repeatBind.size(); k < childBind.size(); k++) {
                TreeElement rChild = (k < repeatAncestry.size() ? repeatAncestry.get(k) : null);
                boolean repeatable = rChild != null && rChild.isRepeatable();
                if (repeatable && !(k == childBind.size() - 1 && isRepeat)) {
                    //catch <repeat nodeset="/a/b"><input ref="/a/b/c/d" /></repeat>...<repeat nodeset="/a/b/c">...</repeat>:
                    //  question's/group's/repeat's most immediate repeat parent in the instance is not its most immediate repeat parent in the form def
                    throw new XFormParseException("<repeat> member's binding [" + childBind.toString() + "] is within the scope of a <repeat> that is not its closest containing <repeat>!");
                }
            }

            verifyRepeatMemberBindings(child, instance, (isRepeat ? (GroupDef)child : parentRepeat));
        }
    }

    private void verifyItemsetBindings (FormInstance instance) {
        for (ItemsetBinding itemset : itemsets) {
            //check proper parent/child relationship
            if (!itemset.nodesetRef.isParentOf(itemset.labelRef, false)) {
                throw new XFormParseException("itemset nodeset ref is not a parent of label ref");
            } else if (itemset.copyRef != null && !itemset.nodesetRef.isParentOf(itemset.copyRef, false)) {
                throw new XFormParseException("itemset nodeset ref is not a parent of copy ref");
            } else if (itemset.valueRef != null && !itemset.nodesetRef.isParentOf(itemset.valueRef, false)) {
                throw new XFormParseException("itemset nodeset ref is not a parent of value ref");
            }

            if (itemset.copyRef != null && itemset.valueRef != null) {
                if (!itemset.copyRef.isParentOf(itemset.valueRef, false)) {
                    throw new XFormParseException("itemset <copy> is not a parent of <value>");
                }
            }

            //make sure the labelref is tested against the right instance
            //check if it's not the main instance
            DataInstance fi = null;
            if (itemset.labelRef.getInstanceName() != null) {
                fi = formDef.getNonMainInstance(itemset.labelRef.getInstanceName());
                if (fi == null) {
                    throw new XFormParseException("Instance: " + itemset.labelRef.getInstanceName() + " Does not exists");
                }
            } else {
                fi = instance;
            }


            if (fi.getTemplatePath(itemset.labelRef) == null) {
                throw new XFormParseException("<label> node for itemset doesn't exist! [" + itemset.labelRef + "]");
            }
            //check value nodes exist
            else if (itemset.valueRef != null && fi.getTemplatePath(itemset.valueRef) == null) {
                throw new XFormParseException("<value> node for itemset doesn't exist! [" + itemset.valueRef + "]");
            }
        }
    }

    private void verifyItemsetSrcDstCompatibility (FormInstance instance) {
        for (ItemsetBinding itemset : itemsets) {
            boolean destRepeatable = (instance.getTemplate(itemset.getDestRef()) != null);
            if (itemset.copyMode) {
                if (!destRepeatable) {
                    throw new XFormParseException("itemset copies to node(s) which are not repeatable");
                }

                //validate homogeneity between src and dst nodes
                TreeElement srcNode = instance.getTemplatePath(itemset.copyRef);
                TreeElement dstNode = instance.getTemplate(itemset.getDestRef());

                if (!FormInstance.isHomogeneous(srcNode, dstNode)) {
                    reporter.warning(XFormParserReporter.TYPE_INVALID_STRUCTURE,
                            "Your itemset source [" + srcNode.getRef().toString() + "] and dest [" + dstNode.getRef().toString() +
                                    "] of appear to be incompatible!", null);
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

    private void applyInstanceProperties (FormInstance instance) {
        for (DataBinding bind : bindings) {
            final TreeReference ref = FormInstance.unpackReference(bind.getReference());
            final List<TreeReference> nodes = new EvaluationContext(instance).expandReference(ref, true);

            if (nodes.size() > 0) {
                attachBindGeneral(bind);
            }
            for (TreeReference nref : nodes) {
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
        node.setDataType(bind.getDataType());

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
            node.setConstraint(new Constraint(bind.constraint, bind.constraintMessage));
        }

        node.setPreloadHandler(bind.getPreload());
        node.setPreloadParams(bind.getPreloadParams());
        node.setBindAttributes(bind.getAdditionalAttributes());
    }

    /** Checks which repeat bindings have explicit template nodes; returns a list of the bindings that do not */
    private static void checkRepeatsForTemplate (FormInstance instance, FormInstance repeatTree, List<TreeReference> missingTemplates) {
        if (repeatTree != null)
            checkRepeatsForTemplate(repeatTree.getRoot(), TreeReference.rootRef(), instance, missingTemplates);
    }

    /** Helper function for checkRepeatsForTemplate */
    private static void checkRepeatsForTemplate (TreeElement repeatTreeNode, TreeReference ref, FormInstance instance, List<TreeReference> missing) {
        String name = repeatTreeNode.getName();
        int mult = (repeatTreeNode.isRepeatable() ? TreeReference.INDEX_TEMPLATE : 0);
        ref = ref.extendRef(name, mult);

        if (repeatTreeNode.isRepeatable()) {
            TreeElement template = instance.resolveReference(ref);
            if (template == null) {
                missing.add(ref);
            }
        }

        for (int i = 0; i < repeatTreeNode.getNumChildren(); i++) {
            checkRepeatsForTemplate(repeatTreeNode.getChildAt(i), ref, instance, missing);
        }
    }

    //iterates through instance and removes template nodes that are not valid. a template is invalid if:
    //  it is declared for a node that is not repeatable
    //  it is for a repeat that is a child of another repeat and is not located within the parent's template node
    private void removeInvalidTemplates (FormInstance instance, FormInstance repeatTree) {
        removeInvalidTemplates(instance.getRoot(), (repeatTree == null ? null : repeatTree.getRoot()), true);
    }

    //helper function for removeInvalidTemplates
    private boolean removeInvalidTemplates (TreeElement instanceNode, TreeElement repeatTreeNode, boolean templateAllowed) {
        int mult = instanceNode.getMult();
        boolean repeatable = repeatTreeNode != null && repeatTreeNode.isRepeatable();

        if (mult == TreeReference.INDEX_TEMPLATE) {
            if (!templateAllowed) {
                reporter.warning(XFormParserReporter.TYPE_INVALID_STRUCTURE, "Template nodes for sub-repeats must be located within the template node of the parent repeat; ignoring template... [" + instanceNode.getName() + "]", null);
                return true;
            } else if (!repeatable) {
                reporter.warning(XFormParserReporter.TYPE_INVALID_STRUCTURE, "Warning: template node found for ref that is not repeatable; ignoring... [" + instanceNode.getName() + "]", null);
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
    private void createMissingTemplates (FormInstance instance, List<TreeReference> missingTemplates) {
        //it is VERY important that the missing template refs are listed in depth-first or breadth-first order... namely, that
        //every ref is listed after a ref that could be its parent. checkRepeatsForTemplate currently behaves this way
        for (TreeReference templRef : missingTemplates) {
            final TreeReference firstMatch;

            //make template ref generic and choose first matching node
            final TreeReference ref = templRef.clone();
            for (int j = 0; j < ref.size(); j++) {
                ref.setMultiplicity(j, TreeReference.INDEX_UNBOUND);
            }
            final List<TreeReference> nodes = new EvaluationContext(instance).expandReference(ref);
            if (nodes.size() == 0) {
                //binding error; not a single node matches the repeat binding; will be reported later
                continue;
            } else {
                firstMatch = nodes.get(0);
            }

            try {
                instance.copyNode(firstMatch, templRef);
            } catch (InvalidReferenceException e) {
                reporter.warning(XFormParserReporter.TYPE_INVALID_STRUCTURE,
                        "Could not create a default repeat template; this is almost certainly a homogeneity error! Your form will not work! (Failed on " +
                                templRef.toString() + ")", null);
            }
            trimRepeatChildren(instance.resolveReference(templRef));
        }
    }

    /**
     * Trims repeatable children of newly created template nodes; we trim because the templates are supposed to be devoid of 'data',
     * and # of repeats for a given repeat node is a kind of data.
     */
    private static void trimRepeatChildren (TreeElement node) {
        for (int i = 0; i < node.getNumChildren(); i++) {
            TreeElement child = node.getChildAt(i);
            if (child.isRepeatable()) {
                node.removeChildAt(i);
                i--;
            } else {
                trimRepeatChildren(child);
            }
        }
    }

    /**
     * Applies properties to instance nodes that are determined by controls bound to those nodes.
     * This should make you feel slightly dirty, but it allows us to be somewhat forgiving with the form
     * (e.g., a select question bound to a 'text' type node).
     */
    private void applyControlProperties(FormInstance instance) {
        for (int h = 0; h < 2; h++) {
            int type = (h == 0 ? org.javarosa.core.model.Constants.DATATYPE_CHOICE : org.javarosa.core.model.Constants.DATATYPE_CHOICE_LIST);

            for (TreeReference ref : (h == 0 ? selectOnes : selectMultis)) {
                for (TreeReference treeRef : new EvaluationContext(instance).expandReference(ref, true)) {
                    TreeElement node = instance.resolveReference(treeRef);
                    //noinspection StatementWithEmptyBody
                    if (node.getDataType() == org.javarosa.core.model.Constants.DATATYPE_CHOICE || node.getDataType() == org.javarosa.core.model.Constants.DATATYPE_CHOICE_LIST) {
                        //do nothing
                    } else if (node.getDataType() == org.javarosa.core.model.Constants.DATATYPE_NULL || node.getDataType() == Constants.DATATYPE_TEXT) {
                        node.setDataType(type);
                    } else {
                        reporter.warning(XFormParserReporter.TYPE_INVALID_STRUCTURE,
                                "Select question " + ref.toString() + " appears to have data type that is incompatible with selection", null);
                    }
                }
            }
        }
    }

    private List<TreeReference> getRepeatableRefs () {
        List<TreeReference> refs = new ArrayList<>(repeats);

        for (ItemsetBinding itemset : itemsets) {
            TreeReference srcRef = itemset.nodesetRef;
            if (!refs.contains(srcRef)) {
                //CTS: Being an itemset root is not sufficient to mark
                //a node as repeatable. It has to be nonstatic (which it
                //must be inherently unless there's a wildcard).
                boolean nonstatic = true;
                for (int j = 0; j < srcRef.size(); ++j) {
                    if (TreeReference.NAME_WILDCARD.equals(srcRef.getName(j))) {
                        nonstatic = false;
                    }
                }

                //CTS: we're also going to go ahead and assume that all external
                //instance are static (we can't modify them TODO: This may only be
                //the case if the instances are of specific types (non Tree-Element
                //style). Revisit if needed.
                if (srcRef.getInstanceName() != null) {
                    nonstatic = false;
                }
                if (nonstatic) {
                    refs.add(srcRef);
                }
            }

            if (itemset.copyMode) {
                TreeReference destRef = itemset.getDestRef();
                if (!refs.contains(destRef)) {
                    refs.add(destRef);
                }
            }
        }

        return refs;
    }

    /**
     * Builds a pseudo-data model tree that describes the repeat structure of the instance. The
     * result is a FormInstance collapsed where all indexes are 0, and repeatable nodes are flagged as such.
     * Ignores (invalid) repeats that bind outside the top-level instance data node. Returns null if no repeats.
     */
    private static FormInstance buildRepeatTree (List<TreeReference> repeatRefs, String topLevelName) {
        TreeElement root = new TreeElement(null, 0);

        for (TreeReference repeatRef : repeatRefs) {
            //check and see if this references a repeat from a non-main instance, if so, skip it
            if (repeatRef.getInstanceName() != null) {
                continue;
            }
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
            cur.setRepeatable(true);
        }

        return (root.getNumChildren() == 0) ? null :
                new FormInstance(root.getChild(topLevelName, TreeReference.DEFAULT_MULTIPLICITY));
    }
}
