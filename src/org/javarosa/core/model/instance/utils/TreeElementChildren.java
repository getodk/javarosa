package org.javarosa.core.model.instance.utils;

import org.javarosa.core.model.instance.TreeElement;

import java.util.Iterator;

/** All operations on children of TreeElement */
public interface TreeElementChildren extends Iterable<TreeElement> {
    int size();

    @Override
    Iterator<TreeElement> iterator();

    void add(TreeElement treeElement);

    void add(int index, TreeElement treeElement);

    TreeElement remove(int index);

    boolean remove(TreeElement treeElement);

    TreeElement get(int index);

    void clear();

    void addAll(TreeElementChildren children);

    boolean isEmpty();
}
