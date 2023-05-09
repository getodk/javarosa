package org.javarosa.core.model.instance;

import java.util.List;

public interface TreeReferenceIndex {
    boolean contains(String section);

    void add(String section, String key, TreeReference reference);

    List<TreeReference> lookup(String section, String key);
}
