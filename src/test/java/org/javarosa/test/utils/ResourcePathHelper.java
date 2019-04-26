package org.javarosa.test.utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ResourcePathHelper {

    /**
     * Makes a Path for a resource file either in a directory corresponding to the test class’s package, or
     * in the resources directory. Automated tests generate some files dynamically, so the Paths created here
     * aren’t always for existing files.
     *
     * @param filename the file name for which to create a path
     * @return a Path for the resource file
     */
    public static Path r(String filename) {
        return r(filename, true);
    }

    /**
     * Makes a Path for a resource file either in a directory corresponding to the test class’s package, or
     * in the resources directory. Automated tests generate some files dynamically, so the Paths created here
     * aren’t always for existing files.
     *
     * @param filename the file name for which to create a path
     * @param fallBack whether to “fall back” to the resources directory if the file is not in the
     *                 class’s corresponding directory
     * @return a Path for the resource file
     */
    public static Path r(String filename, boolean fallBack) {
        final String resourceFileParentPath = inferResourceFileParentPath();
        String prefix = !filename.startsWith("/") ? "src/test/resources" : "";

        final Path resourceFilePath = Paths.get(prefix, resourceFileParentPath, filename);

        if (!fallBack || resourceFilePath.toFile().exists()) {
            return resourceFilePath;
        }
        return Paths.get(prefix, filename);
    }

    /**
     * If the class that called {@link ResourcePathHelper#r(String)} is {@link org.javarosa.core.model.Safe2014DagImpl}
     * then this method will return "org/javarosa/core/model"
     *
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
