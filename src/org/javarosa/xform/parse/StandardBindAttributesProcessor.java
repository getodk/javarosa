package org.javarosa.xform.parse;

import org.javarosa.core.model.DataBinding;
import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.condition.Condition;
import org.javarosa.core.model.condition.Recalculate;
import org.javarosa.model.xform.XPathReference;
import org.javarosa.xpath.XPathConditional;
import org.javarosa.xpath.XPathException;
import org.javarosa.xpath.parser.XPathSyntaxException;
import org.kxml2.kdom.Element;

import java.util.List;

import static org.javarosa.xform.parse.Constants.*;

class StandardBindAttributesProcessor {

    static DataBinding process(IXFormParserFunctions parserFunctions, List<String> usedAtts, Element e) {
        final DataBinding binding  = new DataBinding();

        binding.setId(e.getAttributeValue("", ID_ATTR));

        String nodeset = e.getAttributeValue(null, NODESET_ATTR);
        if (nodeset == null) {
            throw new XFormParseException("XForm Parse: <bind> without nodeset",e);
        }
        IDataReference ref;
        try {
            ref = new XPathReference(nodeset);
        } catch(XPathException xpe) {
            throw new XFormParseException(xpe.getMessage());
        }
        ref = parserFunctions.getAbsRef(ref, parserFunctions.getFormDef());
        binding.setReference(ref);

        binding.setDataType(parserFunctions.getDataType(e.getAttributeValue(null, "type")));

        String xpathRel = e.getAttributeValue(null, "relevant");
        if (xpathRel != null) {
            if ("true()".equals(xpathRel)) {
                binding.relevantAbsolute = true;
            } else if ("false()".equals(xpathRel)) {
                binding.relevantAbsolute = false;
            } else {
                Condition c = parserFunctions.buildCondition(xpathRel, "relevant", ref);
                c = (Condition) parserFunctions.getFormDef().addTriggerable(c);
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
                Condition c = parserFunctions.buildCondition(xpathReq, "required", ref);
                c = (Condition) parserFunctions.getFormDef().addTriggerable(c);
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
                Condition c = parserFunctions.buildCondition(xpathRO, "readonly", ref);
                c = (Condition) parserFunctions.getFormDef().addTriggerable(c);
                binding.readonlyCondition = c;
            }
        }

        String xpathConstr = e.getAttributeValue(null, "constraint");
        if (xpathConstr != null) {
            try {
                binding.constraint = new XPathConditional(xpathConstr);
            } catch (XPathSyntaxException xse) {
                throw new XFormParseException("bind for " + nodeset + " contains invalid constraint expression [" + xpathConstr + "] " + xse.getMessage());
            }
            binding.constraintMessage = e.getAttributeValue(NAMESPACE_JAVAROSA, "constraintMsg");
        }

        String xpathCalc = e.getAttributeValue(null, "calculate");
        if (xpathCalc != null) {
            Recalculate r;
            try {
                r = parserFunctions.buildCalculate(xpathCalc, ref);
            } catch (XPathSyntaxException xpse) {
                throw new XFormParseException("Invalid calculate for the bind attached to \"" + nodeset + "\" : " + xpse.getMessage() + " in expression " + xpathCalc);
            }
            r = (Recalculate) parserFunctions.getFormDef().addTriggerable(r);
            binding.calculate = r;
        }

        binding.setPreload(e.getAttributeValue(NAMESPACE_JAVAROSA, "preload"));
        binding.setPreloadParams(e.getAttributeValue(NAMESPACE_JAVAROSA, "preloadParams"));

        // save all the unused attributes verbatim...
        for(int i=0;i<e.getAttributeCount();i++){
            String name = e.getAttributeName(i);
            if (usedAtts.contains(name)) continue;
            binding.setAdditionalAttribute(e.getAttributeNamespace(i), name, e.getAttributeValue(i));
        }

        return binding;
    }
}
