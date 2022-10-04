package org.javarosa.core.util;

import java.util.HashMap;

/**
 * Store for objects that allows them to be retrieved by type without any casting.
 *
 * @param <T> allows a super type to be enforced for all the stored objects
 */
public class Extras<T> {

    protected final HashMap<String, T> map = new HashMap<>();

    public void put(T extra) {
        map.put(extra.getClass().getName(), extra);
    }

    public <U extends T> U get(Class<U> key) {
        return (U) map.get(key.getName());
    }
}
