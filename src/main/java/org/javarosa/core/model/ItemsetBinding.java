package org.javarosa.core.model;

import static org.javarosa.core.model.FormDef.getAbsRef;
import static org.javarosa.xform.parse.RandomizeHelper.shuffle;
import static org.javarosa.xpath.expr.XPathFuncExpr.toNumeric;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.IConditionExpr;
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
import org.javarosa.model.xform.XPathReference;
import org.javarosa.xpath.XPathConditional;
import org.javarosa.xpath.expr.XPathNumericLiteral;
import org.javarosa.xpath.expr.XPathPathExpr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemsetBinding implements Externalizable, Localizable {
    private static final Logger logger = LoggerFactory.getLogger(ItemsetBinding.class);

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
    private List<SelectChoice> choices; //dynamic choices -- not serialized, obviously

    public boolean randomize = false;
    public XPathNumericLiteral randomSeedNumericExpr = null;
    public XPathPathExpr randomSeedPathExpr = null;

    private static Map<String, List<SelectChoice>> choiceDictionary;

    public static void  populateChoicesDictionary(Map<String, List<TreeElement>> choiceElementDictionary){
        for(Map.Entry<String, List<TreeElement>> set : choiceElementDictionary.entrySet()){
            List<SelectChoice> choices = choiceDictionary.get(set.getKey());
            if(choices == null){ choices = new ArrayList<>(); choiceDictionary.put(set.getKey(), choices); }
            SelectChoice selectChoice = new SelectChoice("labelID", "labelInnerText", "value", false);
            choices.add(selectChoice);
        }
    }

    public List<SelectChoice> getChoicesFromMap (String key) {
        return choiceDictionary.get(key);
    }

    public List<SelectChoice> getChoices () {
        return choices;
    }

    private Long resolveRandomSeed(DataInstance model, EvaluationContext ec) {
        if (randomSeedNumericExpr != null)
            return ((Double) randomSeedNumericExpr.eval(model, ec)).longValue();
        if (randomSeedPathExpr != null)
            return toNumeric(randomSeedPathExpr.eval(model, ec)).longValue();
        return null;
    }

    public void setChoices (List<SelectChoice> choices, DataInstance model, EvaluationContext ec, Localizer localizer) {
        if (this.choices != null) {
            logger.warn("previous choices not cleared out");
            clearChoices();
        }
        this.choices = randomize ? shuffle(choices, resolveRandomSeed(model, ec)) : choices;

        if (randomize) {
            // Match indices to new positions
            for (int i = 0; i < choices.size(); i++)
                choices.get(i).setIndex(i);
        }

        //init localization
        if (localizer != null) {
            String curLocale = localizer.getLocale();
            if (curLocale != null) {
                localeChanged(curLocale, localizer);
            }
        }
    }

    public void clearChoices () {
        this.choices = null;
    }

    public void localeChanged(String locale, Localizer localizer) {
        if (choices != null) {
            for (int i = 0; i < choices.size(); i++) {
                choices.get(i).localeChanged(locale, localizer);
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
