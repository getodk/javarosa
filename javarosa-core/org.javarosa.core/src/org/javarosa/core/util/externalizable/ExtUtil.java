/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.core.util.externalizable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.util.OrderedHashtable;

public class ExtUtil {
	public static byte[] serialize (Object o) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		write(new DataOutputStream(baos), o);
		return baos.toByteArray();
	}

	//original deserialization API (whose limits made us make this whole new framework!); here for backwards compatibility
	public static void deserialize (byte[] data, Externalizable ext) throws IOException, DeserializationException {
		ext.readExternal(new DataInputStream(new ByteArrayInputStream(data)), defaultPrototypes());
	}
		
	public static Object deserialize (byte[] data, Class type) throws IOException, DeserializationException {
		return deserialize(data, type, null);
	}
	
	public static Object deserialize (byte[] data, Class type, PrototypeFactory pf) throws IOException, DeserializationException {
		return read(new DataInputStream(new ByteArrayInputStream(data)), type, pf);
	}
	
	public static int getSize (Object o) {
		try {
			return serialize(o).length;
		} catch (Exception e) {
			return -1;
		}
	}
	
	public static PrototypeFactory defaultPrototypes () {
		return new PrototypeFactory(JavaRosaServiceProvider.instance().getPrototypes());
	}
	
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
			throw new ClassCastException("Not a serializable datatype: " + data.getClass().getName());
		}
	}
	
	public static void writeNumeric (DataOutputStream out, long val) throws IOException {
		writeNumeric(out, val, new ExtWrapIntEncodingUniform());
	}
	
	public static void writeNumeric (DataOutputStream out, long val, ExtWrapIntEncoding encoding) throws IOException {
		write(out, encoding.clone(new Long(val)));
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
		//we could easily come up with more efficient default encoding for string
	}
	
	public static void writeDate (DataOutputStream out, Date val) throws IOException {
		writeNumeric(out, val.getTime());
		//time zone?
	}
	
	public static Object read (DataInputStream in, Class type) throws IOException, DeserializationException {
		return read(in, type, null);
	}
	
	public static Object read (DataInputStream in, Class type, PrototypeFactory pf) throws IOException, DeserializationException {
		if (Externalizable.class.isAssignableFrom(type)) {
			Externalizable ext = (Externalizable)PrototypeFactory.getInstance(type);
			ext.readExternal(in, pf == null ? defaultPrototypes() : pf);
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
			throw new ClassCastException("Not a deserializable datatype: " + type.getName());
		}
	}
	
	public static Object read (DataInputStream in, ExternalizableWrapper ew) throws	IOException, DeserializationException {
		return read(in, ew, null);
	}
	
	public static Object read (DataInputStream in, ExternalizableWrapper ew, PrototypeFactory pf) throws IOException, DeserializationException {
		ew.readExternal(in, pf == null ? defaultPrototypes() : pf);
		return ew.val;
	}
	
	public static long readNumeric (DataInputStream in) throws IOException {
		return readNumeric(in, new ExtWrapIntEncodingUniform());
	}

	public static long readNumeric (DataInputStream in, ExtWrapIntEncoding encoding) throws IOException {
		try {
			return ((Long)read(in, encoding)).longValue();
		} catch (DeserializationException de) {
			throw new RuntimeException("Shouldn't happen: Base-type encoding wrappers should never touch prototypes");
		}
	}	
	
	public static int readInt (DataInputStream in) throws IOException {
		return toInt(readNumeric(in));
	}

	public static short readShort (DataInputStream in) throws IOException {
		return toShort(readNumeric(in));
	}

	public static byte readByte (DataInputStream in) throws	IOException {
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
	
	public static Date readDate (DataInputStream in) throws	IOException {
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
	
	public static long toLong (Object o) {
		if (o instanceof Byte) {
			return ((Byte)o).byteValue();
		} else if (o instanceof Short) {
			return ((Short)o).shortValue();
		} else if (o instanceof Integer) {
			return ((Integer)o).intValue();
		} else if (o instanceof Long) {
			return ((Long)o).longValue();
		} else if (o instanceof Character) {
			return ((Character)o).charValue();
		} else {
			throw new ClassCastException();
		}
	}

	public static String nullIfEmpty (String s) {
		return (s == null ? null : (s.length() == 0 ? null : s));
	}
	
	public static Vector nullIfEmpty (Vector v) {
		return (v == null ? null : (v.size() == 0 ? null : v));
	}
	
	public static Hashtable nullIfEmpty (Hashtable h) {
		return (h == null ? null : (h.size() == 0 ? null : h));
	}
	
	public static String emptyIfNull (String s) {
		return s == null ? "" : s;
	}
	
	public static Vector emptyIfNull (Vector v) {
		return v == null ? new Vector() : v;
	}

	public static Hashtable emptyIfNull (Hashtable h) {
		return h == null ? new Hashtable() : h;
	}	
	
	public static Object unwrap (Object o) {
		return (o instanceof ExternalizableWrapper ? ((ExternalizableWrapper)o).baseValue() : o);
	}
	
	public static boolean equals (Object a, Object b) {
		a = unwrap(a);
		b = unwrap(b);
		
		if (a == null) {
			return b == null;
		} else if (a instanceof Vector) {
			return (b instanceof Vector && vectorEquals((Vector)a, (Vector)b));
		} else if (a instanceof Hashtable) {
			return (b instanceof Hashtable && hashtableEquals((Hashtable)a, (Hashtable)b));
		} else {
			return a.equals(b);
		}		
	}
	
	public static boolean vectorEquals (Vector a, Vector b) {
		if (a.size() != b.size()) {
			return false;
		} else {
			for (int i = 0; i < a.size(); i++) {
				if (!equals(a.elementAt(i), b.elementAt(i))) {
					return false;
				}
			}
			
			return true;
		}
	}
	
	public static boolean arrayEquals (Object[] a, Object[] b) {
		if (a.length != b.length) {
			return false;
		} else {
			for (int i = 0; i < a.length; i++) {
				if (!equals(a[i], b[i])) {
					return false;
				}
			}
			
			return true;
		}
	}
	
	public static boolean hashtableEquals (Hashtable a, Hashtable b) {
		if (a.size() != b.size()) {
			return false;
		} else if (a instanceof OrderedHashtable != b instanceof OrderedHashtable) {
			return false;
		} else {
			for (Enumeration ea = a.keys(); ea.hasMoreElements(); ) {
				Object keyA = ea.nextElement();

				if (!equals(a.get(keyA), b.get(keyA))) {
					return false;
				}
			}
			
			if (a instanceof OrderedHashtable && b instanceof OrderedHashtable) {
				Enumeration ea = a.keys();
				Enumeration eb = b.keys();
				
				while (ea.hasMoreElements()) {
					Object keyA = ea.nextElement();
					Object keyB = eb.nextElement();
					
					if (!keyA.equals(keyB)) { //must use built-in equals for keys, as that's what hashtable uses
						return false;
					}
				}
			}
			
			return true;
		}
	}
	
	public static String printBytes (byte[] data) {
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		for (int i = 0; i < data.length; i++) {
			String hex = Integer.toHexString(data[i]);
			if (hex.length() == 1)
				hex = "0" + hex;
			else
				hex = hex.substring(hex.length() - 2);
			sb.append(hex);
			if (i < data.length - 1) {
				if ((i + 1) % 30 == 0)
					sb.append("\n ");
				else if ((i + 1) % 10 == 0)
					sb.append("  ");
				else
					sb.append(" ");
			}
		}
		sb.append("]");
		return sb.toString();
	}
}
