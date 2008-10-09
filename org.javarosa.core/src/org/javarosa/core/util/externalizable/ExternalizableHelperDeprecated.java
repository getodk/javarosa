package org.javarosa.core.util.externalizable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.javarosa.core.util.Map;
import org.javarosa.core.util.OrderedHashtable;


/*
 * DEPRECATED -- DO NOT USE THIS CLASS!! Use ExtUtil.* / ExtWrap* instead.
 * These functions are not just wrappers around the new serialization framework;
 * they are here for backwards compatibility only!
 */

public class ExternalizableHelperDeprecated {
	public static final int ENCODING_NUM_DEFAULT = 1; //equal performance for entire range of longs
	
	public static void writeNumeric (DataOutputStream dos, long l, int encoding) throws IOException {		
		switch (encoding) {
		case ENCODING_NUM_DEFAULT: writeNumDefault(dos, l); break;
		default: throw new IllegalStateException("Unrecognized numeric encoding");
		}
	}
	
	public static long readNumeric (DataInputStream dis, int encoding) throws IOException {
		switch (encoding) {
		case ENCODING_NUM_DEFAULT: return readNumDefault(dis);
		default: throw new IllegalStateException("Unrecognized numeric encoding");
		}
	}
	
	public static int readNumInt (DataInputStream dis, int encoding) throws IOException {
		return ExtUtil.toInt(readNumeric(dis, encoding));
		
//		long l = readNumeric(dis, encoding);
//		if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE)
//			throw new ArithmeticException("Deserialized value (" + l + ") cannot fit into int");
//		return (int)l;
	}
	
	/**
	 * serialize a numeric value, only using as many bytes as needed. splits up the value into
	 * chunks of 7 bits, using as many chunks as needed to unambiguously represent the value. each
	 * chunk is serialized as a single byte, where the most-significant bit is set to 1 to indicate
	 * there are more bytes to follow, or 0 to indicate the last byte
	 **/
	public static void writeNumDefault (DataOutputStream dos, long l) throws IOException {
		ExtUtil.writeNumeric(dos, l);
		
//		int sig = -1;
//		long k;
//		do {
//			sig++;
//			k = l >> (sig * 7);
//		} while (k < (-1 << 6) || k > (1 << 6) - 1); //[-64,63] -- the range we can fit into one byte
//			
//		for (int i = sig; i >= 0; i--) {
//			byte chunk = (byte)((l >> (i * 7)) & 0x7f);
//			dos.writeByte((i > 0 ? 0x80 : 0x00) | chunk);
//		}
	}

	/**
	 * deserialize a numeric value stored in a variable number of bytes. see writeNumeric
	 **/
	public static long readNumDefault (DataInputStream dis) throws IOException {
		return ExtUtil.readNumeric(dis);
		
//		long l = 0;
//		byte b;
//		boolean firstByte = true;
//		
//		do {
//			b = dis.readByte();
//			
//			if (firstByte) {
//				firstByte = false;
//				l = (((b >> 6) & 0x01) == 0 ? 0 : -1); //set initial sign
//			}
//			
//			l = (l << 7) | (b & 0x7f);
//		} while (((b >> 7) & 0x01) == 1);
//		
//		return l;
	}
		
	/**
	 * Writes a string to the stream.
	 * 
	 * @param dos - the stream for writing.
	 * @param data - the string to write.
	 * @throws IOException - thrown when a problem occurs during the writing to stream.
	 */
	public static void writeUTF(DataOutputStream dos,String data) throws IOException{
		ExtUtil.write(dos, new ExtWrapNullable(data));
			
//		if(data != null){
//			dos.writeBoolean(true);
//			dos.writeUTF(data);
//		}
//		else
//			dos.writeBoolean(false);
	}
	/**
	 * Writes a small (byte size) vector of UTF objects to a stream.
	 * 
	 * @param utfVector - the vector of UTF objects.
	 * @param dos - the stream to write to.
	 * @throws IOException - thrown when a problem occurs during the writing to stream.
	 */
	public static void writeUTFs(Vector utfVector, DataOutputStream dos) throws IOException {
		ExtUtil.write(dos, new ExtWrapList(utfVector == null ? new Vector() : utfVector));
		
//		if(utfVector != null){
//			dos.writeByte(utfVector.size());
//			for(int i=0; i<utfVector.size(); i++ ){
//				writeUTF(dos, ((String)utfVector.elementAt(i)));
//			}
//		}
//		else
//			dos.writeByte(0);
	}
	
