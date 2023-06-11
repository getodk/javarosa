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

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.BiFunction;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.IFunctionHandler;
import org.javarosa.core.model.utils.DateFormatter;
import org.javarosa.xpath.IExprDataType;
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class IFunctionHandlerHelpers {
    private static final Logger logger = LoggerFactory.getLogger(IFunctionHandlerHelpers.class);
    static final IFunctionHandler HANDLER_REGEX = buildHandler("regex", (args, ___) -> {
        logger.info("EVAL REGEX TESTS:");
        for (Object arg : args)
            logger.info("REGEX ARGS: {}", arg.toString());
        return true;
    }, String.class, String.class);
    static final IFunctionHandler HANDLER_TESTFUNC = buildHandler("testfunc", (__, ___) -> true);
    static final IFunctionHandler HANDLER_INCONVERTIBLE = buildHandler("inconvertible", (args, ___) -> new Object());
    static final IFunctionHandler HANDLER_CONVERTIBLE = buildHandler("convertible", (args, ___) -> new IExprDataType() {
        @Override
        public Boolean toBoolean() { return true; }

        @Override
        public Double toNumeric() { return 5.0; }

        @Override
        public String toString() { return "hi"; }
    });
    static final IFunctionHandler HANDLER_ADD = buildHandler("add", (args, ___) -> (Double) args[0] + (Double) args[1], Double.class, Double.class);
    static final IFunctionHandler HANDLER_PROTO = buildHandler("proto", (args, ___) -> printArgs(args), new Class[]{Double.class, Double.class}, new Class[]{Double.class}, new Class[]{String.class, String.class}, new Class[]{Double.class, String.class, Boolean.class});
    static final IFunctionHandler HANDLER_NULL_PROTO = buildNullProtoHandler("null-proto", (__, ___) -> false);
    static final IFunctionHandler HANDLER_RAW = buildRawArgsHandler("raw", (args, ___) -> printArgs(args), Double.class, String.class, Boolean.class);
    static final IFunctionHandler HANDLER_GET_CUSTOM = buildHandler("get-custom", (args, ___) -> (Boolean) args[0] ? new CustomSubType() : new CustomType(), Boolean.class);
    static final IFunctionHandler HANDLER_CONCAT = buildRawArgsHandler("concat", (args, ___) -> {
        StringBuilder sb = new StringBuilder();
        for (Object arg : args)
            sb.append(XPathFuncExpr.toString(arg));
        return sb.toString();
    });
    static final IFunctionHandler HANDLER_CHECK_TYPES = buildHandler("check-types", (args, ___) -> {
        if (args.length != 5 || !(args[0] instanceof Boolean) || !(args[1] instanceof Double) ||
            !(args[2] instanceof String) || !(args[3] instanceof Date) || !(args[4] instanceof CustomType))
            throw new RuntimeException("Types in custom function handler not converted properly/prototype not matched properly");

        return true;
    }, Boolean.class, Double.class, String.class, Date.class, CustomType.class);
    static StatefulFunc HANDLER_STATEFUL_READ = new StatefulFunc("read", (o, args) -> o.value);
    static StatefulFunc HANDLER_STATEFUL_WRITE = new StatefulFunc("write", (o, args) -> {
        o.value = (String) args[0];
        return true;
    }, String.class);

    private static IFunctionHandler buildHandler(String name, BiFunction<Object[], EvaluationContext, Object> evalBlock) {
        return buildHandler(name, evalBlock, new Class[]{});
    }

    private static IFunctionHandler buildHandler(String name, BiFunction<Object[], EvaluationContext, Object> evalBlock, Class... prototypes) {
        return buildHandler(name, evalBlock, false, new Class[][]{prototypes});
    }

    private static IFunctionHandler buildHandler(String name, BiFunction<Object[], EvaluationContext, Object> evalBlock, Class[]... prototypeArrays) {
        return buildHandler(name, evalBlock, false, prototypeArrays);
    }

    private static IFunctionHandler buildRawArgsHandler(String name, BiFunction<Object[], EvaluationContext, Object> evalBlock) {
        return buildRawArgsHandler(name, evalBlock, new Class[]{});
    }

    private static IFunctionHandler buildRawArgsHandler(String name, BiFunction<Object[], EvaluationContext, Object> evalBlock, Class... prototypes) {
        return buildHandler(name, evalBlock, true, new Class[][]{prototypes});
    }

    private static IFunctionHandler buildHandler(String name, BiFunction<Object[], EvaluationContext, Object> evalBlock, boolean rawArgs, Class[][] prototypeArrays) {
        return new IFunctionHandler() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public List<Class[]> getPrototypes() {
                return Arrays.asList(prototypeArrays);
            }

            @Override
            public boolean rawArgs() {
                return rawArgs;
            }

            @Override
            public boolean realTime() {
                return false;
            }

            @Override
            public Object eval(Object[] args, EvaluationContext ec) {
                return evalBlock.apply(args, ec);
            }
        };
    }

    private static IFunctionHandler buildNullProtoHandler(String name, BiFunction<Object[], EvaluationContext, Object> evalBlock) {
        return new IFunctionHandler() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public List<Class[]> getPrototypes() {
                return null;
            }

            @Override
            public boolean rawArgs() {
                return false;
            }

            @Override
            public boolean realTime() {
                return false;
            }

            @Override
            public Object eval(Object[] args, EvaluationContext ec) {
                return evalBlock.apply(args, ec);
            }
        };

    }

    private static String printArgs(Object[] oa) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < oa.length; i++) {
            String fullName = oa[i].getClass().getName();
            int lastIndex = Math.max(fullName.lastIndexOf('.'), fullName.lastIndexOf('$'));
            sb.append(fullName.substring(lastIndex + 1, fullName.length()));
            sb.append(":");
            sb.append(oa[i] instanceof Date ? DateFormatter.formatDate((Date) oa[i], DateFormatter.FORMAT_ISO8601) : oa[i].toString());
            if (i < oa.length - 1)
                sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    static class CustomType {

        public String toString() { return ""; }

        public boolean equals(Object o) { return o instanceof CustomType; }
    }

    private static class CustomSubType extends CustomType {
    }
}
