package org.javarosa.entities;

import kotlin.Pair;

import java.util.List;

public class Entity {

    public final String dataset;
    public final List<Pair<String, String>> fields;

    public Entity(String dataset, List<Pair<String, String>> fields) {
        this.dataset = dataset;
        this.fields = fields;
    }
}