	/**
	 * reads a small vector (byte size) of UTF objects from a stream.
	 * 
	 * @param dis - the stream to be read from
	 * @throws IOException - thrown when a problem occurs during the writing to stream.
	 * 
	 * @return A vector of UTF objects
	 */
	public static Vector readUTFs(DataInputStream dis) throws IOException {
		try {
			Vector v = (Vector)ExtUtil.read(dis, new ExtWrapList(String.class));
			return v.size() == 0 ? null : v;
		} catch (DeserializationException de) {
			throw new RuntimeException("shouldn't happen");
		}
		
//		byte len = dis.readByte();
//		if(len == 0)
//			return null;
//		
//		Vector utfVector = new Vector();
//		
//		for(byte i=0; i<len; i++ ) {
//			utfVector.addElement(readUTF(dis));
//		}
//		
//		return utfVector;
	}	
	
	/**
	 * Writes an Integer to the stream.
	 * 
	 * @param dos - the stream for writing.
	 * @param data - the Interger to write.
	 * @throws IOException - thrown when a problem occurs during the writing to stream.
	 */
	public static void writeInteger(DataOutputStream dos,Integer data) throws IOException{
		ExtUtil.write(dos, new ExtWrapNullable(data));
		
//		if(data != null){
//			dos.writeBoolean(true);
//			dos.writeInt(data.intValue());
//		}
//		else
//			dos.writeBoolean(false);
	} 
	
	/**
	 * Writes a Date to a stream.
	 * 
	 * @param dos - the stream to write to.
	 * @param data - the Date to write.
	 * @throws IOException - thrown when a problem occurs during the writing to stream.
	 */
	public static void writeDate(DataOutputStream dos,Date data) throws IOException{
		ExtUtil.write(dos, new ExtWrapNullable(data));
		
//		if(data != null){
//			dos.writeBoolean(true);
//			dos.writeLong(data.getTime());
//		}
//		else
//			dos.writeBoolean(false);
	} 
	
	/**
	 * Reads a string from a stream.
	 * 
	 * @param dis - the stream to read from.
	 * @return - the read string or null if none.
	 * @throws IOException - thrown when a problem occurs during the reading from stream.
	 */
	public static String readUTF(DataInputStream dis) throws IOException {
		try {
			return (String)ExtUtil.read(dis, new ExtWrapNullable(String.class));
		} catch (DeserializationException de) {
			throw new RuntimeException("shouldn't happen");
		}
			
//		if(dis.readBoolean())
//			return dis.readUTF();
//		return null;
	}
	
	/**
	 * Reads an Integer from a stream.
	 * 
	 * @param dis - the stream to read from.
	 * @return - the read Integer or null of none.
	 * @throws IOException - thrown when a problem occurs during the reading from stream.
	 */
	public static Integer readInteger(DataInputStream dis) throws IOException {
		try {
			return (Integer)ExtUtil.read(dis, new ExtWrapNullable(Integer.class));
		} catch (DeserializationException de) {
			throw new RuntimeException("shouldn't happen");
		}
			
//		if(dis.readBoolean())
//			return new Integer(dis.readInt());
//		return null;
	}
	
	/**
	 * Reads a Date from a stream.
	 * 
	 * @param dis - the stream to read from.
	 * @return - the read Date or null if none.
	 * @throws IOException - thrown when a problem occurs during the reading from stream.
	 */
	public static Date readDate(DataInputStream dis) throws IOException {
		try {
			return (Date)ExtUtil.read(dis, new ExtWrapNullable(Date.class));
		} catch (DeserializationException de) {
			throw new RuntimeException("shouldn't happen");
		}
			
//		if(dis.readBoolean())
//			return new Date(dis.readLong());
//		return null;
	}
	
	/**
	 * Writes a small vector (byte size) of Externalizable objects to a stream.
	 * 
	 * @param externalizableVector - the vector of externalizable objects.
	 * @param dos - the stream to write to.
	 * @throws IOException - thrown when a problem occurs during the writing to stream.
	 */
	public static void writeExternal(Vector externalizableVector, DataOutputStream dos) throws IOException {	
		ExtUtil.write(dos, new ExtWrapList(externalizableVector == null ? new Vector() : externalizableVector));
		
//		if(externalizableVector != null){
//			dos.writeByte(externalizableVector.size());
//			for(int i=0; i<externalizableVector.size(); i++ ){
//				((Externalizable)externalizableVector.elementAt(i)).writeExternal(dos);
//			}
//		}
//		else
//			dos.writeByte(0);
	}
	
