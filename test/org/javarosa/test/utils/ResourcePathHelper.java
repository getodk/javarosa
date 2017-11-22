package org.javarosa.test.utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ResourcePathHelper {

    private static final int CALLER_CLASS_INDEX_IN_STACKTRACE = 3;

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
        final StackTraceElement callingClass =  Thread.currentThread().getStackTrace()[CALLER_CLASS_INDEX_IN_STACKTRACE];
        final String callerPackage = callingClass.getClassName();
        return callerPackage
                .substring(0, callerPackage.lastIndexOf(".")) // strip the class name
                .replaceAll("\\.", "\\" + File.separator); // change all '.' to '/' ('\' on Windows)
    }
}
