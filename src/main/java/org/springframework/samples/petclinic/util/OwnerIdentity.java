/*
 * Copyright 2016-2017 the original author or authors.
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

/**
 * Shared helpers for an owner's duplicate-detection identity: the derived identity key and the
 * Soundex reduction of a surname it is built from. Kept in one place so the create endpoint (which
 * computes the key for duplicate/soft-match detection) and the mapper (which exposes it on the
 * response) agree exactly.
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    /**
     * The owner's derived duplicate-detection identity key: the lower-case SHA-256 hex digest over
     * {@code '<normalizedTelephone>|<lowerEmail>|<soundex(lastName)>'}. A {@code null} telephone or
     * email contributes the empty string, the email is lower-cased so the comparison is
     * case-insensitive, and the last name is reduced to its Soundex code so near-identical surnames
     * collide.
     */
    public static String identityKey(String normalizedTelephone, String email, String lastName) {
        String telephone = normalizedTelephone == null ? "" : normalizedTelephone;
        String emailKey = email == null ? "" : email.toLowerCase();
        return sha256Hex(telephone + "|" + emailKey + "|" + soundex(lastName));
    }

    /**
     * The full lower-case SHA-256 hex digest of the UTF-8 bytes of {@code input}.
     */
    public static String sha256Hex(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * The American Soundex code of {@code name}: the upper-cased first letter followed by up to three
     * digits derived from the remaining letters (b,f,p,v -> 1; c,g,j,k,q,s,x,z -> 2; d,t -> 3; l -> 4;
     * m,n -> 5; r -> 6), where adjacent letters sharing a code (directly, or separated only by 'h' or
     * 'w') collapse to a single digit and vowels act as separators, right-padded with zeros to length
     * four. A {@code null} or letter-free input yields the empty string.
     */
    public static String soundex(String name) {
        if (name == null) {
            return "";
        }
        StringBuilder letters = new StringBuilder();
        for (char c : name.toUpperCase().toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                letters.append(c);
            }
        }
        if (letters.length() == 0) {
            return "";
        }
        StringBuilder code = new StringBuilder().append(letters.charAt(0));
        char previous = soundexCode(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char letter = letters.charAt(i);
            char digit = soundexCode(letter);
            if (digit != '0' && digit != previous) {
                code.append(digit);
            }
            if (letter != 'H' && letter != 'W') {
                previous = digit;
            }
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    private static char soundexCode(char c) {
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
}
