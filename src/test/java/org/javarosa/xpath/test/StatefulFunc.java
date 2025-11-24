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

package org.javarosa.xpath.test;

import static java.util.Collections.singletonList;

import java.util.List;
import java.util.function.BiFunction;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.IFunctionHandler;

class StatefulFunc implements IFunctionHandler {
    private final String name;
    private final BiFunction<StatefulFunc, Object[], Object> evalBlock;
    private final Class[] prototypes;
    String value;

    StatefulFunc(String name, BiFunction<StatefulFunc, Object[], Object> evalBlock, Class... prototypes) {
        this.name = name;
        this.evalBlock = evalBlock;
        this.prototypes = prototypes;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<Class[]> getPrototypes() {
        return singletonList(prototypes);
    }

    @Override
    public boolean rawArgs() { return false; }

    @Override
    public Object eval(Object[] args, EvaluationContext ec) {
        return evalBlock.apply(this, args);
    }

}
