package org.javarosa.core.model;

import static org.javarosa.core.model.FormDef.getAbsRef;
import static org.javarosa.xform.parse.RandomizeHelper.shuffle;
import static org.javarosa.xpath.expr.XPathFuncExpr.toNumeric;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.IConditionExpr;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.MultipleItemsData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.model.instance.DataInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.model.util.restorable.RestoreUtils;
import org.javarosa.core.services.locale.Localizable;
import org.javarosa.core.services.locale.Localizer;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapNullable;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.debug.EvaluationResult;
import org.javarosa.debug.Event;
import org.javarosa.model.xform.XPathReference;
import org.javarosa.xpath.XPathConditional;
import org.javarosa.xpath.XPathException;
import org.javarosa.xpath.expr.XPathNumericLiteral;
import org.javarosa.xpath.expr.XPathPathExpr;

public class ItemsetBinding implements Externalizable, Localizable {
    // Temporarily cached filtered list (not serialized)
    private List<SelectChoice> cachedFilteredChoiceList;

    // Values needed to determine whether the cached list should be expired (not serialized)
    private Map<TreeReference, IAnswerData> cachedTriggerValues;
    private Long cachedRandomizeSeed;

    /**
     * note that storing both the ref and expr for everything is kind of redundant, but we're forced
     * to since it's nearly impossible to convert between the two w/o having access to the underlying
     * xform/xpath classes, which we don't from the core model project
     */
    public TreeReference nodesetRef;   //absolute ref of itemset source nodes
    public IConditionExpr nodesetExpr; //path expression for source nodes; may be relative, may contain predicates
    public TreeReference contextRef;   //context ref for nodesetExpr; ref of the control parent (group/formdef) of itemset question
       //note: this is only here because its currently impossible to both (a) get a form control's parent, and (b)
       //convert expressions into refs while preserving predicates. once these are fixed, this field can go away

    public TreeReference labelRef;     //absolute ref of label
    public IConditionExpr labelExpr;   //path expression for label; may be relative, no predicates
    public boolean labelIsItext;       //if true, content of 'label' is an itext id

    public boolean copyMode;           //true = copy subtree; false = copy string value
    public IConditionExpr copyExpr;    // path expression for copy; may be relative, no predicates
    public TreeReference copyRef;      //absolute ref to copy
    public TreeReference valueRef;     //absolute ref to value
    public IConditionExpr valueExpr;   //path expression for value; may be relative, no predicates (must be relative if copy mode)

    private TreeReference destRef; //ref that identifies the repeated nodes resulting from this itemset
                                   //not serialized -- set by QuestionDef.setDynamicChoices()

    public boolean randomize = false;
    public XPathNumericLiteral randomSeedNumericExpr = null;
    public XPathPathExpr randomSeedPathExpr = null;

