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
