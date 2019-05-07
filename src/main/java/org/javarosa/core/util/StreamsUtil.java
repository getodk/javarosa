package org.javarosa.core.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <b>Warning</b>: This class is unused and should remain that way. It will be removed in a future release.
 *
 * @deprecated
 */
@Deprecated
public class StreamsUtil {

    private StreamsUtil() {
    }

    /**
     * Write everything from input stream to output stream, byte by byte then
     * close the streams
     */
    @Deprecated
    public static void writeFromInputToOutput(InputStream in, OutputStream out, long[] tally) throws IOException {
        int val = in.read();
        while (val != -1) {
            out.write(val);
            incr(tally);
            val = in.read();
        }
        in.close();
    }

    @Deprecated
    public static void writeFromInputToOutput(InputStream in, OutputStream out) throws IOException {
        writeFromInputToOutput(in, out, null);
    }

    /**
     * Write the byte array to the output stream
     */
    @Deprecated
    public static void writeToOutput(byte[] bytes, OutputStream out, long[] tally) throws IOException {

        for (int i = 0; i < bytes.length; i++) {
            out.write(bytes[i]);
            incr(tally);
        }

    }

    @Deprecated
    public static void writeToOutput(byte[] bytes, OutputStream out) throws IOException {
        writeToOutput(bytes, out, null);
    }

    private static void incr (long[] tally) {
        if (tally != null) {
            tally[0]++;
        }
    }

    /**
     * Read bytes from an input stream into a byte array then close the input
     * stream
     */
    @Deprecated
    public static byte[] readFromStream(InputStream in, int len)
            throws IOException {

        byte[] data;
        int read;
        if (len >= 0) {
            data = new byte[len];
            read = 0;
            while (read < len) {
                int k = in.read(data, read, len - read);
                if (k == -1)
                    break;
                read += k;
            }
        } else {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            while (true) {
                int b = in.read();
                if (b == -1) {
                    break;
                }
                buffer.write(b);
            }
            data = buffer.toByteArray();
            read = data.length;
        }

        if (len > 0 && read < len) {
            throw new RuntimeException("expected: " + len + " bytes but read "
                    + read);
        }

        return data;
    }

}
