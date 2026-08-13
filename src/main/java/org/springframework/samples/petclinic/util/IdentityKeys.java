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
 * Derives an owner's consolidated identity key and the phonetic (Soundex) encoding it is built on.
 *
 * <p>The {@link #identityKey(String, String, String) identityKey} is the SHA-256, rendered as
 * lower-case hex, of the fixed {@value #VERSION_TAG} version tag, the owner's normalized telephone,
 * lower-cased email and the {@link #soundex(String) Soundex} of its last name, joined with {@code
 * '|'} separators. It is the single key both the duplicate-detection check and the exposed {@code
 * identityKey} field are computed from, so the two never diverge.
 */
public final class IdentityKeys {

    /**
     * The fixed version tag mixed into every version-2 identifier so no value produced under
     * version 1 is produced again.
     */
    public static final String VERSION_TAG = "V2";

    private IdentityKeys() {
    }

    /**
     * Derives an owner's {@code identityKey}: the lower-case hex SHA-256 of
     * {@code "V2" + '|' + normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}. A
     * {@code null} telephone or email contributes an empty segment; the last name is always
     * contributed via its {@link #soundex(String) Soundex} encoding (empty when the last name is
     * absent). The leading {@value #VERSION_TAG} version tag is the version-2 identity marker.
     *
     * @param telephone the owner's normalized (E.164) telephone, may be {@code null}
     * @param email     the owner's lower-cased email, may be {@code null}
     * @param lastName  the owner's last name, may be {@code null}
     * @return the 64-character lower-case hex identity key
     */
    public static String identityKey(String telephone, String email, String lastName) {
        String input = VERSION_TAG
            + "|" + (telephone == null ? "" : telephone)
            + "|" + (email == null ? "" : email)
            + "|" + soundex(lastName);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    /**
     * Computes the American Soundex code of a name: the retained first letter followed by up to
     * three digits encoding the following consonants (b,f,p,v-&gt;1; c,g,j,k,q,s,x,z-&gt;2; d,t-&gt;3;
     * l-&gt;4; m,n-&gt;5; r-&gt;6), zero-padded to four characters. Adjacent letters with the same
     * code collapse to one; vowels (and y) separate same-coded consonants, while h and w are
     * transparent and do not. A {@code null} or letter-free name yields an empty string.
     *
     * @param name the name to encode, may be {@code null}
     * @return the four-character Soundex code, or an empty string when there is no letter to encode
     */
    public static String soundex(String name) {
        if (name == null) {
            return "";
        }
        StringBuilder letters = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isLetter(c)) {
                letters.append(Character.toUpperCase(c));
            }
        }
        if (letters.length() == 0) {
            return "";
        }
        StringBuilder code = new StringBuilder();
        code.append(letters.charAt(0));
        char previousCode = mappingCode(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char letter = letters.charAt(i);
            if (letter == 'H' || letter == 'W') {
                // Transparent: does not separate same-coded consonants, does not reset the run.
                continue;
            }
            char mapped = mappingCode(letter);
            if (mapped == '0') {
                // A vowel (or y) separates otherwise-identical codes.
                previousCode = '0';
                continue;
            }
            if (mapped != previousCode) {
                code.append(mapped);
            }
            previousCode = mapped;
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    /**
     * Maps a single upper-case letter to its Soundex digit ('0' for vowels, y, h and w).
     */
    private static char mappingCode(char letter) {
        return switch (letter) {
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
