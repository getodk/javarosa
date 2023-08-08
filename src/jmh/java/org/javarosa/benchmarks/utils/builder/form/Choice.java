package org.javarosa.benchmarks.utils.builder.form;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstracts an element of the Secondary instance
 */
public class Choice implements IsNode {
    String label;
    String value;
    Map<String, String> attributes;

    public Choice(String label, String value) {
        this.label = label;
        this.value = value;
        attributes = new HashMap<>();
    }

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String getTagName() {
        return "item";
    }
}
