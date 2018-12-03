// Portions copyright 2002, Google, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.android.vending.licensing.util;

// This code was converted from code at http://iharder.sourceforge.net/base64/
// Lots of extraneous features were removed.
/* The original code said:
 * <p>
 * I am placing this code in the Public Domain. Do with it as you will.
 * This software comes with no guarantees or warranties but with
 * plenty of well-wishing instead!
 * Please visit
 * <a href="http://iharder.net/xmlizable">http://iharder.net/xmlizable</a>
 * periodically to check for updates or to contribute improvements.
 * </p>
 *
 * @author Robert Harder
 * @author rharder@usa.net
 * @version 1.3
 */

/**
 * Base64 converter class. This code is not a full-blown MIME encoder;
 * it simply converts binary data to base64 data and back.
 */
public class Base64 {
  /** The equals sign (=) as a byte. */
  private final static byte EQUALS_SIGN = (byte) '=';

  /** The new line character (\n) as a byte. */
  private final static byte NEW_LINE = (byte) '\n';

  /**
   * The 64 valid Base64 values.
   */
  private final static byte[] ALPHABET =
      {(byte) 'A', (byte) 'B', (byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F',
          (byte) 'G', (byte) 'H', (byte) 'I', (byte) 'J', (byte) 'K',
          (byte) 'L', (byte) 'M', (byte) 'N', (byte) 'O', (byte) 'P',
          (byte) 'Q', (byte) 'R', (byte) 'S', (byte) 'T', (byte) 'U',
          (byte) 'V', (byte) 'W', (byte) 'X', (byte) 'Y', (byte) 'Z',
          (byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd', (byte) 'e',
          (byte) 'f', (byte) 'g', (byte) 'h', (byte) 'i', (byte) 'j',
          (byte) 'k', (byte) 'l', (byte) 'm', (byte) 'n', (byte) 'o',
          (byte) 'p', (byte) 'q', (byte) 'r', (byte) 's', (byte) 't',
          (byte) 'u', (byte) 'v', (byte) 'w', (byte) 'x', (byte) 'y',
          (byte) 'z', (byte) '0', (byte) '1', (byte) '2', (byte) '3',
          (byte) '4', (byte) '5', (byte) '6', (byte) '7', (byte) '8',
          (byte) '9', (byte) '+', (byte) '/'};

  /** Defeats instantiation. */
  private Base64() {
  }

  /* ********  E N C O D I N G   M E T H O D S  ******** */

  /**
   * Encodes up to three bytes of the array <var>source</var>
   * and writes the resulting four Base64 bytes to <var>destination</var>.
   * The source and destination arrays can be manipulated
   * anywhere along their length by specifying
   * <var>srcOffset</var> and <var>destOffset</var>.
   * This method does not check to make sure your arrays
   * are large enough to accommodate <var>srcOffset</var> + 3 for
   * the <var>source</var> array or <var>destOffset</var> + 4 for
   * the <var>destination</var> array.
   * The actual number of significant bytes in your array is
   * given by <var>numSigBytes</var>.
   *
   * @param source the array to convert
   * @param srcOffset the index where conversion begins
   * @param numSigBytes the number of significant bytes in your array
   * @param destination the array to hold the conversion
   * @param destOffset the index where output will be put
   * @param alphabet is the encoding alphabet
   * @return the <var>destination</var> array
   * @since 1.3
   */
  private static byte[] encode3to4(byte[] source, int srcOffset,
      int numSigBytes, byte[] destination, int destOffset, byte[] alphabet) {
    //           1         2         3
    // 01234567890123456789012345678901 Bit position
    // --------000000001111111122222222 Array position from threeBytes
    // --------|    ||    ||    ||    | Six bit groups to index alphabet
    //          >>18  >>12  >> 6  >> 0  Right shift necessary
    //                0x3f  0x3f  0x3f  Additional AND

    // Create buffer with zero-padding if there are only one or two
    // significant bytes passed in the array.
    // We have to shift left 24 in order to flush out the 1's that appear
    // when Java treats a value as negative that is cast from a byte to an int.
    int inBuff =
        (numSigBytes > 0 ? ((source[srcOffset] << 24) >>> 8) : 0)
            | (numSigBytes > 1 ? ((source[srcOffset + 1] << 24) >>> 16) : 0)
            | (numSigBytes > 2 ? ((source[srcOffset + 2] << 24) >>> 24) : 0);

    switch (numSigBytes) {
      case 3:
        destination[destOffset] = alphabet[(inBuff >>> 18)];
        destination[destOffset + 1] = alphabet[(inBuff >>> 12) & 0x3f];
        destination[destOffset + 2] = alphabet[(inBuff >>> 6) & 0x3f];
        destination[destOffset + 3] = alphabet[(inBuff) & 0x3f];
        return destination;
      case 2:
        destination[destOffset] = alphabet[(inBuff >>> 18)];
        destination[destOffset + 1] = alphabet[(inBuff >>> 12) & 0x3f];
        destination[destOffset + 2] = alphabet[(inBuff >>> 6) & 0x3f];
        destination[destOffset + 3] = EQUALS_SIGN;
        return destination;
      case 1:
        destination[destOffset] = alphabet[(inBuff >>> 18)];
        destination[destOffset + 1] = alphabet[(inBuff >>> 12) & 0x3f];
        destination[destOffset + 2] = EQUALS_SIGN;
        destination[destOffset + 3] = EQUALS_SIGN;
        return destination;
      default:
        return destination;
    } // end switch
  } // end encode3to4

  /**
   * Encodes a byte array into Base64 notation.
   * Equivalent to calling
   * {@code encodeBytes(source, 0, source.length)}
   *
   * @param source The data to convert
   * @since 1.4
   */
  public static String encode(byte[] source) {
    byte[] outBuff = encode(source, 0, source.length, ALPHABET, Integer.MAX_VALUE);

    return new String(outBuff, 0, outBuff.length);
  }

  /**
   * Encodes a byte array into Base64 notation.
   *
   * @param source The data to convert
   * @param off Offset in array where conversion should begin
   * @param len Length of data to convert
   * @param alphabet is the encoding alphabet
   * @param maxLineLength maximum length of one line.
   * @return the BASE64-encoded byte array
   */
  private static byte[] encode(byte[] source, int off, int len, byte[] alphabet,
                               int maxLineLength) {
    int lenDiv3 = (len + 2) / 3; // ceil(len / 3)
    int len43 = lenDiv3 * 4;
    byte[] outBuff = new byte[len43 // Main 4:3
        + (len43 / maxLineLength)]; // New lines

    int d = 0;
    int e = 0;
    int len2 = len - 2;
    int lineLength = 0;
    for (; d < len2; d += 3, e += 4) {

      // The following block of code is the same as
      // encode3to4( source, d + off, 3, outBuff, e, alphabet );
      // but inlined for faster encoding (~20% improvement)
      int inBuff =
          ((source[d + off] << 24) >>> 8)
              | ((source[d + 1 + off] << 24) >>> 16)
              | ((source[d + 2 + off] << 24) >>> 24);
      outBuff[e] = alphabet[(inBuff >>> 18)];
      outBuff[e + 1] = alphabet[(inBuff >>> 12) & 0x3f];
      outBuff[e + 2] = alphabet[(inBuff >>> 6) & 0x3f];
      outBuff[e + 3] = alphabet[(inBuff) & 0x3f];

      lineLength += 4;
      if (lineLength == maxLineLength) {
        outBuff[e + 4] = NEW_LINE;
        e++;
        lineLength = 0;
      } // end if: end of line
    } // end for: each piece of array

    if (d < len) {
      encode3to4(source, d + off, len - d, outBuff, e, alphabet);

      lineLength += 4;
      if (lineLength == maxLineLength) {
        // Add a last newline
        outBuff[e + 4] = NEW_LINE;
        e++;
      }
      e += 4;
    }

    assert (e == outBuff.length);
    return outBuff;
  }
}
