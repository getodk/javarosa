package org.javarosa.entities.internal;

import org.javarosa.entities.Entity;

import java.util.List;

public class EntitiesAttachment {

    private final List<Entity> entities;

    public EntitiesAttachment(List<Entity> entities) {
        this.entities = entities;
    }

    public List<Entity> getEntities() {
        return entities;
    }
}