    /**
     * Returns a list of <code>SelectChoice</code> objects based on the data in the model. If any of the itemset expression
     * triggers or the random seed have changed since the last request, a new list will be computed which may be time consuming.
     * Otherwise, a cached list is returned.
     *
     * SIDE EFFECT: any item currently selected as part of the current question's answer that is no longer
     * part of the new filtered list is removed and the new answer is saved back to the model.
     */
    public List<SelectChoice> getChoices(FormDef formDef, TreeReference curQRef) {
        Map<TreeReference, IAnswerData> currentTriggerValues = getCurrentTriggerValues(formDef, curQRef);
        boolean allTriggerRefsBound = currentTriggerValues != null;

        Long currentRandomizeSeed = resolveRandomSeed(formDef.getMainInstance(), formDef.getEvaluationContext());

        // Return cached list if possible
        if (cachedFilteredChoiceList != null && allTriggerRefsBound && Objects.equals(currentTriggerValues, cachedTriggerValues)
            && Objects.equals(currentRandomizeSeed, cachedRandomizeSeed)) {
            return randomize && cachedRandomizeSeed == null ? shuffle(cachedFilteredChoiceList) : cachedFilteredChoiceList;
        }

        formDef.getEventNotifier().publishEvent(new Event("Dynamic choices", new EvaluationResult(curQRef, null)));

        DataInstance formInstance;
        if (nodesetRef.getInstanceName() != null) { // the itemset is defined in a secondary instance
            formInstance = formDef.getNonMainInstance(nodesetRef.getInstanceName());
            if (formInstance == null) {
                throw new XPathException("Instance " + nodesetRef.getInstanceName() + " not found");
            }
        } else {
            formInstance = formDef.getMainInstance();
        }

        List<TreeReference> filteredItemReferences = nodesetExpr.evalNodeset(formDef.getMainInstance(),
            new EvaluationContext(formDef.getEvaluationContext(), contextRef.contextualize(curQRef)));

        if (filteredItemReferences == null) {
            throw new XPathException("Could not find references depended on by" + nodesetRef.getInstanceName());
        }

        Map<String, SelectChoice> selectChoicesForAnswer = initializeAnswerMap(formDef, curQRef);

        List<SelectChoice> choices = new ArrayList<>();
        for (int i = 0; i < filteredItemReferences.size(); i++) {
            SelectChoice choice = getChoiceForTreeReference(formDef, formInstance, i, filteredItemReferences.get(i));
            choices.add(choice);
            if (selectChoicesForAnswer != null && selectChoicesForAnswer.containsKey(choice.getValue())) {
                // Keys with values that don't get set here will have null values and must be filtered out of the answer.
                selectChoicesForAnswer.put(choice.getValue(), choice);
            }
        }

        updateQuestionAnswerInModel(formDef, curQRef, selectChoicesForAnswer);

        cachedFilteredChoiceList = randomize ? shuffle(choices, currentRandomizeSeed) : choices;

        // TODO: write a test that fails if this is removed. It looks like a no-op because it's not accessing the shuffled collection.
        if (randomize) {
            // Match indices to new positions
            for (int i = 0; i < choices.size(); i++)
                choices.get(i).setIndex(i);
        }

        //init localization
        // TODO: write a test that fails if this is removed
        if (formDef.getLocalizer() != null) {
            String curLocale = formDef.getLocalizer().getLocale();
            if (curLocale != null) {
                localeChanged(curLocale, formDef.getLocalizer());
            }
        }

        cachedTriggerValues = currentTriggerValues;
        cachedRandomizeSeed = currentRandomizeSeed;

        return cachedFilteredChoiceList;
    }

    /**
     * Returns a map:
     *  - keys: the references that are triggers for the nodeset expression
     *  - values: current values at those references
     *
     * Returns null if the nodeset expression has any triggers that are unbounded references because there's no single
     * value we could track in that case.
     */
    private Map<TreeReference, IAnswerData> getCurrentTriggerValues(FormDef formDef, TreeReference curQRef) {
        Map<TreeReference, IAnswerData> currentTriggerValues = new HashMap<>();

        Set<TreeReference> triggers = nodesetExpr.getTriggers(curQRef);
        for (TreeReference trigger : triggers) {
            // Only store values for expressions in the primary instance. Secondary instances can never change so no need to store their values.
            if (trigger.getInstanceName() == null) {
                TreeElement element = formDef.getMainInstance().resolveReference(trigger);

                // Unbounded references (e.g. ref to a repeat nodeset rather than a repeat instance) don't have a value we can keep track of.
                if (element != null && !element.isRepeatable()) {
                    currentTriggerValues.put(trigger, element.getValue());
                } else {
                    return null;
                }
            }
        }
        return currentTriggerValues;
    }

    private SelectChoice getChoiceForTreeReference(FormDef formDef, DataInstance formInstance, int i, TreeReference item) {
        String label = labelExpr.evalReadable(formInstance, new EvaluationContext(formDef.getEvaluationContext(), item));
        String value = null;
        if (valueRef != null) {
            value = valueExpr.evalReadable(formInstance, new EvaluationContext(formDef.getEvaluationContext(), item));
        }
        // Provide a default value if none is specified
        value = value != null ? value : "dynamic:" + i;

        TreeElement copyNode = null;
        if (copyMode) {
            copyNode = formDef.getMainInstance().resolveReference(copyRef.contextualize(item));
        }

        SelectChoice choice = new SelectChoice(label, value, labelIsItext);
        choice.setIndex(i);
        if (copyMode)
            choice.copyNode = copyNode;
        return choice;
    }

