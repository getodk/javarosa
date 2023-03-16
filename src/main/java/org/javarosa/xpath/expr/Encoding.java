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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.javarosa.xpath.XPathUnsupportedException;

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

        @Override
        byte[] decode(byte[] bytes) {
            int len = bytes.length;
            byte[] data = new byte[len / 2];
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(bytes[i], 16) << 4)
                    + Character.digit(bytes[i+1], 16));
            }
            return data;
        }
    },
    BASE64("base64") {
        // Copied from: https://github.com/brsanthu/migbase64/blob/master/src/main/java/com/migcomponents/migbase64/Base64.java
        // Irrelevant after Java8
        @Override
        String encode(byte[] sArr) {
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

            return new String(dArr, StandardCharsets.UTF_8);
        }

        @Override
        public byte[] decode(byte[] sArr) {
            int sepCnt = 0;
            for (byte b : sArr)
                if (IA[b & 0xff] < 0)
                    sepCnt++;

            if ((sArr.length - sepCnt) % 4 != 0)
                return new byte[0];

            int pad = 0;
            for (int i = sArr.length; i > 1 && IA[sArr[--i] & 0xff] <= 0; )
                if (sArr[i] == '=')
                    pad++;

            int len = ((sArr.length - sepCnt) * 6 >> 3) - pad;

            byte[] dArr = new byte[len];

            for (int s = 0, d = 0; d < len; ) {
                int i = 0;
                for (int j = 0; j < 4; j++) {
                    int c = IA[sArr[s++] & 0xff];
                    if (c >= 0)
                        i |= c << (18 - j * 6);
                    else
                        j--;
                }

                dArr[d++] = (byte) (i >> 16);
                if (d < len) {
                    dArr[d++] = (byte) (i >> 8);
                    if (d < len)
                        dArr[d++] = (byte) i;
                }
            }

            return dArr;
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

    private static final int[] IA = new int[256];
    static {
        Arrays.fill(IA, -1);
        for (int i = 0, iS = BASE_64_TBL.length; i < iS; i++)
            IA[BASE_64_TBL[i]] = i;
        IA['='] = 0;
    }

    abstract String encode(byte[] bytes);

    abstract byte[] decode(byte[] bytes);
}
