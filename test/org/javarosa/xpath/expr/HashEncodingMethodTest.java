package org.javarosa.xpath.expr;


import static org.javarosa.xpath.expr.XPathFuncExpr.HashEncodingMethod.HEX;
import static org.junit.Assert.assertEquals;

import java.io.UnsupportedEncodingException;
import org.junit.Test;

public class HashEncodingMethodTest {
  @Test
  public void encodes_byte_arrays() throws UnsupportedEncodingException {
    assertEquals(
        "3031323334353637383961626364656620736f6d652074657874",
        HEX.encode("0123456789abcdef some text".getBytes("UTF-8"))
    );
  }
}