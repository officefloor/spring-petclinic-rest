/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * Helpers backing the owner duplicate-detection identity key. The identity key is the lower-case
 * SHA-256 hex of the owner's normalized telephone, lower-cased email and the American Soundex code of
 * the last name, joined with {@code '|'} (see {@code OwnerMapper.toIdentityKey}). Both the key itself
 * and the soft-match (which compares the Soundex code and postcode) route through this single source of
 * truth, so the two cannot diverge.
 *
 * <p>These are deliberately plain {@code static} utilities in their own class rather than default
 * methods on the MapStruct owner mapper: a {@code String -> String} mapper method would be picked up by
 * MapStruct as an implicit type conversion and wrongly applied to unrelated String properties.
 */
public final class IdentityKeyUtils {

    private IdentityKeyUtils() {
    }

    /**
     * Computes the American Soundex code of a name: the (upper-cased) first letter followed by three
     * digits, zero-padded or truncated to exactly four characters. Consonants are coded 1-6 by group;
     * vowels and {@code H}/{@code W} are not coded. Adjacent letters with the same code, and same-code
     * letters separated only by {@code H} or {@code W}, collapse to a single digit, while same-code
     * letters separated by a vowel are coded separately. Non-letters are ignored.
     *
     * @param name the name to encode, possibly {@code null}
     * @return the four-character Soundex code, or {@code ""} when the name has no letters
     */
    public static String soundex(String name) {
        if (name == null) {
            return "";
        }
        String letters = name.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "";
        }
        StringBuilder code = new StringBuilder();
        code.append(letters.charAt(0));
        char previousDigit = digit(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            char d = digit(c);
            if (d != '0' && d != previousDigit) {
                code.append(d);
            }
            // A vowel resets the "previous digit" so a following same-code consonant is coded again;
            // H and W are transparent, keeping the previous digit so they cannot separate a repeat.
            if (c != 'H' && c != 'W') {
                previousDigit = d;
            }
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    private static char digit(char c) {
        return switch (c) {
            case 'B', 'F', 'P', 'V' -> '1';
            case 'C', 'G', 'J', 'K', 'Q', 'S', 'X', 'Z' -> '2';
            case 'D', 'T' -> '3';
            case 'L' -> '4';
            case 'M', 'N' -> '5';
            case 'R' -> '6';
            default -> '0'; // A, E, I, O, U, H, W, Y
        };
    }

    /**
     * Computes the SHA-256 digest of the UTF-8 bytes of {@code value} and renders it as a lower-case
     * hex string (64 characters). This is the single digest-to-hex routine reused across the
     * application; the upper-case variant simply upper-cases this result.
     *
     * @param value the string to hash
     * @return the full SHA-256 digest as lower-case hex
     */
    public static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    /**
     * Computes the SHA-256 digest of the UTF-8 bytes of {@code value} and renders it as an upper-case
     * hex string (64 characters). Delegates to {@link #sha256Hex(String)} so the digest-to-hex logic
     * lives in one place.
     *
     * @param value the string to hash
     * @return the full SHA-256 digest as upper-case hex
     */
    public static String sha256UpperHex(String value) {
        return sha256Hex(value).toUpperCase(Locale.ROOT);
    }
}
