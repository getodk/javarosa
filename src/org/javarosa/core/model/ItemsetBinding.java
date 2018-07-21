package org.javarosa.core.model;

import static org.javarosa.core.model.FormDef.getAbsRef;
import static org.javarosa.xform.parse.RandomizeHelper.shuffle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import org.javarosa.core.model.condition.IConditionExpr;
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
    public Long randomSeed = null;

    public List<SelectChoice> getChoices () {
        return choices;
    }

    public void setChoices (List<SelectChoice> choices, Localizer localizer) {
        if (this.choices != null) {
            logger.warn("previous choices not cleared out");
            clearChoices();
        }
        this.choices = randomize ? shuffle(choices, randomSeed) : choices;

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
        nodesetRef = getPathAbsTreeRef(new XPathReference(getCastExpr(nodesetExpr).getReference()), contextRef);

        if (labelExpr != null) labelRef = getCondAbsTreeRef(labelExpr, nodesetRef);
        if (copyExpr  != null) copyRef  = getCondAbsTreeRef(copyExpr,  nodesetRef);
        if (valueExpr != null) valueRef = getCondAbsTreeRef(valueExpr, nodesetRef);

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

    /** Returns a TreeReference from an absolute reference from an IConditionExpr and a parent TreeReference */
    private static TreeReference getCondAbsTreeRef(IConditionExpr condExpr, TreeReference parentTreeRef) {
        XPathPathExpr xPathPathExpr = getCastExpr(condExpr);
        return getPathAbsTreeRef(new XPathReference(xPathPathExpr), parentTreeRef);
    }

    /** Returns a TreeReference from an absolute reference from an XPathReference and a parent TreeReference */
    private static TreeReference getPathAbsTreeRef(XPathReference xPathReference, TreeReference parentTreeRef) {
        IDataReference dataReference = getAbsRef(xPathReference, parentTreeRef);
        return (TreeReference) dataReference.getReference();
    }

    private static XPathPathExpr getCastExpr(IConditionExpr expr) {
        return (XPathPathExpr) ((XPathConditional) expr).getExpr();
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
        randomSeed = (Long)ExtUtil.read(in, new ExtWrapNullable(new ExtWrapTagged()));
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
        ExtUtil.write(out, new ExtWrapNullable(randomSeed == null ? null : new ExtWrapTagged(randomSeed)));
    }

}
