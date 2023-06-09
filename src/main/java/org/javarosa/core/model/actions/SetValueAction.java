/**
 *
 */
package org.javarosa.core.model.actions;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.data.AnswerDataFactory;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.AbstractTreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.xform.parse.IElementHandler;
import org.javarosa.xform.parse.XFormParseException;
import org.javarosa.xform.parse.XFormParser;
import org.javarosa.xpath.XPathTypeMismatchException;
import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.kxml2.kdom.Element;

/**
 * @author ctsims
 *
 */
public class SetValueAction extends Action {
    /** node that this action is targeting **/
    private TreeReference target;

    /** the value to be assigned to the target when this action is triggered **/
    private XPathExpression value;

    private String explicitValue;

    public static final String ELEMENT_NAME = "setvalue";

    public SetValueAction() {
        // for externalization
    }

    public SetValueAction(TreeReference target, XPathExpression value) {
        super(ELEMENT_NAME);
        this.target = target;
        this.value = value;
    }

    public SetValueAction(TreeReference target, String explicitValue) {
        super(ELEMENT_NAME);
        this.target = target;
        this.explicitValue = explicitValue;
    }

    public static IElementHandler getHandler() {
        return new IElementHandler() {
            public void handle(XFormParser p, Element e, Object parent) throws XFormParseException {
                // the generic parseAction() method in XFormParser already checks to make sure
                // that parent is an IFormElement, and throws an exception if it is not
                p.parseSetValueAction(((IFormElement) parent).getActionController(), e);
            }
        };
    }

    public TreeReference processAction(FormDef model, TreeReference contextRef) {
        // If the target is in a repeat, it's stored as an unbound ref. Use the context that the action is defined in to qualify it
        TreeReference targetReference = contextRef == null ? target : target.contextualize(contextRef);
        EvaluationContext context = new EvaluationContext(model.getEvaluationContext(), targetReference);

        String failMessage = "Target of TreeReference " + target.toString(true) + " could not be resolved!";

        List<TreeReference> references = context.expandReference(targetReference);
        if (references.size() == 0) {
            // If after finding our concrete reference it is a template, this action is outside of the
            // scope of the current target, so we can leave.
            if (model.getMainInstance().hasTemplatePath(target)) {
                return null;
            }
            throw new NullPointerException(failMessage);
        } else if (references.size() > 1) {
            throw new XPathTypeMismatchException("You are trying to target a repeated field. Currently you may only target a field in a specific repeat instance.\n\nXPath nodeset has more than one node [\" + references + \"].");
        } else {
            targetReference = references.get(0);
        }

        AbstractTreeElement node = context.resolveReference(targetReference);
        if (node == null) {
            //After all that, there's still the possibility that the qualified reference contains
            //an unbound template, so see if such a reference could exist. Unfortunately this
            //won't be included in the above walk if the template is nested, since only the
            //top level template retains its subelement templates
            if (model.getMainInstance().hasTemplatePath(target)) {
                return null;
            } else {
                throw new NullPointerException(failMessage);
            }
        }

        Object result;
        if (explicitValue != null) {
            result = explicitValue;
        } else {
            result = XPathFuncExpr.unpack(value.eval(model.getMainInstance(), context));
        }

        int dataType = node.getDataType();
        IAnswerData val = IAnswerData.wrapData(result, dataType);

        if (val == null) {
            model.setValue(null, targetReference, true);
        } else {
            model.setValue(AnswerDataFactory.templateByDataType(dataType).cast(val.uncast()),
                    targetReference, true);
        }

        return targetReference;
    }

    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
        target = (TreeReference)ExtUtil.read(in, TreeReference.class, pf);
        explicitValue = ExtUtil.nullIfEmpty(ExtUtil.readString(in));
        if(explicitValue == null) {
            value = (XPathExpression)ExtUtil.read(in, new ExtWrapTagged(), pf);
        }

    }

    public void writeExternal(DataOutputStream out) throws IOException {
        ExtUtil.write(out, target);

        ExtUtil.write(out, ExtUtil.emptyIfNull(explicitValue));
        if(explicitValue == null) {
            ExtUtil.write(out, new ExtWrapTagged(value));
        }
    }
}
