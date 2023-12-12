package org.javarosa.core.util;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

// Copied from: https://github.com/brsanthu/migbase64/blob/master/src/main/java/com/migcomponents/migbase64/Base64.java
// Irrelevant after Java8
public class Base64 {

    private static final char[] BASE_64_TBL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
    private static final int[] IA = new int[256];
    static {
        Arrays.fill(IA, -1);
        for (int i = 0, iS = BASE_64_TBL.length; i < iS; i++)
            IA[BASE_64_TBL[i]] = i;
        IA['='] = 0;
    }

    public static String encode(byte[] sArr) {
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

    public static byte[] decode(byte[] sArr) {
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
}
