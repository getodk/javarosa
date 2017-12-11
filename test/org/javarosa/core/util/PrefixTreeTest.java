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

import org.junit.Test;

public class PrefixTreeTest {

    @Test public void doTests() {

        PrefixTree t = new PrefixTree();
        System.out.println(t.toString());

        add(t, "abcde");
        add(t, "abcdefghij");
        add(t, "abcdefghijklmno");
        add(t, "abcde");
        add(t, "abcdefg");
        add(t, "xyz");
        add(t, "abcdexyz");
        add(t, "abcppppp");

    }

    public void add (PrefixTree t, String newString) {
        t.addString(newString);
        System.out.println(t.toString());

        for (String string : t.getStrings()) {
            System.out.println(string);
        }
    }
}
