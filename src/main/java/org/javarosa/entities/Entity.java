package org.javarosa.entities;

import kotlin.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Entity {

    public final String dataset;
    public final List<Pair<String, String>> properties;
    public final String id;

    @Nullable
    public final String label;
    public Integer version;

    public Entity(String dataset, String id, @Nullable String label, Integer version, List<Pair<String, String>> properties) {
        this.dataset = dataset;
        this.id = id;
        this.label = label;
        this.version = version;
        this.properties = properties;
    }
}
