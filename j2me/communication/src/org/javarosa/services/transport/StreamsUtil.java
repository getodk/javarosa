package org.javarosa.services.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamsUtil {

	public static void writeFromInputToOutput(InputStream in, OutputStream out)
			throws IOException {
		int val = in.read();
		while (val != -1) {
			out.write(val);
			val = in.read();
		}
		in.close();
		out.flush();
	}

}
