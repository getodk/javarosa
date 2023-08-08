package org.javarosa.benchmarks.utils.builder.form;

import java.util.List;

/**
 * Abstracts the secondary instances
 */
public class ChoiceSelector {

    public enum Type {
        INTERNAL,
        EXTERNAL
    }

    private String instanceId;
    private List<Choice> items;

    public ChoiceSelector(String instanceId, List<Choice>  items) {
        this.instanceId = instanceId;
        this.items = items;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public List<Choice> getItems() {
        return items;
    }

}
