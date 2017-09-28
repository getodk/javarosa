package org.javarosa.core.model.instance.utils;

import org.javarosa.core.model.instance.TreeElement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TreeElementChildrenList implements TreeElementChildren {
    private List<TreeElement> children = new ArrayList<>();

    @Override
    public int size() {
        return children.size();
    }

    @Override
    public Iterator<TreeElement> iterator() {
        return children.iterator();
    }

    @Override
    public void add(TreeElement treeElement) {
        children.add(treeElement);
    }

    @Override
    public void add(int index, TreeElement treeElement) {
        children.add(index, treeElement);
    }

    @Override
    public TreeElement remove(int index) {
        return children.remove(index);
    }

    @Override
    public boolean remove(TreeElement treeElement) {
        return children.remove(treeElement);
    }

    @Override
    public TreeElement get(int index) {
        return children.get(index);
    }

    @Override
    public void clear() {
        children.clear();
    }

    @Override
    public void addAll(TreeElementChildren children) {
        this.children.addAll(((TreeElementChildrenList) children).children);
    }

    @Override
    public boolean isEmpty() {
        return children.isEmpty();
    }
}
