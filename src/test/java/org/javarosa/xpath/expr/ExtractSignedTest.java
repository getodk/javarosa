package org.javarosa.xpath.expr;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.javarosa.core.test.Scenario;
import org.javarosa.xpath.XPathUnhandledException;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.javarosa.core.test.AnswerDataMatchers.stringAnswer;
import static org.javarosa.core.util.BindBuilderXFormsElement.bind;
import static org.javarosa.core.util.XFormsElement.body;
import static org.javarosa.core.util.XFormsElement.head;
import static org.javarosa.core.util.XFormsElement.html;
import static org.javarosa.core.util.XFormsElement.input;
import static org.javarosa.core.util.XFormsElement.mainInstance;
import static org.javarosa.core.util.XFormsElement.model;
import static org.javarosa.core.util.XFormsElement.t;
import static org.javarosa.core.util.XFormsElement.title;
import static org.junit.Assert.fail;

public class ExtractSignedTest {

    @Test
    public void whenSignatureIsValid_returnsNonSignatureContents() throws Exception {
        String message = "real genuine data";
        AsymmetricCipherKeyPair keyPair = createKeyPair();

        byte[] signedMessage = signMessage(message, keyPair.getPrivate());

        String encodedPublicKey = Encoding.BASE64.encode(((Ed25519PublicKeyParameters) keyPair.getPublic()).getEncoded());
        String encodedContents = Encoding.BASE64.encode(signedMessage);

        Scenario scenario = Scenario.init("extract signed form", html(
            head(
                title("extract signed form"),
                model(
                    mainInstance(t("data id=\"extract-signed\"",
                        t("contents", encodedContents),
                        t("extracted")
                    )),
                    bind("/data/contents").type("string"),
                    bind("/data/extracted").type("string").calculate("extract-signed(/data/contents,'" + encodedPublicKey + "')")
                )
            ),
            body(
                input("/data/contents")
            ))
        );

        assertThat(scenario.answerOf("/data/extracted"), is(stringAnswer(message)));
    }

    @Test
    public void whenSignatureIsNotValid_returnsEmptyString() throws Exception {
        String message = "real genuine data";
        AsymmetricCipherKeyPair keyPair1 = createKeyPair();
        AsymmetricCipherKeyPair keyPair2 = createKeyPair();

        byte[] signedMessage = signMessage(message, keyPair1.getPrivate());

        String encodedPublicKey = Encoding.BASE64.encode(((Ed25519PublicKeyParameters) keyPair2.getPublic()).getEncoded());
        String encodedContents = Encoding.BASE64.encode(signedMessage);

        Scenario scenario = Scenario.init("extract signed form", html(
            head(
                title("extract signed form"),
                model(
                    mainInstance(t("data id=\"extract-signed\"",
                        t("contents", encodedContents),
                        t("extracted")
                    )),
                    bind("/data/contents").type("string"),
                    bind("/data/extracted").type("string").calculate("extract-signed(/data/contents,'" + encodedPublicKey + "')")
                )
            ),
            body(
                input("/data/contents")
            ))
        );

        assertThat(scenario.answerOf("/data/extracted"), is(nullValue()));
    }

    @Test
    public void whenNoArgs_throwsException() throws Exception {
        try {
            Scenario.init("extract signed form", html(
                head(
                    title("extract signed form"),
                    model(
                        mainInstance(t("data id=\"extract-signed\"",
                            t("contents", "blah"),
                            t("extracted")
                        )),
                        bind("/data/extracted").type("string").calculate("extract-signed()")
                    )
                ),
                body(
                    input("/data/contents")
                ))
            );

            fail("RuntimeException caused by XPathUnhandledException expected");
        } catch (RuntimeException e) {
            assertThat(e.getCause(), instanceOf(XPathUnhandledException.class));
        }
    }

    private static AsymmetricCipherKeyPair createKeyPair() {
        Ed25519KeyPairGenerator ed25519KeyPairGenerator = new Ed25519KeyPairGenerator();
        ed25519KeyPairGenerator.init(new Ed25519KeyGenerationParameters(new SecureRandom()));
        return ed25519KeyPairGenerator.generateKeyPair();
    }

    private static byte[] signMessage(String message, AsymmetricKeyParameter privateKey) throws CryptoException {
        Signer signer = new Ed25519Signer();
        signer.init(true, privateKey);
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        signer.update(messageBytes, 0, messageBytes.length);
        byte[] signature = signer.generateSignature();

        byte[] signedMessage = new byte[signature.length + messageBytes.length];
        System.arraycopy(signature, 0, signedMessage, 0, signature.length);
        System.arraycopy(messageBytes, 0, signedMessage, signature.length, messageBytes.length);
        return signedMessage;
    }
}
