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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.javarosa.xpath.XPathUnsupportedException;

/**
 * Implements the digest algorithms for XPathFuncExpr digest() function
 */
public enum DigestAlgorithm {
  MD5("MD5"),
  SHA1("SHA-1"),
  SHA256("SHA-256"),
  SHA384("SHA-384"),
  SHA512("SHA-512");

  private final String name;

  DigestAlgorithm(String name) {
    this.name = name;
  }

  private MessageDigest getDigestInstance() {
    try {
      return MessageDigest.getInstance(name);
    } catch (NoSuchAlgorithmException e) {
      // It’s unlikely that these algorithms would not all be implemented
      throw new XPathUnsupportedException("digest(..., '" + name + "', ...)");
    }
  }

  static DigestAlgorithm from(String name) {
    try {
      return valueOf(name.toUpperCase().replaceAll("-", ""));
    } catch (IllegalArgumentException ex) {
      throw new XPathUnsupportedException("digest(..., '" + name + "', ...)");
    }
  }

  public String digest(String payload, Encoding encoding) {
    return encoding.encode(digest(payload));
  }

  public byte[] digest(String payload) {
    return getDigestInstance().digest(uncheckedGetUtf8Bytes(payload));
  }

  private byte[] uncheckedGetUtf8Bytes(String payload) {
    try {
      // Use UTF-8 encoding when reading data to be hashed by default
      return payload.getBytes("UTF-8");
    } catch (UnsupportedEncodingException e) {
      // It’s unlikely that UTF-8 would not be supported
      throw new RuntimeException("The function digest failed to use UTF-8 encoding");
    }
  }

}
