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

import org.javarosa.core.util.Base64;
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
        @Override
        String encode(byte[] sArr) {
            return Base64.encode(sArr);
        }

        @Override
        byte[] decode(byte[] sArr) {
            return Base64.decode(sArr);
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

    abstract String encode(byte[] bytes);

    abstract byte[] decode(byte[] bytes);
}
