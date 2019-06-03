package org.javarosa.benchmarks.utils;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/** Methods for reading from and writing to the FormDef cache */
public class FormDefCache {

    static final Logger logger = LoggerFactory.getLogger(FormDefCache.class);
    static int bufSize = 16 * 1024; // May be set by unit test

    private FormDefCache() {
        // Private constructor
    }

    /**
     * Serializes a FormDef and saves it in the cache. To avoid problems from two callers
     * trying to cache the same file at the same time, we serialize into a temporary file,
     * and rename it when done.
     *
     * @param formDef  - The FormDef to be cached
     * @param formPath - The form XML file
     */
    public static void writeCache(FormDef formDef, String formPath, String cachePath) throws IOException {

        final long formSaveStart = System.currentTimeMillis();
        File cachedFormDefFile = FormDefCache.getCacheFile(new File(formPath), cachePath);
        final File tempCacheFile = File.createTempFile("cache", null,
            new File(cachePath));
        logger.info(String.format("Started saving %s to the cache via temp file %s",
            formDef.getTitle(), tempCacheFile.getName()));

        Exception caughtException = null;
        try {
            DataOutputStream dos = new DataOutputStream(new FileOutputStream(tempCacheFile));
            formDef.writeExternal(dos);
            dos.close();
        } catch (IOException exception) {
            caughtException = exception;
        }

        final boolean tempFileNeedsDeleting = caughtException != null; // There was an error creating it

        // Delete or rename the temp file
        if (tempFileNeedsDeleting) {
            logger.info(String.format("Deleting no-longer-wanted temp cache file %s for form %s",
                tempCacheFile.getName(), formDef.getTitle()));
            if (!tempCacheFile.delete()) {
                logger.error("Unable to delete " + tempCacheFile.getName());
            }
        } else {
            if (tempCacheFile.renameTo(cachedFormDefFile)) {
                logger.info(String.format("Renamed %s to %s",
                    tempCacheFile.getName(), cachedFormDefFile.getName()));
                logger.info(String.format("Caching %s took %.3f seconds.", formDef.getTitle(),
                    (System.currentTimeMillis() - formSaveStart) / 1000F));
            } else {
                logger.error("Unable to rename temporary file %s to cache file %s",
                    tempCacheFile.toString(), cachedFormDefFile.toString());
            }
        }

        if (caughtException != null) { // The client is no longer there, so log the exception
            logger.error(caughtException.getMessage(), caughtException);
        }
    }


    /**
     * If a form is present in the cache, deserializes and returns it as as FormDef.
     * @param formXml a File containing the XML version of the form
     * @return a FormDef, or null if the form is not present in the cache
     */
    public static FormDef readCache(File formXml, String cachePath) {
        final File cachedForm = getCacheFile(formXml, cachePath);
        if (cachedForm.exists()) {
            logger.info("Attempting to load %s from cached file: %s.",
                formXml.getName(), cachedForm.getName());
            final long start = System.currentTimeMillis();
            final FormDef deserializedFormDef = deserializeFormDef(cachedForm);
            if (deserializedFormDef != null) {
                logger.info("Loaded in %.3f seconds.", (System.currentTimeMillis() - start) / 1000F);
                return deserializedFormDef;
            }

            // An error occurred with deserialization. Remove the file, and make a
            // new .formdef from xml.
            logger.warn(String.format("Deserialization FAILED! Deleting cache file: %s",
                cachedForm.getAbsolutePath()));
            cachedForm.delete();
        }
        return null;
    }

    /**
     * Builds and returns a File object for the cached version of a form.
     * @param formXml the File containing the XML form
     * @return a File object
     */
    private static File getCacheFile(File formXml, String cachePath) {
        return new File( cachePath + File.separator +
            getMd5Hash(formXml) + ".formdef");
    }

    private static FormDef deserializeFormDef(File serializedFormDef) {
        FileInputStream fis;
        FormDef fd;
        try {
            // create new form def
            fd = new FormDef();
            fis = new FileInputStream(serializedFormDef);
            DataInputStream dis = new DataInputStream(fis);

            // read serialized formdef into new formdef
            fd.readExternal(dis, ExtUtil.defaultPrototypes());
            dis.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            fd = null;
        }

        return fd;
    }


    public static String getMd5Hash(File file) {
        final InputStream is;
        try {
            is = new FileInputStream(file);

        } catch (FileNotFoundException e) {
            logger.debug(String.format("Cache file %s not found", file.getAbsolutePath()), e);
            return null;

        }

        return getMd5Hash(is);
    }


    public static String getMd5Hash(InputStream is) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            final byte[] buffer = new byte[bufSize];

            while (true) {
                int result = is.read(buffer, 0, bufSize);
                if (result == -1) {
                    break;
                }
                md.update(buffer, 0, result);
            }

            StringBuilder md5 = new StringBuilder(new BigInteger(1, md.digest()).toString(16));
            while (md5.length() < 32) {
                md5.insert(0, "0");
            }

            is.close();
            return md5.toString();

        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage(), e);
            return null;

        } catch (IOException e) {
            logger.error( "Problem reading file.", e);
            return null;
        }
    }
}
