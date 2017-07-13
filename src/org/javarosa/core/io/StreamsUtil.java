package org.javarosa.core.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamsUtil {

	private StreamsUtil() {
		// private constructor
	}

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
	public static void writeFromInputToOutput(InputStream in, OutputStream out, long[] tally) throws InputIOException, OutputIOException {
		//TODO: God this is naive
		int val;
		try {
			val= in.read();
		} catch(IOException e) {
			throw new StreamsUtil().new InputIOException(e);
		}
		while (val != -1) {
			try {
				out.write(val);
			} catch(IOException e) {
				throw new StreamsUtil().new OutputIOException(e);
			}
			incr(tally);
			try {
				val = in.read();
			} catch(IOException e) {
				throw new StreamsUtil().new InputIOException(e);
			}
		}
	}
	
	public static void writeFromInputToOutputSpecific(InputStream in, OutputStream out) throws InputIOException, OutputIOException {
		writeFromInputToOutput(in, out, null);
	}
	
	public static void writeFromInputToOutput(InputStream in, OutputStream out) throws IOException {
		try {
			writeFromInputToOutput(in, out, null);
		} catch (InputIOException e) {
			throw e.internal;
		} catch (OutputIOException e) {
			throw e.internal;
		}
	}
	
	private static final int CHUNK_SIZE = 2048;
	
	/**
	 * 
	 * Write the byte array to the output stream
	 * 
	 * @param bytes
	 * @param out
	 * @throws IOException
	 */
	public static void writeToOutput(byte[] bytes, OutputStream out, long[] tally) throws IOException {
		int offset = 0;
		int remain = bytes.length;
		
		while(remain > 0) {
			int toRead = (remain < CHUNK_SIZE) ? remain : CHUNK_SIZE;
			out.write(bytes, offset, toRead);
			remain -= toRead;
			offset += toRead;
			if(tally != null) {
				tally[0] += toRead;
			}
		}
	}

	public static void writeToOutput(byte[] bytes, OutputStream out) throws IOException {
		writeToOutput(bytes, out, null);
	}
	
	private static void incr (long[] tally) {
		if (tally != null) {
			tally[0]++;
		}
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
	
	
	//Unify the functional aspects here
	
	private abstract class DirectionalIOException extends IOException{
		/**
		 * 
		 */
		private static final long serialVersionUID = -4028085294047475971L;
		
		IOException internal;

		public DirectionalIOException(IOException internal) {
			super(internal.getMessage());
			this.internal = internal;
		}
		
		public IOException getWrapped() {
			return internal;
		}
		
		public void printStackTrace()  {
			internal.printStackTrace();
		}
		
		//TODO: Override all common methodss
	}
	
	public class InputIOException extends DirectionalIOException{
		/**
		 * 
		 */
		private static final long serialVersionUID = 5939766950738216779L;

		public InputIOException(IOException internal) {
			super(internal);
		}
	}
	
    public class OutputIOException extends DirectionalIOException{
		/**
		 * 
		 */
		private static final long serialVersionUID = -1816322555490749440L;

		public OutputIOException(IOException internal) {
			super(internal);
		}
	}
}