    /**
     * Build a map with keys for each value in the current answer, each mapped to null.
     *
     * When we iterate over the new filtered choice list, we will update the values in this map. Keys with null values
     * after this process will be removed from the answer. We will also use this map to bind selection(s) in the IAnswerData
     * to Selection objects. The latter is necessary to get label text for the answers.
     */
    private Map<String, SelectChoice> initializeAnswerMap(FormDef formDef, TreeReference curQRef) {
        Map<String, SelectChoice> selectChoicesForAnswer = null;
        IAnswerData rawValue = formDef.getMainInstance().resolveReference(curQRef).getValue();
        if (rawValue != null) {
            selectChoicesForAnswer = new HashMap<>();

            if (rawValue instanceof MultipleItemsData) {
                for (Selection selection : (List<Selection>) rawValue.getValue()) {
                    selectChoicesForAnswer.put(selection.choice != null ? selection.choice.getValue() : selection.xmlValue, null);
                }
            } else {
                selectChoicesForAnswer.put(rawValue.getDisplayText(), null);
            }
        }

        return selectChoicesForAnswer;
    }

    private void updateQuestionAnswerInModel(FormDef formDef, TreeReference curQRef, Map<String, SelectChoice> selectChoicesForAnswer) {
        IAnswerData originalValue = formDef.getMainInstance().resolveReference(curQRef).getValue();

        if (selectChoicesForAnswer != null) {
            IAnswerData boundAndFilteredValue;
            if (originalValue instanceof MultipleItemsData) {
                boundAndFilteredValue = getFilteredAndBoundSelections((MultipleItemsData) originalValue, selectChoicesForAnswer);
            } else if (selectChoicesForAnswer.containsValue(null)) {
                boundAndFilteredValue = null;
            } else {
                SelectChoice selectChoice = selectChoicesForAnswer.get(originalValue.getDisplayText());
                boundAndFilteredValue = new SelectOneData(selectChoice.selection());
            }

            formDef.getMainInstance().resolveReference(curQRef).setAnswer(boundAndFilteredValue);
        }
    }

    /**
     * @param selections          an answer to a multiple selection question
     * @param selectChoicesForAnswer maps each value that could be in @{code selections} to a SelectChoice if it should be bound
     *                            or null if it should be removed.
     * @return a copy of {@code selections} without the values that were mapped to null in {@code selectChoicesForAnswer} and
     * with all selections bound.
     */
    private static MultipleItemsData getFilteredAndBoundSelections(MultipleItemsData selections, Map<String, SelectChoice> selectChoicesForAnswer) {
        List<Selection> newSelections = new ArrayList<>();
        for (Selection oldSelection : (List<Selection>) selections.getValue()) {
            String key = oldSelection.choice != null ? oldSelection.choice.getValue() : oldSelection.xmlValue;
            SelectChoice selectChoice = selectChoicesForAnswer.get(key);
            if (selectChoice != null) {
                newSelections.add(selectChoice.selection());
            }
        }

        return new MultipleItemsData(newSelections);
    }

    private Long resolveRandomSeed(DataInstance model, EvaluationContext ec) {
        if (randomSeedNumericExpr != null)
            return ((Double) randomSeedNumericExpr.eval(model, ec)).longValue();
        if (randomSeedPathExpr != null)
            return toNumeric(randomSeedPathExpr.eval(model, ec)).longValue();
        return null;
    }

    public void localeChanged(String locale, Localizer localizer) {
        if (cachedFilteredChoiceList != null) {
            for (SelectChoice selectChoice : cachedFilteredChoiceList) {
                selectChoice.localeChanged(locale, localizer);
            }
        }
    }

    public TreeReference getDestRef () {
        return destRef;
    }

