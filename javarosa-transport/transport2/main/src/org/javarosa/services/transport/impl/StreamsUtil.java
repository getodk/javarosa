package org.javarosa.services.transport.impl;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamsUtil {

	/**
	 * 
	 * Write everything from input stream to output stream, byte by byte then
	 * close the streams
	 * 
	 * 
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	public static void writeFromInputToOutput(InputStream in, OutputStream out)
			throws IOException {
		int val = in.read();
		while (val != -1) {
			out.write(val);
			val = in.read();
		}

	}

	/**
	 * 
	 * Write the byte array to the output stream
	 * 
	 * @param bytes
	 * @param out
	 * @throws IOException
	 */
	public static void writeToOutput(byte[] bytes, OutputStream out)
			throws IOException {

		for (int i = 0; i < bytes.length; i++) {
			out.write(bytes[i]);
		}

		// out.flush();

	}

	/**
	 * 
	 * Read bytes from an input stream into a byte array then close the input
	 * stream
	 * 
	 * @param in
	 * @param len
	 * @return
	 * @throws IOException
	 */
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
			// System.out.println("WARNING: expected " + len + "!!");
			throw new RuntimeException("expected: " + len + " bytes but read "
					+ read);
		}
		// replyS
		// System.out.println(new String(data, "UTF-8"));

		return data;
	}

	public static byte[] readFromStream(DataInputStream in) throws IOException {
		byte[] data = null;

		StringBuffer message = new StringBuffer();

		try {
			int n = in.readByte();

			if (n != -1 && n > 0) {

				for (int x = 0; x < n; x++) {

					message.append(in.readUTF());
				}
			}

			data = message.toString().getBytes();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return data;

	}

}
