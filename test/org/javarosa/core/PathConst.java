package org.javarosa.core;

import java.io.File;

public class PathConst {

	public static final File getTestResourcePath() {
		String path = System.getProperty("java.class.path");
		String[] parts = path.split(";");
		File base = null;
		for ( String p : parts ) {
			File f = new File(p);
			if ( f.getName().equals("bin") && f.getParentFile().getName().equals("core") ) {
				base = f.getParentFile();
			}
		}
		return new File(base, "resources");
	}
}
