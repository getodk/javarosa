package org.javarosa.test.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ResourcePathHelper {
    /** Makes a Path for a resource file */
    public static Path r(String filename) {
        return Paths.get("resources", filename);
    }
}
