package org.javarosa.core.util.externalizable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.javarosa.core.util.UnavailableExternalizerException;

public class ExtWrapIntEncodingSmall extends ExternalizableWrapper {
	/* max magnitude of negative number encodable in one byte; allowed range [0,254]
	 * increasing this steals from the max positive range
	 * ex.: BIAS = 0   -> [0,254] will fit in one byte; all other values will overflow
	 *      BIAS = 30  -> [-30,224]
	 *      BIAS = 254 -> [-254,0]
	 */
	public static final int DEFAULT_NEGATIVE_BIAS = 1;
	
	public int bias;
	
	/* serialization */
	
	public ExtWrapIntEncodingSmall (long l) {
		this(l, DEFAULT_NEGATIVE_BIAS);
	}

	public ExtWrapIntEncodingSmall (long l, int bias) {
		val = new Long(l);
		this.bias = bias;
	}

	/* deserialization */
	
	public ExtWrapIntEncodingSmall () {
		this(null, DEFAULT_NEGATIVE_BIAS);
	}
	
	//need the garbage param or else it conflicts with (long) constructor
	public ExtWrapIntEncodingSmall (Object ignore, int bias) {
		this.bias = bias;
	}
	
	public ExternalizableWrapper clone(Object val) {
		long l;
		
		if (val instanceof Byte) {
			l = ((Byte)val).byteValue();
		} else if (val instanceof Short) {
			l = ((Short)val).shortValue();
		} else if (val instanceof Integer) {
			l = ((Integer)val).intValue();
		} else if (val instanceof Long) {
			l = ((Long)val).longValue();
		} else if (val instanceof Character) {
			l = ((Character)val).charValue();
		} else {
			throw new ClassCastException();
		}
		
		return new ExtWrapIntEncodingSmall(l, bias);
	}
	
	public void readExternal(DataInputStream in, Vector prototypes) throws 
		IOException, UnavailableExternalizerException, IllegalAccessException, InstantiationException {
		byte b = in.readByte();
		long l;
		
		if (b == (byte)0xff) {
			l = in.readInt();
		} else {
			l = (b < 0 ? b + 256 : b) - bias;
		}
		
		val = new Long(l);
	}

	/**
	 * serialize a numeric value, only using as many bytes as needed. splits up the value into
	 * chunks of 7 bits, using as many chunks as needed to unambiguously represent the value. each
	 * chunk is serialized as a single byte, where the most-significant bit is set to 1 to indicate
	 * there are more bytes to follow, or 0 to indicate the last byte
	 **/
	public void writeExternal(DataOutputStream out) throws IOException {
		int n = ExtUtil.toInt(((Long)val).longValue());
				
		if (n >= -bias && n < 255 - bias) {
			n += bias;
			out.writeByte((byte)(n >= 128 ? n - 256 : n));
		} else {
			out.writeByte(0xff);
			out.writeInt(n);
		}
	}

	public void metaReadExternal(DataInputStream in, Vector prototypes) throws IOException {
		bias = in.readUnsignedByte();
	}

	public void metaWriteExternal(DataOutputStream out) throws IOException {
		out.writeByte((byte)bias);
	}
}
