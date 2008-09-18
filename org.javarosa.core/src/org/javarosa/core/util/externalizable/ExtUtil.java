package org.javarosa.core.util.externalizable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Vector;

import org.javarosa.core.util.Externalizable;
import org.javarosa.core.util.UnavailableExternalizerException;

public class ExtUtil {

	public static void write (DataOutputStream out, Object data) throws IOException {
		if (data instanceof Externalizable) {
			((Externalizable)data).writeExternal(out);
		} else if (data instanceof Byte) {
			writeNumeric(out, ((Byte)data).byteValue());
		} else if (data instanceof Short) {
			writeNumeric(out, ((Short)data).shortValue());
		} else if (data instanceof Integer) {
			writeNumeric(out, ((Integer)data).intValue());
		} else if (data instanceof Long) {
			writeNumeric(out, ((Long)data).longValue());
		} else if (data instanceof Character) {
			writeChar(out, ((Character)data).charValue());
		} else if (data instanceof Float) {
			writeDecimal(out, ((Float)data).floatValue());
		} else if (data instanceof Double) {
			writeDecimal(out, ((Double)data).doubleValue());
		} else if (data instanceof Boolean) {
			writeBool(out, ((Boolean)data).booleanValue());
		} else if (data instanceof String) {
			writeString(out, (String)data);
		} else if (data instanceof Date) {
			writeDate(out, (Date)data);
		} else {
			throw new RuntimeException("can't serialize");
		}
	}
	
	public static void writeNumeric (DataOutputStream out, long val) throws IOException {
		write(out, new ExtWrapIntEncodingUniform(val));
	}
	
	public static void writeChar (DataOutputStream out, char val) throws IOException {
		out.writeChar(val);
	}
	
	public static void writeDecimal (DataOutputStream out, double val) throws IOException {
		out.writeDouble(val);
	}
	
	public static void writeBool (DataOutputStream out, boolean val) throws IOException {
		out.writeBoolean(val);
	}
	
	public static void writeString (DataOutputStream out, String val) throws IOException {
		out.writeUTF(val);
	}
	
	public static void writeDate (DataOutputStream out, Date val) throws IOException {
		writeNumeric(out, val.getTime());
		//time zone?
	}
	
	public static Object read (DataInputStream in, Class type)
		throws IOException, UnavailableExternalizerException, IllegalAccessException, InstantiationException {
		if (Externalizable.class.isAssignableFrom(type)) {
			Externalizable ext = (Externalizable)type.newInstance();
			ext.readExternal(in);
			return ext;
		} else if (type == Byte.class) {
			return new Byte(readByte(in));
		} else if (type == Short.class) {
			return new Short(readShort(in));
		} else if (type == Integer.class) {
			return new Integer(readInt(in));
		} else if (type == Long.class) {
			return new Long(readNumeric(in));
		} else if (type == Character.class) {
			return new Character(readChar(in));
		} else if (type == Float.class) {
			return new Float((float)readDecimal(in));
		} else if (type == Double.class) {
			return new Double(readDecimal(in));
		} else if (type == Boolean.class) {
			return new Boolean(readBool(in));
		} else if (type == String.class) {
			return readString(in);
		} else if (type == Date.class) {
			return readDate(in);
		} else {
			throw new RuntimeException("can't deserialize");
		}
	}
	
	public static Object read (DataInputStream in, ExternalizableWrapper ew) throws
		IOException, UnavailableExternalizerException, IllegalAccessException, InstantiationException {
		return read(in, ew, null);
	}
	
	public static Object read (DataInputStream in, ExternalizableWrapper ew, Vector prototypes) throws
		IOException, UnavailableExternalizerException, IllegalAccessException, InstantiationException {
		ew.readExternal(in, ExtWrapTagged.initPrototypes(prototypes));
		return ew.val;
	}
	
	public static long readNumeric (DataInputStream in) throws
		IOException, UnavailableExternalizerException, IllegalAccessException, InstantiationException {
		return ((Long)read(in, new ExtWrapIntEncodingUniform())).longValue();
	}
	
	public static int readInt (DataInputStream in) throws
		IOException, UnavailableExternalizerException, IllegalAccessException, InstantiationException {
		return toInt(readNumeric(in));
	}

	public static short readShort (DataInputStream in) throws
		IOException, UnavailableExternalizerException, IllegalAccessException, InstantiationException {
		return toShort(readNumeric(in));
	}

	public static byte readByte (DataInputStream in) throws
		IOException, UnavailableExternalizerException, IllegalAccessException, InstantiationException {
		return toByte(readNumeric(in));
	}
	
	public static char readChar (DataInputStream in) throws IOException {
		return in.readChar();
	}
	
	public static double readDecimal (DataInputStream in) throws IOException {
		return in.readDouble();
	}
	
	public static boolean readBool (DataInputStream in) throws IOException {
		return in.readBoolean();
	}
	
	public static String readString (DataInputStream in) throws IOException {
		return in.readUTF();
	}
	
	public static Date readDate (DataInputStream in) throws
		IOException, UnavailableExternalizerException, IllegalAccessException, InstantiationException {
		return new Date(readNumeric(in));
		//time zone?
	}
	
	public static int toInt (long l) {
		if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE)
			throw new ArithmeticException("Value (" + l + ") cannot fit into int");
		return (int)l;
	}

	public static short toShort (long l) {
		if (l < Short.MIN_VALUE || l > Short.MAX_VALUE)
			throw new ArithmeticException("Value (" + l + ") cannot fit into short");
		return (short)l;
	}

	public static byte toByte (long l) {
		if (l < Byte.MIN_VALUE || l > Byte.MAX_VALUE)
			throw new ArithmeticException("Value (" + l + ") cannot fit into byte");
		return (byte)l;
	}
}
