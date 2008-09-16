package org.javarosa.core.util.test;

import j2meunit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.util.Externalizable;
import org.javarosa.core.util.UnavailableExternalizerException;

public class ExternalizableTest {
	private static final boolean VERBOSE = true;
	
	public static byte[] serialize (Externalizable ext) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ext.writeExternal(new DataOutputStream(baos));
		return baos.toByteArray();
	}
	
	public static Externalizable deserialize (byte[] stream, Class extType) throws IOException, InstantiationException, IllegalAccessException, UnavailableExternalizerException {
		if (!Externalizable.class.isAssignableFrom(extType)) {
			throw new ClassCastException();
		}
		
		ByteArrayInputStream bais = new ByteArrayInputStream(stream);
		Externalizable ext = ((Externalizable)extType.newInstance());
		ext.readExternal(new DataInputStream(bais));
		return ext;
	} 
	
	public static void testSerialization (Externalizable original, TestCase tc) {
		testSerialization(original, tc, "Serialization failure for " + original.getClass().getName());
	}
	
	public static void testSerialization (Externalizable original, TestCase tc, String failMessage) {
		Externalizable reconstituted;
		byte[] bytes;
		
		print("");
		print("Original: " + original.toString());
			
		try {
			bytes = serialize(original);

			print("Serialized as:");
			print(printBytes(bytes));
			
			reconstituted = deserialize(bytes, original.getClass());

			print("Reconstituted: " + reconstituted.toString());

			if (original.equals(reconstituted)) {
				print("SUCCESS");
			} else {
				print("FAILURE");
				tc.fail(failMessage + ": Objects do not match");
			}
			
			print("");
		} catch (Exception e) {
			if (VERBOSE) {
				e.printStackTrace();
			}
				
			tc.fail(failMessage + ": Exception! " + e.getClass().getName() + " " + e.getMessage());
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
	
	private static void print (String s) {
		if (VERBOSE) {
			System.out.println(s);
		}
	}
}
