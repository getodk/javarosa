package org.javarosa.entities.internal;

import org.javarosa.entities.Entity;

import java.util.List;

public class Entities {

    private final List<Entity> entities;

    public Entities(List<Entity> entities) {
        this.entities = entities;
    }

    public List<Entity> getEntities() {
        return entities;
    }
}
