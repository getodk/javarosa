package org.javarosa.core.util;

import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.EdECPoint;
import java.security.spec.EdECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.NamedParameterSpec;

public class Ed25519 {

    private static final int SIGNATURE_LENGTH = 64;

    @Nullable
    public static String extractSigned(byte[] contents, byte[] publicKey) {
        if (contents.length < 64) {
            return null;
        }

        byte[] signature = new byte[SIGNATURE_LENGTH];
        System.arraycopy(contents, 0, signature, 0, SIGNATURE_LENGTH);

        int messageLength = contents.length - SIGNATURE_LENGTH;
        byte[] message = new byte[messageLength];
        System.arraycopy(contents, SIGNATURE_LENGTH, message, 0, messageLength);

        if (verify(publicKey, signature, message)) {
            return new String(message, StandardCharsets.UTF_8);
        } else {
            return null;
        }
    }

    private static boolean verify(byte[] publicKey, byte[] signature, byte[] message) {
        try {
            Signature signer = Signature.getInstance("Ed25519");

            EdECPublicKeySpec pubSpec = getPubSpec(publicKey);
            KeyFactory keyFactory = KeyFactory.getInstance("Ed25519");
            signer.initVerify(keyFactory.generatePublic(pubSpec));

            signer.update(message);
            return signer.verify(signature);
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeySpecException e) {
            throw new RuntimeException();
        } catch (InvalidKeyException e) {
            return false;
        }
    }

    private static EdECPublicKeySpec getPubSpec(byte[] publicKey) {
        byte[] publicKeyCopy = new byte[publicKey.length];
        System.arraycopy(publicKey, 0, publicKeyCopy, 0, publicKey.length);

        int lastbyteInt = publicKeyCopy[publicKeyCopy.length - 1];
        boolean isXOdd = (lastbyteInt & 255) >> 7 == 1;

        publicKeyCopy[publicKeyCopy.length - 1] &= 127;
        BigInteger y = new BigInteger(1, reverseBytes(publicKeyCopy));

        NamedParameterSpec paramSpec = new NamedParameterSpec("Ed25519");
        EdECPoint point = new EdECPoint(isXOdd, y);
        return new EdECPublicKeySpec(paramSpec, point);
    }

    private static byte[] reverseBytes(byte[] bytes) {
        byte[] reversed = new byte[bytes.length];
        int lastIndex = bytes.length - 1;

        for (int i = 0; i < bytes.length; i++) {
            reversed[lastIndex - i] = bytes[i];
        }

        return reversed;
    }
}
