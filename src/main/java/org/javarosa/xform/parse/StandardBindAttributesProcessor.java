package org.javarosa.xform.parse;

import static org.javarosa.xform.parse.Constants.ID_ATTR;
import static org.javarosa.xform.parse.Constants.NODESET_ATTR;
import static org.javarosa.xform.parse.XFormParser.NAMESPACE_JAVAROSA;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.javarosa.core.model.DataBinding;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.condition.ConditionAction;
import org.javarosa.core.model.condition.Triggerable;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.model.xform.XPathReference;
import org.javarosa.xpath.XPathConditional;
import org.javarosa.xpath.XPathException;
import org.javarosa.xpath.parser.XPathSyntaxException;
import org.kxml2.kdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class StandardBindAttributesProcessor {
    private static final Logger logger = LoggerFactory.getLogger(StandardBindAttributesProcessor.class);

    StandardBindAttributesProcessor(Map<String, Integer> typeMappings) {
        this.typeMappings = typeMappings;
    }

    private Map<String, Integer> typeMappings;

    DataBinding createBinding(IXFormParserFunctions parserFunctions, FormDef formDef,
                              Collection<String> usedAttributes, Collection<String> passedThroughAttributes,
                              Element element, List<BindingAttributeProcessor> bindingAttributeProcessors) {
        final DataBinding binding = new DataBinding();

        binding.setId(element.getAttributeValue("", ID_ATTR));

        final String nodeset = element.getAttributeValue(null, NODESET_ATTR);
        if (nodeset == null) {
            throw new XFormParseException("XForm Parse: <bind> without nodeset", element);
        }

        IDataReference ref;
        try {
            ref = new XPathReference(nodeset);
        } catch (XPathException xpe) {
            throw new XFormParseException(xpe.getMessage());
        }
        ref = parserFunctions.getAbsRef(ref, formDef);
        binding.setReference(ref);

        binding.setDataType(getDataType(element.getAttributeValue(null, "type")));

        String xpathRel = element.getAttributeValue(null, "relevant");
        if (xpathRel != null) {
            if ("true()".equals(xpathRel)) {
                binding.relevantAbsolute = true;
            } else if ("false()".equals(xpathRel)) {
                binding.relevantAbsolute = false;
            } else {
                binding.relevancyCondition = formDef.addTriggerable(buildCondition(xpathRel, "relevant", ref));
            }
        }

        String xpathReq = element.getAttributeValue(null, "required");
        if (xpathReq != null) {
            if ("true()".equals(xpathReq)) {
                binding.requiredAbsolute = true;
            } else if ("false()".equals(xpathReq)) {
                binding.requiredAbsolute = false;
            } else {
                binding.requiredCondition = formDef.addTriggerable(buildCondition(xpathReq, "required", ref));
            }
        }

        String xpathRO = element.getAttributeValue(null, "readonly");
        if (xpathRO != null) {
            if ("true()".equals(xpathRO)) {
                binding.readonlyAbsolute = true;
            } else if ("false()".equals(xpathRO)) {
                binding.readonlyAbsolute = false;
            } else {
                binding.readonlyCondition = formDef.addTriggerable(buildCondition(xpathRO, "readonly", ref));
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
            try {
                binding.calculate = formDef.addTriggerable(buildCalculate(xpathCalc, ref));
            } catch (XPathSyntaxException xpse) {
                throw new XFormParseException("Invalid calculate for the bind attached to \"" + nodeset +
                    "\" : " + xpse.getMessage() + " in expression " + xpathCalc);
            }

        }

        binding.setPreload(element.getAttributeValue(NAMESPACE_JAVAROSA, "preload"));
        binding.setPreloadParams(element.getAttributeValue(NAMESPACE_JAVAROSA, "preloadParams"));

        bindingAttributeProcessors.forEach(bindingAttributeProcessor -> {
            for (int i = 0; i < element.getAttributeCount(); i++) {
                String name = element.getAttributeName(i);
                if (bindingAttributeProcessor.getUsedAttributes().contains(name)) {
                    bindingAttributeProcessor.processBindingAttribute(name, element.getAttributeValue(i), binding);
                }
            }
        });

        List<String> processorAttributes = bindingAttributeProcessors.stream()
            .flatMap((Function<BindingAttributeProcessor, Stream<String>>) bindingAttributeProcessor -> {
                return bindingAttributeProcessor.getUsedAttributes().stream();
            })
            .collect(Collectors.toList());

        List<String> allUsedAttributes = new ArrayList<>();
        allUsedAttributes.addAll(usedAttributes);
        allUsedAttributes.addAll(processorAttributes);

        saveUnusedAttributes(binding, element, allUsedAttributes, passedThroughAttributes);

        return binding;
    }

    private Triggerable buildCondition(String xpath, String type, IDataReference contextRef) {
        final ConditionAction trueAction;
        final ConditionAction falseAction;
        final String prettyType;

        switch (type) {
            case "relevant":
                prettyType = "display";
                trueAction = ConditionAction.RELEVANT;
                falseAction = ConditionAction.NOT_RELEVANT;
                break;
            case "required":
                prettyType = "require";
                trueAction = ConditionAction.REQUIRE;
                falseAction = ConditionAction.DONT_REQUIRE;
                break;
            case "readonly":
                prettyType = "readonly";
                trueAction = ConditionAction.READ_ONLY;
                falseAction = ConditionAction.ENABLE;
                break;
            default:
                throw new XFormParseException("Unsupported type " + type + " passed to buildCondition");
        }

        final XPathConditional xPathConditional;
        try {
            xPathConditional = new XPathConditional(xpath);
        } catch (XPathSyntaxException xse) {
            logger.error("XForm Parse Error: Encountered a problem with {} condition for node [{}] at line: {}{}", prettyType, contextRef.getReference().toString(), xpath, xse.getMessage());
            throw new XFormParseException("Encountered a problem with " + prettyType + " condition for node [" +
                contextRef.getReference().toString() + "] at line: " + xpath + ", " + xse.getMessage());
        }

        return Triggerable.condition(xPathConditional, trueAction, falseAction, FormInstance.unpackReference(contextRef));
    }

    private Triggerable buildCalculate(String xpath, IDataReference contextRef) throws XPathSyntaxException {
        return Triggerable.recalculate(new XPathConditional(xpath), FormInstance.unpackReference(contextRef));
    }

    /**
     * Returns data type corresponding to type string; doesn't handle defaulting to 'text' if type unrecognized/unknown
     */
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
                logger.warn("XForm Parse Warning: unrecognized data type [{}]", type);
            }
        }

        return dataType;
    }

    private void saveUnusedAttributes(DataBinding binding, Element element, Collection<String> usedAttributes,
                                      Collection<String> passedThroughAttributes) {
        for (int i = 0; i < element.getAttributeCount(); i++) {
            String name = element.getAttributeName(i);
            if (!usedAttributes.contains(name) || passedThroughAttributes.contains(name)) {
                binding.setAdditionalAttribute(element.getAttributeNamespace(i), name, element.getAttributeValue(i));
            }
        }
    }
}
