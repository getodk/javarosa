package org.javarosa.xform.util;

import org.kxml2.io.KXmlSerializer;
import org.kxml2.kdom.Element;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SecondaryInstanceAnalyzer {
    // a list of SIs that need to be built in memory
    private final List<String> inMemorySecondaryInstances;

    private final Pattern INSTANCE_FUNCTION_PATTERN = Pattern.compile("instance\\s*\\(\\s*'([^\\s]{1,64})'\\s*");

    public SecondaryInstanceAnalyzer() {
        inMemorySecondaryInstances = new ArrayList<>();
    }

    private String toXMLTag(Element element) {
        String converted = "";
        KXmlSerializer kXmlSerializer = new KXmlSerializer();
        StringWriter stringWriter = new StringWriter();
        kXmlSerializer.setOutput(stringWriter);
        try {
            element.write(kXmlSerializer);
            kXmlSerializer.flush();
            kXmlSerializer.endDocument();
            stringWriter.close();
            converted = stringWriter.toString();
        } catch (IOException e) {
            System.out.println(e.toString());
        }

        return converted;
    }

    public void analyzeElement(Element element) {
        String elementTagString = toXMLTag(element);
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
