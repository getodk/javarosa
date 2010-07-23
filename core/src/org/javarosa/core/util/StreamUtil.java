/**
 * 
 */
package org.javarosa.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author ctsims
 *
 */
public class StreamUtil {
	private static final int CHUNK_SIZE = 64;
	
	public static void transfer(InputStream input, OutputStream output) throws IOException {
        byte[] b = new byte[CHUNK_SIZE];
        int read;
        while ((read = input.read(b)) != -1) {
        	output.write(b, 0, read);
        }
	}
}
