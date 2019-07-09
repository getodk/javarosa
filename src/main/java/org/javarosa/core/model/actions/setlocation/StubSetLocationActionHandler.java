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

package org.javarosa.core.model.actions.setlocation;

/**
 * Registering this stub allows for clients that only read forms such as ODK Validate to recognize the action. However,
 * acquiring location requires a platform-specific implementation so clients that enable form filling must provide a
 * subclass implementation that actually provides location information.
 */
public final class StubSetLocationActionHandler extends SetLocationActionHandler {
    @Override
    public SetLocationAction getSetLocationAction() {
        // We'd like to use the default constructor but then the name wouldn't be set because the default constructor
        // has to have an empty body for serialization. Instead, set a null reference and let handle set the target.
        return new StubSetLocationAction(null);
    }
}