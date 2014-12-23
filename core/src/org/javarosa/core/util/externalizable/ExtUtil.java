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

import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.services.PrototypeManager;
import org.javarosa.core.util.CacheTable;
import org.javarosa.core.util.OrderedMap;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

public class ExtUtil {
	public static boolean interning = true;
	public static CacheTable<String> stringCache;
	public static byte[] serialize (Object o) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			write(new DataOutputStream(baos), o);
		} catch (IOException ioe) {
			throw new RuntimeException("IOException writing to ByteArrayOutputStream; shouldn't happen!");
		}
		return baos.toByteArray();
	}

	public static Object deserialize (byte[] data, Class type) throws DeserializationException {
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		try {
			return read(new DataInputStream(bais), type);
		} catch (EOFException eofe) {
			throw new DeserializationException("Unexpectedly reached end of stream when deserializing");
		} catch (UTFDataFormatException udfe) {
			throw new DeserializationException("Unexpectedly reached end of stream when deserializing");
		} catch (IOException e) {
			throw new RuntimeException("Unknown IOException reading from ByteArrayInputStream; shouldn't happen!");
		} finally {
			try {
				bais.close();
			} catch (IOException e) {
				//already closed. Don't sweat it
			}
		}
	}

	public static Object deserialize (byte[] data, ExternalizableWrapper ew) throws DeserializationException {
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		try {
			return read(new DataInputStream(bais), ew);
		} catch (EOFException eofe) {
			throw new DeserializationException("Unexpectedly reached end of stream when deserializing");
		} catch (UTFDataFormatException udfe) {
			throw new DeserializationException("Unexpectedly reached end of stream when deserializing");
		} catch (IOException e) {
			throw new RuntimeException("Unknown IOException reading from ByteArrayInputStream; shouldn't happen!");
		} finally {
			try {
				bais.close();
			} catch (IOException e) {
				//already closed. Don't sweat it
			}
		}
	}

	public static int getSize (Object o) {
		return serialize(o).length;
	}

	public static PrototypeFactory defaultPrototypes () {
		return PrototypeManager.getDefault();
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
		} else if (data instanceof byte[]) {
			writeBytes(out, (byte[])data);
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

	public static void writeBytes(DataOutputStream out, byte[] bytes) throws IOException {
		ExtUtil.writeNumeric(out, bytes.length);
		if (bytes.length > 0) //i think writing zero-length array might close the stream
			out.write(bytes);
	}

	//functions like these are bad; they should use the built-in list serialization facilities
	public static void writeInts(DataOutputStream out, int[] ints) throws IOException {
		ExtUtil.writeNumeric(out, ints.length);
		for(int i : ints) {
			ExtUtil.writeNumeric(out, i);
		}
	}

	public static void writeAttributes(DataOutputStream out, List<TreeElement> attributes) throws IOException {
		ExtUtil.writeNumeric(out,  attributes.size());
		for ( TreeElement e : attributes ) {
			ExtUtil.write(out, e.getNamespace());
			ExtUtil.write(out,  e.getName());
			ExtUtil.write(out, e.getAttributeValue());
		}
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
			return Integer.valueOf(readInt(in));
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
		} else if (type == byte[].class) {
			return readBytes(in);
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
		String s = in.readUTF();
		return (interning && stringCache != null) ? stringCache.intern(s) : s;
	}

	public static Date readDate (DataInputStream in) throws	IOException {
		return new Date(readNumeric(in));
		//time zone?
	}

	public static byte[] readBytes(DataInputStream in) throws IOException {
		int size = (int)ExtUtil.readNumeric(in);
		byte[] bytes = new byte[size];
		int read = 0;
		int toread = size;
		while(read != size) {
			read = in.read(bytes, 0, toread);
			toread -= read;
		}
		return bytes;
	}

	//bad
	public static int[] readInts(DataInputStream in) throws IOException {
		int size = (int)ExtUtil.readNumeric(in);
		int[] ints = new int[size];
		for(int i = 0 ; i < size ; ++i ) {
			ints[i] = (int)ExtUtil.readNumeric(in);
		}
		return ints;
	}

	public static List<TreeElement> readAttributes(DataInputStream in, TreeElement parent) throws IOException {
		int size = (int) ExtUtil.readNumeric(in);
      List<TreeElement> attributes = new ArrayList<TreeElement>(size);
		for ( int i = 0 ; i < size; ++i ) {
			String namespace = ExtUtil.readString(in);
			String name = ExtUtil.readString(in);
			String value = ExtUtil.readString(in);

			TreeElement attr = TreeElement.constructAttributeElement(namespace, name, value);
			attr.setParent(parent);
			attributes.add(attr);
		}
		return attributes;
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

	public static byte[] nullIfEmpty (byte[] ba) {
		return (ba == null ? null : (ba.length == 0 ? null : ba));
	}

	public static String nullIfEmpty (String s) {
		return (s == null ? null : (s.length() == 0 ? null : s));
	}

	public static List nullIfEmpty (List v) {
		return (v == null ? null : (v.size() == 0 ? null : v));
	}

	public static HashMap nullIfEmpty (HashMap h) {
		return (h == null ? null : (h.size() == 0 ? null : h));
	}

	public static byte[] emptyIfNull (byte[] ba) {
		return ba == null ? new byte[0] : ba;
	}

	public static String emptyIfNull (String s) {
		return s == null ? "" : s;
	}

	public static List emptyIfNull (List v) {
		return v == null ? new ArrayList(0) : v;
	}

	public static HashMap emptyIfNull (HashMap h) {
		return h == null ? new HashMap() : h;
	}

	public static Object unwrap (Object o) {
		return (o instanceof ExternalizableWrapper ? ((ExternalizableWrapper)o).baseValue() : o);
	}

	public static boolean equals (Object a, Object b) {
		if ( a == b ) {
			return true;
		}
		a = unwrap(a);
		b = unwrap(b);

		if (a == null) {
			return b == null;
		} else if (a instanceof List) {
			return (b instanceof List && listEquals((List)a, (List)b));
		} else if (a instanceof HashMap) {
			return (b instanceof HashMap && hashMapEquals((HashMap) a, (HashMap) b));
		} else {
			return a.equals(b);
		}
	}

	public static boolean listEquals (List a, List b) {
		if ( a == b ) {
			return true;
		}
		if (a.size() != b.size()) {
			return false;
		} else {
			for (int i = 0; i < a.size(); i++) {
				if (!equals(a.get(i), b.get(i))) {
					return false;
				}
			}

			return true;
		}
	}

	public static boolean arrayEquals (Object[] a, Object[] b) {
		if ( a == b ) {
			return true;
		}
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

	public static boolean hashMapEquals(HashMap a, HashMap b) {
		if ( a == b ) {
			return true;
		}
		if (a.size() != b.size()) {
			return false;
		} else if (a instanceof OrderedMap != b instanceof OrderedMap) {
			return false;
		} else {
      for (Object keyA : a.keySet()) {

				if (!equals(a.get(keyA), b.get(keyA))) {
					return false;
				}
			}

			if (a instanceof OrderedMap && b instanceof OrderedMap) {
        Iterator ea = a.keySet().iterator();
        Iterator eb = b.keySet().iterator();

				while (ea.hasNext()) {
					Object keyA = ea.next();
					Object keyB = eb.next();

					if (!keyA.equals(keyB)) { //must use built-in equals for keys, as that's what HashMap uses
						return false;
					}
				}
			}

			return true;
		}
	}

	public static String printBytes (byte[] data) {
		StringBuilder sb = new StringBuilder();
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





	//**REMOVE THESE TWO FUNCTIONS//
	//original deserialization API (whose limits made us make this whole new framework!); here for backwards compatibility
	public static void deserialize (byte[] data, Externalizable ext) throws IOException, DeserializationException {
		ext.readExternal(new DataInputStream(new ByteArrayInputStream(data)), defaultPrototypes());
	}
	public static Object deserialize (byte[] data, Class type, PrototypeFactory pf) throws IOException, DeserializationException {
        return read(new DataInputStream(new ByteArrayInputStream(data)), type, pf);
	}
	////

	public static void attachCacheTable(CacheTable<String> stringCache) {
		ExtUtil.stringCache = stringCache;
	}
}
