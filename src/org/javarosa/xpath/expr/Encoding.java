/*
 * Copyright 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.javarosa.xpath.expr;

import org.javarosa.xpath.XPathUnsupportedException;

import java.io.UnsupportedEncodingException;

/**
 * Implements the hash encoding methods for XPathFuncExpr digest() function
 */
enum Encoding {
  HEX("hex") {
    @Override
    String encode(byte[] bytes) {
      StringBuilder sb = new StringBuilder(bytes.length * 2);
      for (byte b : bytes) {
        sb.append(HEX_TBL[(b >> 4) & 0xF]);
        sb.append(HEX_TBL[(b & 0xF)]);
      }
      return sb.toString();
    }
  },
  BASE64("base64") {
    @Override
    String encode(byte[] sArr) {
      // Copied from: https://github.com/brsanthu/migbase64/blob/master/src/main/java/com/migcomponents/migbase64/Base64.java
      // Irrelevant after Java8
      int sLen = sArr.length;
      int sOff = 0;

      if (sLen == 0)
        return "";

      int eLen = (sLen / 3) * 3;
      int dLen = ((sLen - 1) / 3 + 1) << 2;
      byte[] dArr = new byte[dLen];

      for (int s = sOff, d = 0; s < sOff + eLen; ) {
        int i = (sArr[s++] & 0xff) << 16 | (sArr[s++] & 0xff) << 8 | (sArr[s++] & 0xff);
        dArr[d++] = (byte) BASE_64_TBL[(i >>> 18) & 0x3f];
        dArr[d++] = (byte) BASE_64_TBL[(i >>> 12) & 0x3f];
        dArr[d++] = (byte) BASE_64_TBL[(i >>> 6) & 0x3f];
        dArr[d++] = (byte) BASE_64_TBL[i & 0x3f];
      }

      int left = sLen - eLen;
      if (left > 0) {
        int i = ((sArr[sOff + eLen] & 0xff) << 10) | (left == 2 ? ((sArr[sOff + sLen - 1] & 0xff) << 2) : 0);
        dArr[dLen - 4] = (byte) BASE_64_TBL[i >> 12];
        dArr[dLen - 3] = (byte) BASE_64_TBL[(i >>> 6) & 0x3f];
        dArr[dLen - 2] = left == 2 ? (byte) BASE_64_TBL[i & 0x3f] : (byte) '=';
        dArr[dLen - 1] = '=';
      }

      try {
        return new String(dArr, "UTF-8");
      } catch (UnsupportedEncodingException e) {
        // It’s unlikely that UTF-8 would not be supported
        throw new RuntimeException("Encoding to base64 failed to use UTF-8");
      }
    }
  };

  private final String name;

  Encoding(String name) {
    this.name = name;
  }

  static Encoding from(String name) {
    try {
      return valueOf(name.toUpperCase());
    } catch (IllegalArgumentException ex) {
      throw new XPathUnsupportedException("digest(..., ..., '" + name + "')");
    }
  }

  private static final char[] HEX_TBL = "0123456789abcdef".toCharArray();

  private static final char[] BASE_64_TBL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();

  abstract String encode(byte[] bytes);
}
