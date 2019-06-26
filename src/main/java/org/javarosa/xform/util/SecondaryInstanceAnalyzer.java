package org.javarosa.xform.util;

import org.kxml2.kdom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.javarosa.xform.util.XFormSerializer.elementToString;


public class SecondaryInstanceAnalyzer {
    // a list of SIs that need to be built in memory
    private final List<String> inMemorySecondaryInstances;

    private final Pattern INSTANCE_FUNCTION_PATTERN = Pattern.compile("instance\\s*\\(\\s*'([^\\s]{1,64})'\\s*");

    public SecondaryInstanceAnalyzer() {
        inMemorySecondaryInstances = new ArrayList<>();
    }

    public void analyzeElement(Element element) {
        String elementTagString = elementToString(element);
        if (elementTagString == null)
            return;

        Matcher matcher = INSTANCE_FUNCTION_PATTERN.matcher(elementTagString);
        String functionFirstParam = matcher.find() ? matcher.group(1) : null;
        if (functionFirstParam != null && !inMemorySecondaryInstances.contains(functionFirstParam))
            inMemorySecondaryInstances.add(functionFirstParam);
    }

    public List<String> getInMemorySecondaryInstances() {
        return inMemorySecondaryInstances;
    }

    public boolean shouldSecondaryInstanceBeParsed(String instanceId) {
        return inMemorySecondaryInstances.contains(instanceId);
    }
}
