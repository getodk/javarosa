package org.javarosa.entities;

import kotlin.Pair;

import java.util.List;

public class Entity {

    public final String dataset;
    public final List<Pair<String, String>> properties;
    public final String id;
    public final String label;

    public Entity(String dataset, String id, String label, List<Pair<String, String>> properties) {
        this.dataset = dataset;
        this.id = id;
        this.label = label;
        this.properties = properties;
    }
}
