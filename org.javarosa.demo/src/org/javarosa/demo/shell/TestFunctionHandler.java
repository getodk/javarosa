package org.javarosa.demo.shell;

import java.util.Vector;

import org.javarosa.xpath.IFunctionHandler;

public class TestFunctionHandler implements IFunctionHandler {

	public Object eval(Object[] args) {
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

}
