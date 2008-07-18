package org.javarosa.core.model.utils;

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
	
	public static void readExternalizable(DataInputStream dis, Externalizable item)  throws IOException, InstantiationException,IllegalAccessException, UnavailableExternalizerException {
		if(dis.readBoolean()) {
			item.readExternal(dis);
		}		
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
}
