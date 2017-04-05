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
    StandardBindAttributesProcessor(XFormParserReporter reporter, Map<String, Integer> typeMappings) {
        this.reporter = reporter;
        this.typeMappings = typeMappings;
    }

    private XFormParserReporter reporter;
    private Map<String, Integer> typeMappings;

    /** Methods for setting certain binding properties */
    private interface BoolAndConditionSetter {
        void setBoolean(boolean value);
        void setCondition(Condition condition);
    }
    
    DataBinding createBinding(IXFormParserFunctions parserFunctions, FormDef formDef,
                              Collection<String> usedAttributes, Element element) {
        final DataBinding binding = new DataBinding();

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

        setBoolAndCondition(ref, element, "relevant", formDef, new BoolAndConditionSetter() {
            @Override public void setBoolean  (boolean value)       { binding.relevantAbsolute   = value;     }
            @Override public void setCondition(Condition condition) { binding.relevancyCondition = condition; }
        });
        
        setBoolAndCondition(ref, element, "required", formDef, new BoolAndConditionSetter() {
            @Override public void setBoolean  (boolean value)       { binding.requiredAbsolute   = value;   }
            @Override public void setCondition(Condition condition) { binding.requiredCondition= condition; }
        });
        
        setBoolAndCondition(ref, element, "readonly", formDef, new BoolAndConditionSetter() {
            @Override public void setBoolean  (boolean value)       { binding.readonlyAbsolute   = value;    }
            @Override public void setCondition(Condition condition) { binding.readonlyCondition = condition; }
        });
        
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

    private void setBoolAndCondition(IDataReference ref, Element element, String name,
                                     FormDef formDef, BoolAndConditionSetter setter) {
        final String xpathRel = element.getAttributeValue(null, name);

        if (xpathRel != null) {
            switch (xpathRel) {
                case "true()":
                    setter.setBoolean(true);
                    break;
                case "false()":
                    setter.setBoolean(false);
                    break;
                default:
                    Condition condition = buildCondition(xpathRel, name, ref);
                    setter.setCondition((Condition) formDef.addTriggerable(condition));
                    break;
            }
        }
    }

    private Condition buildCondition(String xpath, String type, IDataReference contextRef) {
        final int trueAction;
        final int falseAction;
        final String prettyType;

        switch (type) {
            case "relevant":
                prettyType  = "display";
                trueAction  = Condition.ACTION_SHOW;
                falseAction = Condition.ACTION_HIDE;
                break;
            case "required":
                prettyType  = "require";
                trueAction  = Condition.ACTION_REQUIRE;
                falseAction = Condition.ACTION_DONT_REQUIRE;
                break;
            case "readonly":
                prettyType  = "readonly";
                trueAction  = Condition.ACTION_DISABLE;
                falseAction = Condition.ACTION_ENABLE;
                break;
            default:
                throw new XFormParseException("Unsupported type " + type + " passed to buildCondition");
        }

        final XPathConditional xPathConditional;
        try {
            xPathConditional = new XPathConditional(xpath);
        } catch (XPathSyntaxException xse) {
            String errorMessage = "Encountered a problem with " + prettyType + " condition for node ["  + 
                    contextRef.getReference().toString() + "] at line: " + xpath + ", " +  xse.getMessage();
            reporter.error(errorMessage);
            throw new XFormParseException(errorMessage);
        }

        return new Condition(xPathConditional, trueAction, falseAction, FormInstance.unpackReference(contextRef));
    }

    private Recalculate buildCalculate(String xpath, IDataReference contextRef) throws XPathSyntaxException {
        return new Recalculate(new XPathConditional(xpath), FormInstance.unpackReference(contextRef));
    }

    /** Returns data type corresponding to type string; doesn't handle defaulting to 'text' if type unrecognized/unknown */
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
            if (! usedAttributes.contains(name)) {
                binding.setAdditionalAttribute(element.getAttributeNamespace(i), name, element.getAttributeValue(i));
            }
        }
    }
}
