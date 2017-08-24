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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public class ExtWrapIntEncodingUniform extends ExtWrapIntEncoding {
	/* serialization */
	
	public ExtWrapIntEncodingUniform (long l) {
		val = new Long(l);
	}

	/* deserialization */
	
	public ExtWrapIntEncodingUniform () {

	}
	
	public ExternalizableWrapper clone(Object val) {
		return new ExtWrapIntEncodingUniform(ExtUtil.toLong(val));
	}

	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException {
		long l = 0;
		byte b;
		boolean firstByte = true;
		
		do {
			b = in.readByte();
			
			if (firstByte) {
				firstByte = false;
				l = (((b >> 6) & 0x01) == 0 ? 0 : -1); //set initial sign
			}
			
			l = (l << 7) | (b & 0x7f);
		} while (((b >> 7) & 0x01) == 1);
		
		val = new Long(l);
	}
	
	/**
	 * serialize a numeric value, only using as many bytes as needed. splits up the value into
	 * chunks of 7 bits, using as many chunks as needed to unambiguously represent the value. each
	 * chunk is serialized as a single byte, where the most-significant bit is set to 1 to indicate
	 * there are more bytes to follow, or 0 to indicate the last byte
	 **/
	public void writeExternal(DataOutputStream out) throws IOException {
		long l = ((Long)val).longValue();
		
		int sig = -1;
		long k;
		do {
			sig++;
			k = l >> (sig * 7);
		} while (k < (-1 << 6) || k > (1 << 6) - 1); //[-64,63] -- the range we can fit into one byte

		for (int i = sig; i >= 0; i--) {
			byte chunk = (byte)((l >> (i * 7)) & 0x7f);
			out.writeByte((i > 0 ? 0x80 : 0x00) | chunk);
		}
	}

	public void metaReadExternal(DataInputStream in, PrototypeFactory pf) {
		//do nothing
	}

	public void metaWriteExternal(DataOutputStream out) {
		//do nothing
	}
}
