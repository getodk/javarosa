package org.javarosa.xpath.expr;

import javax.xml.bind.DatatypeConverter;
import org.javarosa.xpath.XPathUnsupportedException;

/**
 * Implements the hash encoding methods for XPathFuncExpr digest() function
 */
enum Encoding {
  HEX("hex") {
    @Override
    String encode(byte[] bytes) {
      return DatatypeConverter.printHexBinary(bytes).toLowerCase();
    }
  },
  BASE64("base64") {
    @Override
    String encode(byte[] bytes) {
      return DatatypeConverter.printBase64Binary(bytes);
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

  abstract String encode(byte[] bytes);
}
