package org.javarosa.core.model.instance.utils;

import org.javarosa.core.model.instance.TreeElement;

import java.util.Iterator;
import java.util.List;

/** All operations on children of TreeElement */
public interface TreeElementChildren extends Iterable<TreeElement> {
    int size();

    @Override
    Iterator<TreeElement> iterator();

    /** Adds a child at the specified index */
    void add(int index, TreeElement treeElement);

    /** Removes a child at the specified index */
    TreeElement remove(int index);

    /** Removes a specific child */
    boolean remove(TreeElement treeElement);

    /** Removes all children */
    void clear();

    void addAll(Iterable<TreeElement> childIterable);

    boolean isEmpty();

    void addInOrder(TreeElement child);

    /** Gets the child at the specified index */
    TreeElement get(int index);

    /** Gets the child with the specified name and multiplicity */
    TreeElement get(String name, int multiplicity);

    /** Gets all children with the specified name */
    List<TreeElement> get(String name);

    /** Gets a count of all children with the specified name */
    int getCount(String name);

}