	public static Map readExternalStringValueMap(DataInputStream in, Class classname) throws IOException, DeserializationException {
		Hashtable h = (Hashtable)ExtUtil.read(in, new ExtWrapMap(String.class, classname));
		
		Map m = new Map();
		for (Enumeration e = h.keys(); e.hasMoreElements(); ) {
			String key = (String)e.nextElement();
			Externalizable value = (Externalizable)h.get(key);
			m.put(key, value);
		}
		return m;
		
//		Map theMap = new Map();
//		int sizes = in.readInt();
//		for(int i = 0 ; i < sizes ; i++) {
//			String key = in.readUTF();
//			Externalizable value = (Externalizable)classname.newInstance();
//			value.readExternal(in);
//			theMap.put(key, value);
//		}
//		return theMap;
	}

	public static void writeExternalStringValueMap(DataOutputStream out, Map map) throws IOException {
		Hashtable h = new Hashtable();
		for (Enumeration e = map.keys(); e.hasMoreElements(); ) {
			String key = (String)e.nextElement();
			Externalizable value = (Externalizable)map.get(key);
			h.put(key, value);
		}
		
		ExtUtil.write(out, new ExtWrapMap(h));

//		out.writeInt(map.size());
//		
//		Enumeration en = map.keys();
//		while(en.hasMoreElements()) {
//			String key = (String)en.nextElement();
//			out.writeUTF(key);
//			((Externalizable)map.get(key)).writeExternal(out);
//		}
	}
	
	/**
	 * Writes a big vector (of int size) of externalizable objects from a stream.
	 * 
	 * @param externalizableVector
	 * @param dos
	 * @throws IOException
	 */
	public static void writeBig(Vector externalizableVector, DataOutputStream dos) throws IOException {	
		ExtUtil.write(dos, new ExtWrapList(ExtUtil.emptyIfNull(externalizableVector)));
		
//		if(externalizableVector != null){
//			dos.writeInt(externalizableVector.size());
//			for(int i=0; i<externalizableVector.size(); i++ ){
//				((Externalizable)externalizableVector.elementAt(i)).writeExternal(dos);
//			}
//		}
//		else
//			dos.writeInt(0);
	}
	
	/**
	 * Reads a small vector (byte size) of externalizable objects of a certain class from a stream.
	 * 
	 * @param dis - the stream to read from.
	 * @param cls - the class of the externalizable objects contained in the vector.
	 * @return - the Vector of externalizable objets or null if none.
	 * @throws IOException - thrown when a problem occurs during the reading from stream.
	 * @throws InstantiationException - thrown when a problem occurs during the peristent object creation.
	 * @throws IllegalAccessException - thrown when a problem occurs when setting values of the externalizable object.
	 */
	public static Vector readExternal(DataInputStream dis, Class cls) throws IOException, DeserializationException {
		return ExtUtil.nullIfEmpty((Vector)ExtUtil.read(dis, new ExtWrapList(cls)));
		
//		byte len = dis.readByte();
//		if(len == 0)
//			return null;
//
//		Vector externalizableVector = new Vector();
//		
//		for(byte i=0; i<len; i++ ){
//			Object obj = (Externalizable)cls.newInstance();
//			((Externalizable)obj).readExternal(dis);
//			externalizableVector.addElement(obj);
//		}
//		
//		return externalizableVector;
	}
	
	/**
	 * Reads a big vector (with int size) of a externalizable class from stream.
	 * 
	 * @param dis
	 * @param cls
	 * @return
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static Vector readBig(DataInputStream dis, Class cls) throws IOException, DeserializationException {
		return ExtUtil.nullIfEmpty((Vector)ExtUtil.read(dis, new ExtWrapList(cls)));
		
//		int len = dis.readInt();
//		if(len == 0)
//			return null;
//
//		Vector externalizableVector = new Vector();
//		
//		for(int i=0; i<len; i++ ){
//			Object obj = (Externalizable)cls.newInstance();
//			((Externalizable)obj).readExternal(dis);
//			externalizableVector.addElement(obj);
//		}
//		
//		return externalizableVector;
	}
					
	//useless function
	public static boolean isEOF(DataInputStream dis){
		return false;
	}
}
