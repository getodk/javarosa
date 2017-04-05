package org.javarosa.xform.parse;

import org.javarosa.core.model.DataBinding;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.condition.Condition;
import org.javarosa.core.model.condition.Recalculate;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.model.xform.XPathReference;
import org.javarosa.xpath.XPathConditional;
import org.javarosa.xpath.XPathException;
import org.javarosa.xpath.parser.XPathSyntaxException;
import org.kxml2.kdom.Element;

import java.util.Collection;
import java.util.Map;

import static org.javarosa.xform.parse.Constants.*;

class StandardBindAttributesProcessor {
    public StandardBindAttributesProcessor(XFormParserReporter reporter, Map<String, Integer> typeMappings) {
        this.reporter = reporter;
        this.typeMappings = typeMappings;
    }

    private XFormParserReporter reporter;
    private Map<String, Integer> typeMappings;

    DataBinding createBinding(IXFormParserFunctions parserFunctions, FormDef formDef,
                                     Collection<String> usedAttributes, Element element) {
        final DataBinding binding  = new DataBinding();

        binding.setId(element.getAttributeValue("", ID_ATTR));

        final String nodeset = element.getAttributeValue(null, NODESET_ATTR);
        if (nodeset == null) {
            throw new XFormParseException("XForm Parse: <bind> without nodeset",element);
        }

        IDataReference ref;
        try {
            ref = new XPathReference(nodeset);
        } catch(XPathException xpe) {
            throw new XFormParseException(xpe.getMessage());
        }
        ref = parserFunctions.getAbsRef(ref, formDef);
        binding.setReference(ref);

        binding.setDataType(getDataType(element.getAttributeValue(null, "type")));

        final String xpathRel = element.getAttributeValue(null, "relevant");
        if (xpathRel != null) {
            if ("true()".equals(xpathRel)) {
                binding.relevantAbsolute = true;
            } else if ("false()".equals(xpathRel)) {
                binding.relevantAbsolute = false;
            } else {
                binding.relevancyCondition = getCondition(parserFunctions, formDef, ref, xpathRel, "relevant");
            }
        }

        final String xpathReq = element.getAttributeValue(null, "required");
        if (xpathReq != null) {
            if ("true()".equals(xpathReq)) {
                binding.requiredAbsolute = true;
            } else if ("false()".equals(xpathReq)) {
                binding.requiredAbsolute = false;
            } else {
                binding.requiredCondition = getCondition(parserFunctions, formDef, ref, xpathReq, "required");
            }
        }

        final String xpathRO = element.getAttributeValue(null, "readonly");
        if (xpathRO != null) {
            if ("true()".equals(xpathRO)) {
                binding.readonlyAbsolute = true;
            } else if ("false()".equals(xpathRO)) {
                binding.readonlyAbsolute = false;
            } else {
                binding.readonlyCondition = getCondition(parserFunctions, formDef, ref, xpathRO, "readonly");
            }
        }

        final String xpathConstr = element.getAttributeValue(null, "constraint");
        if (xpathConstr != null) {
            try {
                binding.constraint = new XPathConditional(xpathConstr);
            } catch (XPathSyntaxException xse) {
                throw new XFormParseException("bind for " + nodeset + " contains invalid constraint expression [" + xpathConstr + "] " + xse.getMessage());
            }
            binding.constraintMessage = element.getAttributeValue(NAMESPACE_JAVAROSA, "constraintMsg");
        }

        final String xpathCalc = element.getAttributeValue(null, "calculate");
        if (xpathCalc != null) {
            Recalculate r;
            try {
                r = buildCalculate(xpathCalc, ref);
            } catch (XPathSyntaxException xpse) {
                throw new XFormParseException("Invalid calculate for the bind attached to \"" + nodeset +
                        "\" : " + xpse.getMessage() + " in expression " + xpathCalc);
            }
            r = (Recalculate) formDef.addTriggerable(r);
            binding.calculate = r;
        }

        binding.setPreload(element.getAttributeValue(NAMESPACE_JAVAROSA, "preload"));
        binding.setPreloadParams(element.getAttributeValue(NAMESPACE_JAVAROSA, "preloadParams"));

        saveUnusedAttributes(binding, element, usedAttributes);

        return binding;
    }
    
    

    private Condition buildCondition(String xpath, String type, IDataReference contextRef) {
        XPathConditional cond;
        int trueAction = -1, falseAction = -1;

        String prettyType;

        if ("relevant".equals(type)) {
            prettyType = "display condition";
            trueAction = Condition.ACTION_SHOW;
            falseAction = Condition.ACTION_HIDE;
        } else if ("required".equals(type)) {
            prettyType = "require condition";
            trueAction = Condition.ACTION_REQUIRE;
            falseAction = Condition.ACTION_DONT_REQUIRE;
        } else if ("readonly".equals(type)) {
            prettyType = "readonly condition";
            trueAction = Condition.ACTION_DISABLE;
            falseAction = Condition.ACTION_ENABLE;
        } else{
            prettyType = "unknown condition";
        }

        try {
            cond = new XPathConditional(xpath);
        } catch (XPathSyntaxException xse) {

            String errorMessage = "Encountered a problem with " + prettyType + " for node ["  + contextRef.getReference().toString() + "] at line: " + xpath + ", " +  xse.getMessage();

            reporter.error(errorMessage);

            throw new XFormParseException(errorMessage);
        }

        return new Condition(cond, trueAction, falseAction, FormInstance.unpackReference(contextRef));
    }

    private Recalculate buildCalculate(String xpath, IDataReference contextRef) throws XPathSyntaxException {
        XPathConditional calc = new XPathConditional(xpath);

        return new Recalculate(calc, FormInstance.unpackReference(contextRef));
    }

    private Condition getCondition(IXFormParserFunctions parserFunctions, FormDef formDef,
                                          IDataReference ref, String xpathRel, String name) {
        Condition c = buildCondition(xpathRel, name, ref);
        c = (Condition) formDef.addTriggerable(c);
        return c;
    }

    //returns data type corresponding to type string; doesn't handle defaulting to 'text' if type unrecognized/unknown
    private int getDataType(String type) {
        int dataType = org.javarosa.core.model.Constants.DATATYPE_NULL;

        if (type != null) {
            //cheap out and ignore namespace
            if (type.contains(":")) {
                type = type.substring(type.indexOf(":") + 1);
            }

            if (typeMappings.containsKey(type)) {
                dataType = typeMappings.get(type);
            } else {
                dataType = org.javarosa.core.model.Constants.DATATYPE_UNSUPPORTED;
                reporter.warning(XFormParserReporter.TYPE_ERROR_PRONE, "unrecognized data type [" + type + "]", null);
            }
        }

        return dataType;
    }

    private void saveUnusedAttributes(DataBinding binding, Element element, Collection<String> usedAttributes) {
        for (int i = 0; i < element.getAttributeCount(); i++) {
            String name = element.getAttributeName(i);
            if (usedAttributes.contains(name)) continue;
            binding.setAdditionalAttribute(element.getAttributeNamespace(i), name, element.getAttributeValue(i));
        }
    }
}
