package org.javarosa.entities;

import kotlin.Pair;

import java.util.List;

public class Entity {

    public final String dataset;
    public final List<Pair<String, String>> properties;

    public Entity(String dataset, List<Pair<String, String>> properties) {
        this.dataset = dataset;
        this.properties = properties;
    }
}
