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

package org.javarosa.core.model.condition;

import java.util.List;

public interface IFunctionHandler {
	/**
	 * @return The name of function being handled
	 */
	String getName();

	/**
	 * @return List of allowed prototypes for this function. Each prototype is
	 *         an array of Class, corresponding to the types of the expected
	 *         arguments. The first matching prototype is used.
	 */
   List<Class[]> getPrototypes();

	/**
	 * @return true if this handler should be fed the raw argument list if no
	 *         prototype matches it
	 */
	boolean rawArgs();

	/**
	 * @return true if the result of this handler depends on some dynamic data
	 *         source, and the expression cannot be pre-computed before the
	 *         question is reached (un-supported)
	 * 
	 */
	boolean realTime();

	/**
	 * Evaluate the function
	 */
	Object eval(Object[] args, EvaluationContext ec);
}
