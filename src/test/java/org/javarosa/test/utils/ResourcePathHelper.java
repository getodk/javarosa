package org.javarosa.test.utils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import static java.nio.file.Files.isRegularFile;
import static java.nio.file.Files.walk;
import static java.util.stream.Collectors.toSet;

public class ResourcePathHelper {
    private static Set<Path> resourcePathsCache = buildCache();

    private static synchronized Set<Path> buildCache() {
        try {
            URI uri = ResourcePathHelper.class.getResource("/logback-test.xml.example").toURI();
            Path root = Paths.get(uri).getParent();
            return walk(root)
                .filter(p -> isRegularFile(p))
                .collect(toSet());
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Provides the path to the first filename match in a depth-first traversal starting
     * at the test resource root.
     *
     * @param filename the file name for which to create a path
     * @return a Path for the resource file
     */
    public static Path r(String filename) {
        if (resourcePathsCache == null)
            throw new RuntimeException("Too fast! The resources cache hasn't been built yet! Don't use r() within static members!");
        return resourcePathsCache.stream()
            .filter(p -> p.endsWith(filename))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("File " + filename + " not found among files in resources"));
    }
}