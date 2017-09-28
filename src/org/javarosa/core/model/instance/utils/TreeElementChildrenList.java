package org.javarosa.core.model.instance.utils;

import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;

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
    public void addInOrder(TreeElement child) {
        final int childMultiplicity = child.getMult();
        final int searchMultiplicity;
        final int newIndexAdjustment;
        if (childMultiplicity == TreeReference.INDEX_TEMPLATE) {
            searchMultiplicity = 0;
            newIndexAdjustment = 0;
        } else {
            searchMultiplicity = childMultiplicity == 0 ? TreeReference.INDEX_TEMPLATE : childMultiplicity - 1;
            newIndexAdjustment = 1;
        }
        final ElementAndLoc el = getChildAndLoc(child.getName(), searchMultiplicity);
        final int newIndex = el == null ? children.size() : el.index + newIndexAdjustment;
        children.add(newIndex, child);
    }

    @Override
    public TreeElement get(String name, int multiplicity) {
        TreeElementChildrenList.ElementAndLoc el = getChildAndLoc(name, multiplicity);
        if (el == null) {
            return null;
        }
        return el.treeElement;
    }

    /**
     * Returns the count of children with the given name, and optionally supplies the children themselves.
     * @param name the name to look for
     * @param results a List into which to store the children, or null
     * @return the number of children with the given name
     */
    private int findChildrenWithName(String name, List<TreeElement> results) {
        int count = 0;
        for (TreeElement child : children) {
            if ((child.getMultiplicity() != TreeReference.INDEX_TEMPLATE) &&
                    TreeElementNameComparator.elementMatchesName(child, name)) {
                ++count;
                if (results != null) {
                    results.add(child);
                }
            }
        }
        return count;
    }

    @Override
    public List<TreeElement> get(String name) {
        List<TreeElement> children = new ArrayList<>();
        findChildrenWithName(name, children);
        return children;
    }

    @Override
    public int getCount(String name) {
        return findChildrenWithName(name, null);
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

    private class ElementAndLoc {
        final TreeElement treeElement;
        final int index;

        ElementAndLoc(TreeElement treeElement, int index) {
            this.treeElement = treeElement;
            this.index = index;
        }
    }

    private ElementAndLoc getChildAndLoc(String name, int multiplicity) {
        if (name.equals(TreeReference.NAME_WILDCARD)) {
            if(multiplicity == TreeReference.INDEX_TEMPLATE || children.size() < multiplicity + 1) {
                return null;
            }
            return new ElementAndLoc(children.get(multiplicity), multiplicity); //droos: i'm suspicious of this
        }

        for (int i = 0; i < children.size(); i++) {
            TreeElement child = children.get(i);
            if (name.equals(child.getName()) && child.getMult() == multiplicity) {
                return new ElementAndLoc(child, i);
            }
        }

        return null;
    }
}
