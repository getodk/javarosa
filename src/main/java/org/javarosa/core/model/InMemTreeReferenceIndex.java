package org.javarosa.core.model;

import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.model.instance.TreeReferenceIndex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;

class InMemTreeReferenceIndex implements TreeReferenceIndex {

    private final Map<String, Map<String, List<TreeReference>>> map = new HashMap<>();

    @Override
    public boolean contains(String section) {
        return map.containsKey(section);
    }

    @Override
    public void add(String section, String key, TreeReference reference) {
        if (!map.containsKey(section)) {
            map.put(section, new HashMap<>());
        }

        Map<String, List<TreeReference>> sectionMap = map.get(section);
        if (!sectionMap.containsKey(key)) {
            sectionMap.put(key, new ArrayList<>());
        }

        sectionMap.get(key).add(reference);
    }

    @Override
    public List<TreeReference> lookup(String section, String key) {
        if (map.containsKey(section) && map.get(section).containsKey(key)) {
            return map.get(section).get(key);
        } else {
            return emptyList();
        }
    }
}
