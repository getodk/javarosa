package org.javarosa.core.util.externalizable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.javarosa.core.util.MD5;
import org.javarosa.core.util.UnavailableExternalizerException;

public class ExtWrapTagged extends ExternalizableWrapper {
	public Vector prototypes;
	
	public final static int CLASS_HASH_SIZE = 4;
	public final static byte[] WRAPPER_TAG = {(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff};
	
	public static Hashtable WRAPPER_CODES;
	
	static {
		WRAPPER_CODES = new Hashtable();
		WRAPPER_CODES.put(ExtWrapNullable.class, new Integer(0x00));
		WRAPPER_CODES.put(ExtWrapList.class, new Integer(0x20));
		WRAPPER_CODES.put(ExtWrapListPoly.class, new Integer(0x21));
		WRAPPER_CODES.put(ExtWrapMap.class, new Integer(0x22));
		WRAPPER_CODES.put(ExtWrapMapPoly.class, new Integer(0x23));
		WRAPPER_CODES.put(ExtWrapIntEncodingUniform.class, new Integer(0x40));
		WRAPPER_CODES.put(ExtWrapIntEncodingSmall.class, new Integer(0x41));
	}
	
	/* serialization */
	
	public ExtWrapTagged (Object val) {
		if (val == null) {
			throw new NullPointerException();
		} else if (val instanceof ExtWrapTagged) {
			throw new IllegalArgumentException("Wrapping tagged with tagged is redundant");
		}
		
		this.val = val;
	}
	
	/* deserialization */
	
	public ExtWrapTagged (Object ignore, Vector prototypes) {
		initPrototypes(prototypes);
	}
	
	private void initPrototypes (Vector prototypes) {
		this.prototypes = prototypes == null ? new Vector() : prototypes;
		fillDefaultClasses();
		checkCollisions();
	}
	
	private void fillDefaultClasses () {
		addNoDup(prototypes, Object.class);
		addNoDup(prototypes, Integer.class);
		addNoDup(prototypes, Long.class);
		addNoDup(prototypes, Short.class);
		addNoDup(prototypes, Byte.class);
		addNoDup(prototypes, Character.class);
		addNoDup(prototypes, Boolean.class);
		addNoDup(prototypes, Float.class);
		addNoDup(prototypes, Double.class);
		addNoDup(prototypes, String.class);
		addNoDup(prototypes, Date.class);
	}
	
	private void addNoDup (Vector v, Object o) {
		if (!v.contains(o)) {
			v.addElement(o);
		}
	}
	
	public ExternalizableWrapper clone (Object val) {
		return new ExtWrapTagged(val, prototypes);
	}
	
	public void readExternal(DataInputStream in) throws 
		IOException, UnavailableExternalizerException, IllegalAccessException, InstantiationException {
		ExternalizableWrapper type = readTag(in, prototypes);
		val = ExtUtil.read(in, type);
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		writeTag(out, val);
		ExtUtil.write(out, val);
	}

	public static byte[] getClassHash (Class type) {
		byte[] hash = new byte[CLASS_HASH_SIZE];
		byte[] md5 = MD5.hash(type.getName().getBytes()); //add support for a salt, in case of collision?
		
		for (int i = 0; i < hash.length; i++)
			hash[i] = md5[i];
		
		return hash;
	}
	
	private static boolean compareHash (byte[] a, byte[] b) {
		if (a.length != b.length) {
			return false;
		}
		
		for (int i = 0; i < a.length; i++) {
			if (a[i] != b[i])
				return false;
		}
		
		return true;
	}
	
	public static void writeTag (DataOutputStream out, Object o) throws IOException {
		if (o instanceof ExternalizableWrapper && !(o instanceof ExtType)) {
			out.write(WRAPPER_TAG, 0, CLASS_HASH_SIZE);
			ExtUtil.writeNumeric(out, ((Integer)WRAPPER_CODES.get(o.getClass())).intValue());
			((ExternalizableWrapper)o).metaWriteExternal(out);
		} else {
			Class type = null;
			
			if (o instanceof ExtType) { //ExtWrapTagged?
				ExtType extType = (ExtType)o;
				if (extType.val != null) {
					o = extType.val;
				} else {
					type = extType.type;
				}
			}
			if (type == null) {
				type = o.getClass();
			}
				
			byte[] tag = getClassHash(type);
			out.write(tag, 0, tag.length);
		}
	}

	public static ExternalizableWrapper readTag (DataInputStream in, Vector prototypes) throws IOException, UnavailableExternalizerException, IllegalAccessException, InstantiationException {
		byte[] tag = new byte[CLASS_HASH_SIZE];
		in.read(tag, 0, tag.length);
		
		if (compareHash(tag, WRAPPER_TAG)) {
			int wrapperCode = (int)ExtUtil.readInt(in);
			
			//find wrapper indicated by code
			ExternalizableWrapper type = null;
			for (Enumeration e = WRAPPER_CODES.keys(); e.hasMoreElements(); ) {
				Class t = (Class)e.nextElement();
				if (((Integer)WRAPPER_CODES.get(t)).intValue() == wrapperCode) {
					type = (ExternalizableWrapper)t.newInstance();
				}
			}
			if (type == null) {
				throw new UnavailableExternalizerException("");
			}
			
			type.metaReadExternal(in, prototypes);
			return type;
		} else {
			//find class corresponding to hash
			Class type = null;
			for (int i = 0; i < prototypes.size(); i++) {
				Class t = (Class)prototypes.elementAt(i);
				if (compareHash(tag, getClassHash(t))) {
					type = t;
					break;
				}
			}
			if (type == null) {
				throw new UnavailableExternalizerException("");
			}
			
			return new ExtType(type);
		}		
	}

	public void metaReadExternal(DataInputStream in, Vector prototypes) throws
		IOException, UnavailableExternalizerException, IllegalAccessException, InstantiationException {
		throw new RuntimeException("Tagged wrapper should never be tagged");
	}

	public void metaWriteExternal(DataOutputStream out) throws IOException {
		throw new RuntimeException("Tagged wrapper should never be tagged"); //writeTag(out, val);
	}
	
	private void checkCollisions () {
		Hashtable hashes = new Hashtable();
		for (Enumeration e = prototypes.elements(); e.hasMoreElements(); ) {
			Class t = (Class)e.nextElement();
			byte[] hash = getClassHash(t);
			
			if (compareHash(hash, WRAPPER_TAG)) {
				throw new Error("Hash collision! " + t.getName() + " and reserved wrapper tag");
			}
			
			for (Enumeration f = hashes.keys(); f.hasMoreElements(); ) {
				Class u = (Class)f.nextElement();
				
				if (compareHash(hash, (byte[])hashes.get(u))) {
					throw new Error("Hash collision! " + t.getName() + " and " + u.getName());					
				}
			}
			
			hashes.put(t, hash);
		}
	}
}
