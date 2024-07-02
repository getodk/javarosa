package org.javarosa.test;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class TempFileUtils {

    static File createTempDir(String name) {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        File subDir = new File(tempDir, name + UUID.randomUUID().toString());
        subDir.mkdir();
        return subDir;
    }

    static File createTempFile(String prefix, String suffix) {
        return createTempFile(null, prefix, suffix);
    }

    public static File createTempFile(File parent, String prefix, String suffix) {
        try {
            File tempFile = File.createTempFile(prefix, suffix, parent);
            tempFile.deleteOnExit();
            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
