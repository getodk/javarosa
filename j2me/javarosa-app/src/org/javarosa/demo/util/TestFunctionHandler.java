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

package org.javarosa.demo.util;

import java.util.Vector;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.IFunctionHandler;

public class TestFunctionHandler implements IFunctionHandler {

	public Object eval(Object[] args, EvaluationContext ec) {
		if (args.length == 1) {
			String s = (String)args[0];
			return s + s;
		} else if (args.length == 2) {
			double a = ((Double)args[0]).doubleValue();
			double b = ((Double)args[1]).doubleValue();

			return new Double(a * b);
		} else {
			return null;
		}
	}

	public String getName() {
		return "testfunc";
	}

	public Vector getPrototypes() {
		Vector pt = new Vector();
		Class[] p1 = {String.class};
		Class[] p2 = {Double.class, Double.class};
		pt.addElement(p1);
		pt.addElement(p2);
		return pt;
	}

	public boolean rawArgs() {
		return false;
	}

	public boolean realTime () {
		return false;
	}
}
