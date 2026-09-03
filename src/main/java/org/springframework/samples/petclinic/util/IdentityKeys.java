/*
 * Copyright 2002-2013 the original author or authors.
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

import org.springframework.samples.petclinic.model.Owner;

/**
 * The owner identity key: the SHA-256 hex of
 * {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}. Duplicate detection
 * compares this key; the soft-match compares {@link #soundex(String)} of the last name.
 */
public final class IdentityKeys {

    private IdentityKeys() {
    }

    /** The version-2 identity key of the given owner: the SHA-256 input mixes in the fixed 'V2' version
     *  tag so no key produced under version 1 recurs. */
    public static String of(Owner owner) {
        String raw = "V2" + orEmpty(owner.getTelephone()) + '|' + orEmpty(owner.getEmail()) + '|' + soundex(owner.getLastName());
        return sha256Hex(raw);
    }

    /** The American Soundex code of {@code name} (letter + 3 digits), or empty when there is no letter. */
    public static String soundex(String name) {
        String letters = (name == null ? "" : name.toUpperCase(Locale.ROOT)).replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "";
        }
        StringBuilder out = new StringBuilder().append(letters.charAt(0));
        char prev = code(letters.charAt(0));
        for (int i = 1; i < letters.length() && out.length() < 4; i++) {
            char c = letters.charAt(i);
            char digit = code(c);
            if (digit != '0' && digit != prev) {
                out.append(digit);
            }
            if (c != 'H' && c != 'W') {
                prev = digit;
            }
        }
        while (out.length() < 4) {
            out.append('0');
        }
        return out.toString();
    }

    private static char code(char c) {
        return switch (c) {
            case 'B', 'F', 'P', 'V' -> '1';
            case 'C', 'G', 'J', 'K', 'Q', 'S', 'X', 'Z' -> '2';
            case 'D', 'T' -> '3';
            case 'L' -> '4';
            case 'M', 'N' -> '5';
            case 'R' -> '6';
            default -> '0';
        };
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
