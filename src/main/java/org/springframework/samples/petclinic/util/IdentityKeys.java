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

/**
 * Derivation of an owner's duplicate-detection {@code identityKey}. The version-2 key is the
 * lower-case hex SHA-256 of
 * {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName) + '|' + "V2"}, with an
 * empty segment substituted for an absent telephone or email. Because a Soundex code (not the raw surname)
 * and the telephone both feed the key, two owners collide only when their telephone, email and the
 * phonetic code of their surname all match; owners that differ in any one of those segments — for
 * example two members of the same household with different telephones — produce different keys.
 *
 * <p>Both the create endpoint (for its single duplicate check) and the owner mapper (which exposes
 * the key on the DTO) derive the key here, so the value a caller sees is exactly the value the
 * server compares against.
 */
public final class IdentityKeys {

    private IdentityKeys() {
    }

    /**
     * Derives the {@code identityKey} from an owner's already-normalized telephone and email and its
     * raw last name (which is reduced to its Soundex code here).
     *
     * @param normalizedTelephone the E.164 telephone, or {@code null}
     * @param lowerEmail the lower-cased email, or {@code null}
     * @param lastName the last name, or {@code null}
     * @return the 64-character lower-case hex identity key
     */
    public static String identityKey(String normalizedTelephone, String lowerEmail, String lastName) {
        String raw = (normalizedTelephone == null ? "" : normalizedTelephone)
            + "|" + (lowerEmail == null ? "" : lowerEmail)
            + "|" + soundex(lastName)
            + "|" + VERSION_TAG;
        return sha256Hex(raw);
    }

    /**
     * The fixed version-2 tag mixed into every derived identifier so that no value produced under
     * version 1 is ever reproduced. It appears only inside the identifiers, never in the user-facing
     * {@code locality}, {@code timezone} or owner-segment region.
     */
    public static final String VERSION_TAG = "V2";

    /**
     * The American Soundex phonetic code of {@code name}: its first letter followed by three digits
     * encoding the remaining consonants, upper-cased and padded/truncated to four characters. Letters
     * that share an encoding stay a single digit when adjacent or separated only by {@code h}/{@code w};
     * a vowel between them re-encodes the digit. Non-letters are ignored; an empty or letter-free name
     * yields the empty string.
     *
     * @param name the name to encode, may be {@code null}
     * @return the Soundex code, or the empty string when the name has no letters
     */
    public static String soundex(String name) {
        if (name == null) {
            return "";
        }
        StringBuilder letters = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = Character.toUpperCase(name.charAt(i));
            if (c >= 'A' && c <= 'Z') {
                letters.append(c);
            }
        }
        if (letters.length() == 0) {
            return "";
        }
        StringBuilder code = new StringBuilder();
        code.append(letters.charAt(0));
        char previous = digit(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            if (c == 'H' || c == 'W') {
                continue;
            }
            char d = digit(c);
            if (d != '0' && d != previous) {
                code.append(d);
            }
            previous = d;
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    /**
     * The Soundex digit for a single upper-case letter, or {@code '0'} for a vowel or other letter
     * that carries no code.
     */
    private static char digit(char c) {
        switch (c) {
            case 'B': case 'F': case 'P': case 'V':
                return '1';
            case 'C': case 'G': case 'J': case 'K': case 'Q': case 'S': case 'X': case 'Z':
                return '2';
            case 'D': case 'T':
                return '3';
            case 'L':
                return '4';
            case 'M': case 'N':
                return '5';
            case 'R':
                return '6';
            default:
                return '0';
        }
    }

    /**
     * The full lower-case hex SHA-256 of the UTF-8 bytes of {@code value}.
     *
     * @param value the string to hash
     * @return the 64-character lower-case hex digest
     */
    public static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