    public IConditionExpr getRelativeValue () {
        TreeReference relRef = null;

        if (copyRef == null) {
            relRef = valueRef; //must be absolute in this case
        } else if (valueRef != null) {
            relRef = valueRef.relativize(copyRef);
        }

        return relRef != null ? RestoreUtils.xfFact.refToPathExpr(relRef) : null;
    }

    public void initReferences(QuestionDef q) {
        // To construct the xxxRef, we need the full model, which wasn't available before now.
        // Compute the xxxRefs now.

        // Convert the expression to a relative reference and then anchor it to the context.
        nodesetRef = getAbsoluteRef(nodesetExpr, contextRef);

        // For the label, copy and value, get absolute references. To do that, start with the expressions, convert
        // them to relative references and then anchor those to the nodesetRef we previously anchored.
        if (labelExpr != null) labelRef = getAbsoluteRef(labelExpr, nodesetRef);
        if (copyExpr  != null) copyRef  = getAbsoluteRef(copyExpr,  nodesetRef);
        if (valueExpr != null) valueRef = getAbsoluteRef(valueExpr, nodesetRef);

        if (q != null) {
            // When loading from XML, the first time through, during verification, q will be null.
            // The second time through, q will be non-null.
            // Otherwise, when loading from binary, this will be called only once with a non-null q.
            destRef = ((TreeReference) q.getBind().getReference()).clone();
            if (copyMode) {
                destRef.add(copyRef.getNameLast(), TreeReference.INDEX_UNBOUND);
            }
        }
    }

    private static TreeReference getAbsoluteRef(IConditionExpr condExpr, TreeReference baseRef) {
        XPathPathExpr xPathPathExpr = (XPathPathExpr) ((XPathConditional) condExpr).getExpr();
        IDataReference dataReference = getAbsRef(new XPathReference(xPathPathExpr), baseRef);
        return (TreeReference) dataReference.getReference();
    }

    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
        nodesetExpr = (IConditionExpr)ExtUtil.read(in, new ExtWrapTagged(), pf);
        contextRef = (TreeReference)ExtUtil.read(in, TreeReference.class, pf);
        labelExpr = (IConditionExpr)ExtUtil.read(in, new ExtWrapTagged(), pf);
        valueExpr = (IConditionExpr)ExtUtil.read(in, new ExtWrapNullable(new ExtWrapTagged()), pf);
        copyExpr = (IConditionExpr)ExtUtil.read(in, new ExtWrapNullable(new ExtWrapTagged()), pf);
        labelIsItext = ExtUtil.readBool(in);
        copyMode = ExtUtil.readBool(in);
        randomize = ExtUtil.readBool(in);
        randomSeedNumericExpr = (XPathNumericLiteral) ExtUtil.read(in, new ExtWrapNullable(new ExtWrapTagged()), pf);
        randomSeedPathExpr = (XPathPathExpr) ExtUtil.read(in, new ExtWrapNullable(new ExtWrapTagged()), pf);
    }

    public void writeExternal(DataOutputStream out) throws IOException {
        ExtUtil.write(out, new ExtWrapTagged(nodesetExpr));
        ExtUtil.write(out, contextRef);
        ExtUtil.write(out, new ExtWrapTagged(labelExpr));
        ExtUtil.write(out, new ExtWrapNullable(valueExpr == null ? null : new ExtWrapTagged(valueExpr)));
        ExtUtil.write(out, new ExtWrapNullable(copyExpr == null ? null : new ExtWrapTagged(copyExpr)));
        ExtUtil.writeBool(out, labelIsItext);
        ExtUtil.writeBool(out, copyMode);
        ExtUtil.writeBool(out, randomize);
        ExtUtil.write(out, new ExtWrapNullable(randomSeedNumericExpr == null ? null : new ExtWrapTagged(randomSeedNumericExpr)));
        ExtUtil.write(out, new ExtWrapNullable(randomSeedPathExpr == null ? null : new ExtWrapTagged(randomSeedPathExpr)));
    }

}
