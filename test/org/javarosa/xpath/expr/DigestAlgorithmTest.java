package org.javarosa.xpath.expr;


import static org.javarosa.xpath.expr.DigestAlgorithm.MD5;
import static org.javarosa.xpath.expr.DigestAlgorithm.SHA1;
import static org.javarosa.xpath.expr.DigestAlgorithm.SHA256;
import static org.javarosa.xpath.expr.DigestAlgorithm.SHA384;
import static org.javarosa.xpath.expr.DigestAlgorithm.SHA512;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class DigestAlgorithmTest {
  @Parameterized.Parameter(value = 0)
  public String testName;

  @Parameterized.Parameter(value = 1)
  public DigestAlgorithm algorithm;

  @Parameterized.Parameter(value = 2)
  public String input;

  @Parameterized.Parameter(value = 3)
  public String expectedOutput;

  @Parameterized.Parameters(name = "{0}")
  public static Iterable<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {"MD5", MD5, "some text", "552e21cd4cd9918678e3c1a0df491bc3"},
        {"SHA-1", SHA1, "some text", "37aa63c77398d954473262e1a0057c1e632eda77"},
        {"SHA-256", SHA256, "some text", "b94f6f125c79e3a5ffaa826f584c10d52ada669e6762051b826b55776d05aed2"},
        {"SHA-384", SHA384, "some text", "cc94ec3e9873c0b9a72486442958f671067cdf77b9427416d031440cc62041e2ee1344498447ec0ced9f7043461bd1f3"},
        {"SHA-512", SHA512, "some text", "e2732baedca3eac1407828637de1dbca702c3fc9ece16cf536ddb8d6139cd85dfe7464b8235b29826f608ccf4ac643e29b19c637858a3d8710a59111df42ddb5"},
    });
  }

  @Test
  public void generates_a_digest() {
    assertEquals(expectedOutput, algorithm.digest(input, Encoding.HEX));
  }
}