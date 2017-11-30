package org.javarosa.test.utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ResourcePathHelper {

    /** Makes a Path for a resource file */
    public static Path r(String filename) {
        final String resourceFileParentPath = inferResourceFileParentPath();
        final Path resourceFilePath = Paths.get("resources", resourceFileParentPath, filename);

        if (resourceFilePath.toFile().exists()) {
            return resourceFilePath;
        } else { // try to find the file in the resources root directory
            return Paths.get("resources", filename);
        }
    }

    /**
     * If the class that called {@link ResourcePathHelper#r(String)} is {@link org.javarosa.core.model.Safe2014DagImpl}
     * then this method will return "org/javarosa/core/model"
     * @return Caller test class package as a path
     */
    private static String inferResourceFileParentPath() {
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        int callerStackIndex = 2; // 0 is getStackTrace, 1 is this method, 2 is immediate caller
        while (callerStackIndex < stackTrace.length &&
                stackTrace[callerStackIndex].getClassName().equals(ResourcePathHelper.class.getName())) {
            ++callerStackIndex;
        }
        final String callerClassName = stackTrace[callerStackIndex].getClassName();
        return callerClassName
                .substring(0, callerClassName.lastIndexOf(".")) // strip the class name
                .replace(".", File.separator);  // change all '.' to '/' ('\' on Windows)
    }
}
