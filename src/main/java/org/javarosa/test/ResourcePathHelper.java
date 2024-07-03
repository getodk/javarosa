package org.javarosa.test;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class ResourcePathHelper {
    private static Set<File> resourcePathsCache = buildCache();

    private static synchronized Set<File> buildCache() {
        File root = new File(ResourcePathHelper.class.getResource("/logback-test.xml.example").getFile()).getParentFile();
        Collection<File> files = FileUtils.listFiles(root, null, true);
        return files.stream()
            .collect(Collectors.toSet());
    }

    /**
     * Provides the path to the first filename match in a depth-first traversal starting
     * at the test resource root.
     *
     * @param filename the file name for which to create a path
     * @return a Path for the resource file
     */
    public static File r(String filename) {
        if (resourcePathsCache == null)
            throw new RuntimeException("Too fast! The resources cache hasn't been built yet! Don't use r() within static members!");
        return resourcePathsCache.stream()
            .filter(f -> f.getAbsolutePath().endsWith(File.separator + filename))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("File " + filename + " not found among files in resources"));
    }
}
