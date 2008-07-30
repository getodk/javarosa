package org.javarosa.core.model.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.javarosa.core.services.storage.utilities.Externalizable;
import org.javarosa.core.services.storage.utilities.UnavailableExternalizerException;



/**
 * Helper class to write and read collection to and from streams. This class also
 * writes the built in types taking care of nulls if any.
 * 
 * @author Daniel Kayiwa
 *
 */
public class ExternalizableHelper {
	public static final int ENCODING_NUM_DEFAULT = 1; //equal performance for entire range of longs
	public static final int ENCODING_NUM_STINGY = 2;  //highly optimized for [0,254], ints only
	
	/* max magnitude of negative number encodable in one byte; allowed range [0,254]
	 * increasing this steals from the max positive range
	 * ex.: BIAS = 0   -> [0,254] will fit in one byte; all other values will overflow
	 *      BIAS = 30  -> [-30,224]
	 *      BIAS = 254 -> [-254,0]
	 */
	private static final int STINGY_NEGATIVE_BIAS = 0;
	
	public static void writeNumeric (DataOutputStream dos, long l, int encoding) throws IOException {
		switch (encoding) {
		case ENCODING_NUM_DEFAULT: writeNumDefault(dos, l); break;
		case ENCODING_NUM_STINGY: writeNumStingy(dos, l); break;
		default: throw new IllegalStateException("Unrecognized numeric encoding");
		}
	}
	
	public static long readNumeric (DataInputStream dis, int encoding) throws IOException {
		switch (encoding) {
		case ENCODING_NUM_DEFAULT: return readNumDefault(dis);
		case ENCODING_NUM_STINGY: return readNumStingy(dis);
		default: throw new IllegalStateException("Unrecognized numeric encoding");
		}
	}
	
