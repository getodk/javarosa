/*
 * Copyright 2019 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.javarosa.core.model.condition;

public enum ConditionAction {
    NULL(0, false),
    SHOW(1, true),
    HIDE(2, true),
    ENABLE(3, false),
    DISABLE(4, false),
    LOCK(5, false),
    UNLOCK(6, false),
    REQUIRE(7, false),
    DONT_REQUIRE(8, false);

    private final int code;
    private final boolean cascading;

    ConditionAction(int code, boolean cascading) {
        this.code = code;
        this.cascading = cascading;
    }

    public static ConditionAction from(int code) {
        for (ConditionAction candidate : values())
            if (candidate.code == code)
                return candidate;
        throw new RuntimeException("Unknown Condition action with code " + code);
    }

    public int getCode() {
        return code;
    }

    public boolean isCascading() {
        return cascading;
    }
}
