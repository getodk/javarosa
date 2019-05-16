package org.javarosa.core.model.instance.utils;

import static org.javarosa.core.model.instance.TreeReference.DEFAULT_MULTIPLICITY;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;

/**
 * A collection of {@link TreeElement} children. They are stored in an {@link ArrayList}.
 * when all children have the same name and no special (< 0) multiplicities, elements can
 * be retrieved in constant time.
 */
public class TreeElementChildrenList implements Iterable<TreeElement> {
    private final List<TreeElement> children = new ArrayList<>();
    /**
     * If all children have the same name, and all multiplicities are ≥ 0, children can be located in constant time
     */
    private boolean allHaveSameNameAndNormalMult = true;

    /**
     * Returns the number of children
     */
    public int size() {
        return children.size();
    }

    @Override
    public Iterator<TreeElement> iterator() {
        return children.iterator();
    }

    /**
     * Adds a child at the specified index
     */
    public void add(int index, TreeElement child) {
        checkAndSetSameNameAndNormalMult(child.getName(), child.getMultiplicity());
        children.add(index, child);
    }

    /**
     * Adds all of the provided children
     */
    public void addAll(Iterable<TreeElement> childIterable) {
        for (TreeElement child : childIterable) {
            checkAndSetSameNameAndNormalMult(child.getName(), child.getMultiplicity());
            children.add(child);
        }
    }

    public void addInOrder(TreeElement child) {
        final int childMultiplicity = child.getMultiplicity();
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
        checkAndSetSameNameAndNormalMult(child.getName(), child.getMultiplicity());
        children.add(newIndex, child);
    }

    /**
     * Gets the child at the specified index
     */
    public TreeElement get(int index) {
        return children.get(index);
    }

    /**
     * Gets all children with the specified name
     */
    public List<TreeElement> get(String name) {
        List<TreeElement> children = new ArrayList<>();
        findChildrenWithName(name, children);
        return children;
    }

    static class FilterKey {
        private final String name;
        private final String field;
        private final String value;

        FilterKey(String name, String field, String value) {
            this.name = name;
            this.field = field;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FilterKey filterKey = (FilterKey) o;
            return Objects.equals(name, filterKey.name) &&
                Objects.equals(field, filterKey.field) &&
                Objects.equals(value, filterKey.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, field, value);
        }

        @Override
        public String toString() {
            return name + '[' + field + ':' + value + ']';
        }
    }

    private final Map<FilterKey, List<TreeElement>> filteredRawValuesCache = new ConcurrentHashMap<>();

    public List<TreeElement> get(String name, String filterFieldName, String filterValue, boolean useCache) {
        if (!useCache) {
            List<TreeElement> filteredRawValues = new ArrayList<>();
            int multiplicitySeq = 0;
            for (TreeElement rawValue : this)
                if (rawValue.getName().equals(name) && rawValue.getChildrenWithName(filterFieldName).get(0).getValue().getDisplayText().equals(filterValue)) {
                    // Set the mult so that populateDynamicChoices doesn't complain
                    TreeElement rawValueCopy = rawValue.shallowCopy();
                    rawValueCopy.setMult(multiplicitySeq++);
                    filteredRawValues.add(rawValueCopy);
                }
            return filteredRawValues;
        } else {
            FilterKey key = new FilterKey(name, filterFieldName, filterValue);
            if (filteredRawValuesCache.containsKey(key))
                return filteredRawValuesCache.get(key);
            List<TreeElement> filteredRawValues = new ArrayList<>();
            int multiplicitySeq = 0;
            for (TreeElement rawValue : this)
                if (rawValue.getName().equals(name) && rawValue.getChildrenWithName(filterFieldName).get(0).getValue().getDisplayText().equals(filterValue)) {
                    // Set the mult so that populateDynamicChoices doesn't complain
                    TreeElement rawValueCopy = rawValue.shallowCopy();
                    rawValueCopy.setMult(multiplicitySeq++);
                    filteredRawValues.add(rawValueCopy);
                }
            filteredRawValuesCache.put(key, filteredRawValues);
            return filteredRawValues;
        }
    }

    /**
     * Gets the child with the specified name and multiplicity
     */
    public TreeElement get(String name, int multiplicity) {
        TreeElementChildrenList.ElementAndLoc el = getChildAndLoc(name, multiplicity);
        if (el == null) {
            return null;
        }
        return el.treeElement;
    }

    /**
     * Gets a count of all children with the specified name
     */
    public int getCount(String name) {
        return findChildrenWithName(name, null);
    }

    /**
     * Sets {@link #allHaveSameNameAndNormalMult}
     */
    private void checkAndSetSameNameAndNormalMult(String name, int mult) {
        allHaveSameNameAndNormalMult = sameNameAndNormalMult(name, mult);
    }

    /**
     * Returns whether the constant time optimization described in {@link #allHaveSameNameAndNormalMult} can be applied.
     *
     * @param name the name of a child
     * @param mult the multiplicity of a child
     */
    private boolean sameNameAndNormalMult(String name, int mult) {
        return allHaveSameNameAndNormalMult && mult >= 0 &&
            (children.isEmpty() || name.equals(children.get(0).getName()));
    }

    /**
     * Returns the count of children with the given name, and if {@code results} is not null, stores the children there.
     *
     * @param name    the name to look for
     * @param results a List into which to store the children, or null
     * @return the number of children with the given name
     */
    private int findChildrenWithName(String name, List<TreeElement> results) {
        if (sameNameAndNormalMult(name, DEFAULT_MULTIPLICITY)) {
            if (results != null) {
                results.addAll(children);
            }
            return children.size();
        }

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

    /**
     * Removes a child at the specified index
     */
    public TreeElement remove(int index) {
        return children.remove(index);
    }

    /**
     * Removes a specific child
     */
    public boolean remove(TreeElement treeElement) {
        return children.remove(treeElement);
    }

    /**
     * Removes the first child with the given name and multiplicity, if one exists
     */
    public void remove(String name, int multiplicity) {
        TreeElement child = get(name, multiplicity);
        if (child != null) {
            remove(child);
        }
    }

    public void removeAll(String name) {
        for (TreeElement child : get(name)) {
            remove(child);
        }
    }

    /**
     * Removes all children
     */
    public void clear() {
        children.clear();
    }

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
            if (multiplicity == TreeReference.INDEX_TEMPLATE || children.size() < multiplicity + 1) {
                return null;
            }
            return new ElementAndLoc(children.get(multiplicity), multiplicity); //droos: i'm suspicious of this
        }

        if (sameNameAndNormalMult(name, multiplicity) && multiplicity < children.size()) { // A constant time path
            TreeElement childAtMultPos = children.get(multiplicity);
            if (childAtMultPos.getMultiplicity() == multiplicity) {
                return new ElementAndLoc(childAtMultPos, multiplicity);
            }
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