	public static int readNumInt (DataInputStream dis, int encoding) throws IOException {
		long l = readNumeric(dis, encoding);
		if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE)
			throw new ArithmeticException("Deserialized value (" + l + ") cannot fit into int");
		return (int)l;
	}

	public static short readNumShort (DataInputStream dis, int encoding) throws IOException {
		long l = readNumeric(dis, encoding);
		if (l < Short.MIN_VALUE || l > Short.MAX_VALUE)
			throw new ArithmeticException("Deserialized value (" + l + ") cannot fit into short");
		return (short)l;
	}

	public static byte readNumByte (DataInputStream dis, int encoding) throws IOException {
		long l = readNumeric(dis, encoding);
		if (l < Byte.MIN_VALUE || l > Byte.MAX_VALUE)
			throw new ArithmeticException("Deserialized value (" + l + ") cannot fit into byte");
		return (byte)l;
	}	
	
	/*
	 * serialize a numeric value, only using as many bytes as needed. splits up the value into
	 * chunks of 7 bits, using as many chunks as needed to unambiguously represent the value. each
	 * chunk is serialized as a single byte, where the most-significant bit is set to 1 to indicate
	 * there are more bytes to follow, or 0 to indicate the last byte
	 */
	public static void writeNumDefault (DataOutputStream dos, long l) throws IOException {		
		int sig = -1;
		long k;
		do {
			sig++;
			k = l >> (sig * 7);
		} while (k < (-1 << 6) || k > (1 << 6) - 1); //[-64,63] -- the range we can fit into one byte
			
		for (int i = sig; i >= 0; i--) {
			byte chunk = (byte)((l >> (i * 7)) & 0x7f);
			dos.writeByte((i > 0 ? 0x80 : 0x00) | chunk);
		}
	}

	/*
	 * deserialize a numeric value stored in a variable number of bytes. see writeNumeric
	 */
	public static long readNumDefault (DataInputStream dis) throws IOException {
		long l = 0;
		byte b;
		boolean firstByte = true;
		
		do {
			b = dis.readByte();
			
			if (firstByte) {
				firstByte = false;
				l = (((b >> 6) & 0x01) == 0 ? 0 : -1); //set initial sign
			}
			
			l = (l << 7) | (b & 0x7f);
		} while (((b >> 7) & 0x01) == 1);
		
		return l;
	}
	
	public static void writeNumStingy (DataOutputStream dos, long l) throws IOException {		
		if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE)
			throw new ArithmeticException("Value (" + l + ") too large for chosen encoding");
		
		if (l >= -STINGY_NEGATIVE_BIAS && l < 255 - STINGY_NEGATIVE_BIAS) {
			l += STINGY_NEGATIVE_BIAS;
			dos.writeByte((byte)(l >= 128 ? l - 256 : l));
		} else {
			dos.writeByte(0xff);
			dos.writeInt((int)l);
		}
	}

	public static long readNumStingy (DataInputStream dis) throws IOException {
		byte b = dis.readByte();
		long l;
		
		if (b == (byte)0xff) {
			l = dis.readInt();
		} else {
			l = (b < 0 ? b + 256 : b);
			l -= STINGY_NEGATIVE_BIAS;
		}
		
		return l;
	}
	
	/**
	 * Writes a string to the stream.
	 * 
	 * @param dos - the stream for writing.
	 * @param data - the string to write.
	 * @throws IOException - thrown when a problem occurs during the writing to stream.
	 */
	public static void writeUTF(DataOutputStream dos,String data) throws IOException{
		if(data != null){
			dos.writeBoolean(true);
			dos.writeUTF(data);
		}
		else
			dos.writeBoolean(false);
	}
	/**
	 * Writes a small (byte size) vector of UTF objects to a stream.
	 * 
	 * @param utfVector - the vector of UTF objects.
	 * @param dos - the stream to write to.
	 * @throws IOException - thrown when a problem occurs during the writing to stream.
	 */
	public static void writeUTFs(Vector utfVector, DataOutputStream dos) throws IOException {	
		if(utfVector != null){
			dos.writeByte(utfVector.size());
			for(int i=0; i<utfVector.size(); i++ ){
				writeUTF(dos, ((String)utfVector.elementAt(i)));
			}
		}
		else
			dos.writeByte(0);
	}
	
	/**
	 * reads a small vector (byte size) of UTF objects from a stream.
	 * 
	 * @param dis - the stream to be read from
	 * @throws IOException - thrown when a problem occurs during the writing to stream.
	 * 
	 * @return A vector of UTF objects
	 */
	public static Vector readUTFs(DataInputStream dis) throws IOException, InstantiationException,IllegalAccessException {
		
		byte len = dis.readByte();
		if(len == 0)
			return null;
		
		Vector utfVector = new Vector();
		
		for(byte i=0; i<len; i++ ) {
			utfVector.addElement(readUTF(dis));
		}
		
		return utfVector;
	}
	
	
	/**
	 * Writes an Integer to the stream.
	 * 
	 * @param dos - the stream for writing.
	 * @param data - the Interger to write.
	 * @throws IOException - thrown when a problem occurs during the writing to stream.
	 */
	public static void writeInteger(DataOutputStream dos,Integer data) throws IOException{
		if(data != null){
			dos.writeBoolean(true);
			dos.writeInt(data.intValue());
		}
		else
			dos.writeBoolean(false);
	} 
	
	/**
	 * Writes a Date to a stream.
	 * 
	 * @param dos - the stream to write to.
	 * @param data - the Date to write.
	 * @throws IOException - thrown when a problem occurs during the writing to stream.
	 */
	public static void writeDate(DataOutputStream dos,Date data) throws IOException{
		if(data != null){
			dos.writeBoolean(true);
			dos.writeLong(data.getTime());
		}
		else
			dos.writeBoolean(false);
	} 
	
	/**
	 * Writes a boolean to a stream.
	 * 
	 * @param dos - the stream to write to.
	 * @param data - the boolean to write.
	 * @throws IOException - thrown when a problem occurs during the writing to stream.
	 */
	public static void writeBoolean(DataOutputStream dos,Boolean data) throws IOException{
		if(data != null){
			dos.writeBoolean(true);
			dos.writeBoolean(data.booleanValue());
		}
		else
			dos.writeBoolean(false);
	} 
	
	/**
	 * Reads a string from a stream.
	 * 
	 * @param dis - the stream to read from.
	 * @return - the read string or null if none.
	 * @throws IOException - thrown when a problem occurs during the reading from stream.
	 */
	public static String readUTF(DataInputStream dis) throws IOException {
		if(dis.readBoolean())
			return dis.readUTF();
		return null;
	}
	
	/**
	 * Reads an Integer from a stream.
	 * 
	 * @param dis - the stream to read from.
	 * @return - the read Integer or null of none.
	 * @throws IOException - thrown when a problem occurs during the reading from stream.
	 */
	public static Integer readInteger(DataInputStream dis) throws IOException {
		if(dis.readBoolean())
			return new Integer(dis.readInt());
		return null;
	}
	
	/**
	 * Reads a Date from a stream.
	 * 
	 * @param dis - the stream to read from.
	 * @return - the read Date or null if none.
	 * @throws IOException - thrown when a problem occurs during the reading from stream.
	 */
	public static Date readDate(DataInputStream dis) throws IOException {
		if(dis.readBoolean())
			return new Date(dis.readLong());
		return null;
	}
	
	/**
	 * Reads a boolean from a stream.
	 * 
	 * @param dis - the stream to read from.
	 * @return - the read boolean or null if none.
	 * @throws IOException - thrown when a problem occurs during the reading from stream.
	 */
	public static Boolean readBoolean(DataInputStream dis) throws IOException {
		if(dis.readBoolean())
			return new Boolean(dis.readBoolean());
		return null;
	}
	
	/**
	 * Writes a small vector (byte size) of Externalizable objects to a stream.
	 * 
	 * @param externalizableVector - the vector of externalizable objects.
	 * @param dos - the stream to write to.
	 * @throws IOException - thrown when a problem occurs during the writing to stream.
	 */
	public static void writeExternal(Vector externalizableVector, DataOutputStream dos) throws IOException {	
		if(externalizableVector != null){
			dos.writeByte(externalizableVector.size());
			for(int i=0; i<externalizableVector.size(); i++ ){
				((Externalizable)externalizableVector.elementAt(i)).writeExternal(dos);
			}
		}
		else
			dos.writeByte(0);
	}
	
	/**
	 * Writes a small vector (byte size) of Externalizable objects to a stream.
	 * 
	 * @param externalizableVector - the vector of externalizable objects.
	 * @param dos - the stream to write to.
	 * @throws IOException - thrown when a problem occurs during the writing to stream.
	 */
	public static void writeExternalGeneric(Vector externalizableVector, DataOutputStream dos) throws IOException {	
		if(externalizableVector != null){
			dos.writeByte(externalizableVector.size());
			for(int i=0; i<externalizableVector.size(); i++ ){
				dos.writeUTF(externalizableVector.elementAt(i).getClass().getName());
				((Externalizable)externalizableVector.elementAt(i)).writeExternal(dos);
			}
		}
		else
			dos.writeByte(0);
	}
	
	public static Externalizable readExternalizable(DataInputStream dis, Externalizable item)  throws IOException, InstantiationException,IllegalAccessException, UnavailableExternalizerException {
		if(dis.readBoolean()) {
			item.readExternal(dis);
		} else {
			item = null;
		}
		return item;
	}
	
	/**
	 * Writes a possibly null Externalizable item.
	 * 
	 * @param item
	 * @param dos
	 * @throws IOException
	 */
	public static void writeExternalizable(Externalizable item, DataOutputStream dos) throws IOException {
		if(item == null) {
			dos.writeBoolean(false);
		} else {
			dos.writeBoolean(true);
			item.writeExternal(dos);
		}
	}
	
	/**
	 * Writes a big vector (of int size) of externalizable objects from a stream.
	 * 
	 * @param externalizableVector
	 * @param dos
	 * @throws IOException
	 */
	public static void writeBig(Vector externalizableVector, DataOutputStream dos) throws IOException {	
		if(externalizableVector != null){
			dos.writeInt(externalizableVector.size());
			for(int i=0; i<externalizableVector.size(); i++ ){
				((Externalizable)externalizableVector.elementAt(i)).writeExternal(dos);
			}
		}
		else
			dos.writeInt(0);
	}
	
	public static void writeExternal(Vector externalizableVector, DataOutputStream dos, int len) throws IOException {	
		if(externalizableVector != null){
			dos.writeInt(externalizableVector.size());
			for(int i=0; i<externalizableVector.size(); i++ ){
				((Externalizable)externalizableVector.elementAt(i)).writeExternal(dos);
			}
		}
		else
			dos.writeInt(0);
	}
	
	public static void writeIntegers(Vector intVector, DataOutputStream dos) throws IOException {	
		if(intVector != null){
			dos.writeByte(intVector.size());
			for(int i=0; i<intVector.size(); i++ )
				dos.writeInt(((Integer)intVector.elementAt(i)).intValue());
		}
		else
			dos.writeByte(0);
	}
	
	/**
	 * Writes a list of bytes a stream.
	 * 
	 * @param byteVector - the Byte vector.
	 * @param dos  - the stream.
	 * @throws IOException
	 */
	public static void writeBytes(Vector byteVector, DataOutputStream dos) throws IOException {	
		if(byteVector != null){
			dos.writeByte(byteVector.size());
			for(int i=0; i<byteVector.size(); i++ )
				dos.writeByte(((Byte)byteVector.elementAt(i)).byteValue());
		}
		else
			dos.writeByte(0);
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
	public static Vector readExternal(DataInputStream dis, Class cls) throws IOException, InstantiationException,IllegalAccessException, UnavailableExternalizerException {
		
		byte len = dis.readByte();
		if(len == 0)
			return null;

		Vector externalizableVector = new Vector();
		
		for(byte i=0; i<len; i++ ){
			Object obj = (Externalizable)cls.newInstance();
			((Externalizable)obj).readExternal(dis);
			externalizableVector.addElement(obj);
		}
		
		return externalizableVector;
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
	public static Vector readExternal(DataInputStream dis, PrototypeFactory factory) throws IOException, InstantiationException,IllegalAccessException, UnavailableExternalizerException {
		
		byte len = dis.readByte();
		if(len == 0)
			return null;

		Vector externalizableVector = new Vector();
		
		for(byte i=0; i<len; i++ ){
			String factoryName = dis.readUTF();
			Object obj = null;
			try {
				obj = (Externalizable)factory.getNewInstance(factoryName);
			} catch (InstantiationException e) {
				throw new UnavailableExternalizerException("An Externalizable factory for the type " + factoryName + " could not be found");
			} catch (IllegalAccessException e) {
				throw new UnavailableExternalizerException("An Externalizable factory for the type " + factoryName + " could not be found");
			}
			((Externalizable)obj).readExternal(dis);
			externalizableVector.addElement(obj);
		}
		
		return externalizableVector;
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
	public static Vector readBig(DataInputStream dis, Class cls) throws IOException, InstantiationException,IllegalAccessException, UnavailableExternalizerException {
		
		int len = dis.readInt();
		if(len == 0)
			return null;

		Vector externalizableVector = new Vector();
		
		for(int i=0; i<len; i++ ){
			Object obj = (Externalizable)cls.newInstance();
			((Externalizable)obj).readExternal(dis);
			externalizableVector.addElement(obj);
		}
		
		return externalizableVector;
	}
	
	public static Vector readExternal(DataInputStream dis, Class cls, int len) throws IOException, InstantiationException,IllegalAccessException, UnavailableExternalizerException {
		
		if(len == 0)
			return null;

		Vector externalizableVector = new Vector();
		
		for(int i=0; i<len; i++ ){
			Object obj = (Externalizable)cls.newInstance();
			((Externalizable)obj).readExternal(dis);
			externalizableVector.addElement(obj);
		}
		
		return externalizableVector;
	}
	
	public static Vector readIntegers(DataInputStream dis) throws IOException, InstantiationException,IllegalAccessException {
		
		byte len = dis.readByte();
		if(len == 0)
			return null;
		
		Vector intVector = new Vector();
		
		for(byte i=0; i<len; i++ )
			intVector.addElement(new Integer(dis.readInt()));
		
		return intVector;
	}
	
	/**
	 * Reads a list of bytes from the stream.
	 * 
	 * @param dis
	 * @return
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static Vector readBytes(DataInputStream dis) throws IOException, InstantiationException,IllegalAccessException {
		
		byte len = dis.readByte();
		if(len == 0)
			return null;
		
		Vector byteVector = new Vector();
		
		for(byte i=0; i<len; i++ )
			byteVector.addElement(new Byte(dis.readByte()));
		
		return byteVector;
	}
	
	/**
	 * Write a hashtable of string keys and values to a stream.
	 * 
	 * @param stringHashtable - a hashtable of string keys and values.
	 * @param dos - that stream to write to.
	 * @throws IOException - thrown when a problem occurs during the writing to stream.
	 */
	public static void writeExternal(Hashtable stringHashtable, DataOutputStream dos) throws IOException {	
		if(stringHashtable != null){
			dos.writeByte(stringHashtable.size());
			Enumeration keys = stringHashtable.keys();
			String key;
			while(keys.hasMoreElements()){
				key  = (String)keys.nextElement();
				dos.writeUTF(key);
				dos.writeUTF((String)stringHashtable.get(key));
			}
		}
		else
			dos.writeByte(0);
	}
	
	/* grrr.... why doesn't SimpleOrderedHashtable extend Hashtable? */
	public static void writeExternal(SimpleOrderedHashtable stringHashtable, DataOutputStream dos) throws IOException {	
		if(stringHashtable != null){
			writeNumeric(dos, stringHashtable.size(), ENCODING_NUM_DEFAULT);
			Enumeration keys = stringHashtable.keys();
			String key;
			while(keys.hasMoreElements()){
				key  = (String)keys.nextElement();
				dos.writeUTF(key);
				dos.writeUTF((String)stringHashtable.get(key));
			}
		}
		else
			dos.writeByte(0);
	}
	
	public static void writeExternalCompoundSOH(SimpleOrderedHashtable compoundHashtable, DataOutputStream dos) throws IOException {	
		if(compoundHashtable != null){
			writeNumeric(dos, compoundHashtable.size(), ENCODING_NUM_DEFAULT);
			Enumeration keys = compoundHashtable.keys();
			String key;
			while(keys.hasMoreElements()){
				key  = (String)keys.nextElement();
				dos.writeUTF(key);
				writeExternal((SimpleOrderedHashtable)compoundHashtable.get(key), dos);
			}
		}
		else
			dos.writeByte(0);
	}
	
	/**
	 * Reads a hashtable of string keys and values from a stream.
	 * 
	 * @param dis - the stream to read from.
	 * @return - the hashtable of string keys and values or null if none.
	 * @throws IOException - thrown when a problem occurs during the reading from stream.
	*/
	public static Hashtable readExternal(DataInputStream dis) throws IOException {
		byte len = dis.readByte();
		if(len == 0)
			return null;
		
		Hashtable stringHashtable = new Hashtable();

		for(byte i=0; i<len; i++ )
			stringHashtable.put(dis.readUTF(), dis.readUTF());
		
		return stringHashtable;
	}
	
	//we should be able to distinguish between null and empty vectors/hashtables
	public static SimpleOrderedHashtable readExternalSOH(DataInputStream dis) throws IOException {
		long len = readNumeric(dis, ENCODING_NUM_DEFAULT);
		if(len == 0)
			return null;
		
		SimpleOrderedHashtable stringHashtable = new SimpleOrderedHashtable();

		for(long i=0; i<len; i++ )
			stringHashtable.put(dis.readUTF(), dis.readUTF());
		
		return stringHashtable;
	}
	
	public static SimpleOrderedHashtable readExternalCompoundSOH(DataInputStream dis) throws IOException {
		long len = readNumeric(dis, ENCODING_NUM_DEFAULT);
		if(len == 0)
			return null;
		
		SimpleOrderedHashtable compoundHashtable = new SimpleOrderedHashtable();

		for(long i=0; i<len; i++ ) {
			String key = dis.readUTF();
			SimpleOrderedHashtable subHashtable = readExternalSOH(dis);
			compoundHashtable.put(key, subHashtable == null ? new SimpleOrderedHashtable() : subHashtable);
		}
			
		return compoundHashtable;
	}
	
	public static boolean isEOF(DataInputStream dis){
		
		/*boolean eof = true;
		
		try{
			dis.mark(1);
			if(dis.readExternal() != -1)
				eof = false;
			dis.reset();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		return eof;*/
		
		return false;
	}
	
	//write vector of bools
	public static void writeExternalVB(Vector externalizableVector, DataOutputStream dos) throws IOException {	
		if(externalizableVector != null){
			dos.writeByte(externalizableVector.size());
			for(int i=0; i<externalizableVector.size(); i++ ){
				writeBoolean(dos, (Boolean)externalizableVector.elementAt(i));
			}
		}
		else
			dos.writeByte(0);
	}
	
	//read vector of bools
	public static Vector readExternalVB (DataInputStream dis) throws IOException {
		byte len = dis.readByte();
		if(len == 0)
			return null;
		
		Vector v = new Vector();
		for(byte i=0; i<len; i++ )
			v.addElement(readBoolean(dis));
		
		return v;
	}
	
	
	/**
	 * Gets a externalizable object size in bytes.
	 * 
	 * @param externalizable - the externalizable object.
	 * @return the number of bytes.
	 */
	public static int getSize(Externalizable externalizable){
		try{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			externalizable.writeExternal(dos);
			return baos.toByteArray().length;
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return 0;
	}
	
	public static boolean numericEncodingUnitTest (long valIn, int encoding) {
		byte[] bytesOut; 
		long valOut;
		
		System.out.print("Testing: " + valIn + " [");
		switch (encoding) {
		case ENCODING_NUM_DEFAULT: System.out.print("default"); break;
		case ENCODING_NUM_STINGY: System.out.print("stingy"); break;
		}
		System.out.print("]");
		
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream(100);
			ExternalizableHelper.writeNumeric(new DataOutputStream(baos), valIn, encoding);
			bytesOut = baos.toByteArray();

			ByteArrayInputStream bais = new ByteArrayInputStream(bytesOut);
			valOut = ExternalizableHelper.readNumeric(new DataInputStream(bais), encoding);
		} catch (IOException ioe) {
			System.out.println("  IOException!");
			return false;
		}
		
		System.out.print("; serialized as: [");
		for (int i = 0; i < bytesOut.length; i++) {
			String hex = Integer.toHexString(bytesOut[i]);
			if (hex.length() == 1)
				hex = "0" + hex;
			else
				hex = hex.substring(hex.length() - 2);
			System.out.print(hex);
			if (i < bytesOut.length - 1)
				System.out.print(" ");
		}
		System.out.print("]; deserialized: ");
		if (valIn == valOut) {
			System.out.println("OK");
			return true;
		} else {
			System.out.println("ERROR!! [" + valOut + "]");
			return false;
		}
	}
	
	public static void numericEncodingUnitTestSuite () {
		int enc;
		
		enc = ENCODING_NUM_DEFAULT;
		
		numericEncodingUnitTest(0, enc);
		numericEncodingUnitTest(-1, enc);
		numericEncodingUnitTest(1, enc);			
		numericEncodingUnitTest(-2, enc);
	
		for (int i = 3; i <= 64; i++) {
			long min = (i < 64 ? -((long)0x01 << (i - 1)) : Long.MIN_VALUE);
			long max = (i < 64 ? ((long)0x01 << (i - 1)) - 1 : Long.MAX_VALUE);
			
			numericEncodingUnitTest(max - 1, enc);
			numericEncodingUnitTest(max, enc);
			if (i < 64)
				numericEncodingUnitTest(max + 1, enc);
			numericEncodingUnitTest(min + 1, enc);
			numericEncodingUnitTest(min, enc);
			if (i < 64)
				numericEncodingUnitTest(min - 1, enc);					
		}
		
		enc = ENCODING_NUM_STINGY;
		
		for (int i = 0; i <= 1; i++) {
			int offset = (i == 0 ? 0 : STINGY_NEGATIVE_BIAS);
			if (offset == 0 && i == 1)
				break;
			
			numericEncodingUnitTest(0 - offset, enc);
			numericEncodingUnitTest(1 - offset, enc);
			numericEncodingUnitTest(126 - offset, enc);
			numericEncodingUnitTest(127 - offset, enc);
			numericEncodingUnitTest(128 - offset, enc);
			numericEncodingUnitTest(129 - offset, enc);
			numericEncodingUnitTest(253 - offset, enc);
			numericEncodingUnitTest(254 - offset, enc);
			numericEncodingUnitTest(255 - offset, enc);
			numericEncodingUnitTest(256 - offset, enc);
			numericEncodingUnitTest(-1 - offset, enc);
			numericEncodingUnitTest(-2 - offset, enc);
			numericEncodingUnitTest(-127 - offset, enc);
			numericEncodingUnitTest(-128 - offset, enc);
			numericEncodingUnitTest(-129 - offset, enc);
		}
		numericEncodingUnitTest(3750, enc);
		numericEncodingUnitTest(-3750, enc);
		numericEncodingUnitTest(33947015, enc);
		numericEncodingUnitTest(-33947015, enc);		
		numericEncodingUnitTest(Integer.MAX_VALUE, enc);
		numericEncodingUnitTest(Integer.MAX_VALUE - 1, enc);
		numericEncodingUnitTest(Integer.MIN_VALUE, enc);
		numericEncodingUnitTest(Integer.MIN_VALUE + 1, enc);
	}
		
}
