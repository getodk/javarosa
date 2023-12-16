package org.javarosa.core.util;

import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;

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
            Ed25519PublicKeyParameters publicKeyParameters = new Ed25519PublicKeyParameters(publicKey, 0);
            Signer signer = new Ed25519Signer();
            signer.init(false, publicKeyParameters);
            signer.update(message, 0, message.length);

            return signer.verifySignature(signature);
        } catch (ArrayIndexOutOfBoundsException e) {
            // The key was too small
            return false;
        } catch (IllegalArgumentException e) {
            // The key was invalid
            return false;
        }
    }
}
