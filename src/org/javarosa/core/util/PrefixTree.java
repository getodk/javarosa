/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.core.util;

import java.util.ArrayList;
import java.util.List;

public class PrefixTree {
    //Sometimes the string optimizations here are basically useless
    //due to wide availability of memory. It's easier in many cases
    //to simply keep using the framework, but just disable the actual
    //stemming/prefix ops
    boolean disablePrefixing = false;

    private PrefixTreeNode root;

    int minimumPrefixLength;
    int minimumHeuristicLength;

    //Common delimiters which we'd prefer as prefix breaks rather than
    //maximum string space
    private static final char[] DELIMITERS = {'\\', '/', '.'};
    private static final int DEL_SACRIFICE = 3;
    boolean finalized = false;

    public PrefixTree() {
        this(0);
    }

    public PrefixTree(int minimumPrefixLength) {
        logMessageAndHashCode("creating");
        root = new PrefixTreeNode(new char[0]);
        this.minimumPrefixLength = Math.max(minimumPrefixLength++, 0);
        this.minimumHeuristicLength = Math.max(minimumPrefixLength / 2, 3);
    }

    public static int sharedPrefixLength(char[] a, int aStart, char[] b) {
        int len;
        int minLength = Math.min(a.length - aStart, b.length);

        for (len = 0; len < minLength; len++) {
            if (a[len + aStart] != b[len])
                break;
        }

        return len;
    }

    public PrefixTreeNode addString(String newString) {
        if (finalized) {
            throw new RuntimeException("Can't manipulate a finalized Prefix Tree");
        }

        if (disablePrefixing) {
            PrefixTreeNode newNode = new PrefixTreeNode(newString.toCharArray());
            newNode.setTerminal();
            root.addChild(newNode);
            return newNode;
        }

        PrefixTreeNode current = root;

        final char[] chars = newString.toCharArray();
        int currentIndex = 0;

        while (currentIndex < chars.length) {

            //The length of the string we've incorporated into the tree
            int incorporatedLen = 0;

            //The (potential) next node in the tree which prefixes the rest of the string
            PrefixTreeNode node = null;

            //TODO: This would be way faster if we sorted upon insertion....
            if (current.getChildren() != null) {
                for (PrefixTreeNode prefixTreeNode : current.getChildren()) {
                    node = prefixTreeNode;

                    final char[] prefix = node.getPrefix();
                    if (ArrayUtilities.arraysEqual(prefix, 0, chars, currentIndex)) {
                        return node;
                    }

                    incorporatedLen = sharedPrefixLength(chars, currentIndex, prefix);
                    if (incorporatedLen > minimumPrefixLength) {
                        //See if we have any breaks which might make more heuristic sense than simply grabbing the
                        //biggest difference
                        for (char c : DELIMITERS) {
                            int sepLen = -1;
                            for (int i = currentIndex + incorporatedLen - 1; i >= currentIndex; i--) {
                                if (chars[i] == c) {
                                    sepLen = i - currentIndex;
                                    break;
                                }
                            }
                            if (sepLen != -1 && incorporatedLen - sepLen < DEL_SACRIFICE &&
                                    sepLen > minimumHeuristicLength) {
                                incorporatedLen = sepLen;
                                break;
                            }
                        }

                        break;
                    }
                    node = null;
                }
            }

            //If we didn't find anything that shared any common roots
            if (node == null) {
                //Create a placeholder for the rest of the string
                final char[] newArray;
                if (currentIndex == 0) {
                    newArray = chars;
                } else {
                    newArray = new char[chars.length - currentIndex];
                    System.arraycopy(chars, currentIndex, newArray, 0, chars.length - currentIndex);
                }
                node = new PrefixTreeNode(newArray);

                incorporatedLen = chars.length - currentIndex;

                //Add this to the highest level prefix we've found
                current.addChild(node);
            }
            //Otherwise check to see if we are going to split the current prefix
            else if (incorporatedLen < node.getPrefix().length) {
                final char[] newPrefix = new char[incorporatedLen];
                System.arraycopy(chars, currentIndex, newPrefix, 0, incorporatedLen);
                node = current.budChild(node, newPrefix, incorporatedLen);
            }

            current = node;
            currentIndex += incorporatedLen;
        }

        current.setTerminal();
        return current;
    }

    public List<String> getStrings() {
        if (finalized) {
            throw new RuntimeException("Can't get the strings from a finalized Prefix Tree");
        }
        List<String> v = new ArrayList<>(1);
        root.decompose(v, "");
        return v;
    }

    public void enablePrefixing(boolean enable) {
        this.disablePrefixing = !enable;
    }

    public static class Info {
        public int nodeCount = 0;
        public int stringSpace = 0;
    }

    /** Provides information about the space used by this tree */
    public Info getInfo() {
        return root.getInfo();
    }

    public String toString() {
        return root.toString();
    }

    public void seal() {
        root.seal();
        finalized = true;
    }

    public void clear() {
        logMessageAndHashCode("clearing");
        finalized = false;
        root = new PrefixTreeNode(new char[0]);
    }

    private void logMessageAndHashCode(String message) {
        System.out.printf("PrefixTree %s %s\n", System.identityHashCode(this), message);
    }
}
