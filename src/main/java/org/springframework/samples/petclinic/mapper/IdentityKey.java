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

package org.springframework.samples.petclinic.mapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * Derives an owner's {@code identityKey}: the full lower-case hexadecimal SHA-256 digest of
 * {@code V2 + '|' + normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}, where the
 * leading {@code V2} is the fixed version-2 tag ({@link OwnerLocality#IDENTITY_VERSION_TAG}) mixed in
 * so no version-1 identity key is reproduced. The telephone is
 * expected to already be in its normalized (E.164) form; the email is lower-cased and the last
 * name is reduced to its American Soundex code here, so two owners whose surnames sound alike
 * share the {@code soundex(lastName)} component. A {@code null} telephone or email contributes the
 * empty string. This single key drives both duplicate detection (a 409 on an exact key match) and
 * the {@code identityKey} field returned on the owner representation.
 */
public final class IdentityKey {

    private IdentityKey() {
    }

    /**
     * Builds the {@code identityKey} from an owner's normalized telephone, email and last name. The
     * telephone is used as-is (empty when {@code null}), the email is lower-cased (empty when
     * {@code null}) and the last name is reduced to its Soundex code, then the three parts are joined
     * with {@code '|'} and hashed with SHA-256.
     */
    public static String of(String telephone, String email, String lastName) {
        String tel = telephone == null ? "" : telephone;
        String mail = email == null ? "" : email.toLowerCase(Locale.ROOT);
        return sha256Hex(OwnerLocality.IDENTITY_VERSION_TAG + "|" + tel + "|" + mail + "|" + soundex(lastName));
    }

    /**
     * Returns the American Soundex code of {@code value}: the upper-cased first letter followed by
     * three digits derived from the remaining letters (B,F,P,V-&gt;1; C,G,J,K,Q,S,X,Z-&gt;2; D,T-&gt;3;
     * L-&gt;4; M,N-&gt;5; R-&gt;6). Vowels and {@code Y} separate codes; {@code H} and {@code W} do
     * not, so letters they separate are treated as adjacent. Adjacent letters mapping to the same
     * code are coded once. The result is padded with zeros / truncated to four characters. A value
     * with no letters (or {@code null}) yields the empty string.
     */
    public static String soundex(String value) {
        if (value == null) {
            return "";
        }
        String letters = value.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "";
        }
        StringBuilder code = new StringBuilder();
        code.append(letters.charAt(0));
        char previous = digitFor(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char letter = letters.charAt(i);
            if (letter == 'H' || letter == 'W') {
                continue;
            }
            char digit = digitFor(letter);
            if (digit != '0' && digit != previous) {
                code.append(digit);
            }
            previous = digit;
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.substring(0, 4);
    }

    /**
     * Maps a single upper-case letter to its Soundex digit, or {@code '0'} for a letter that is not
     * coded (a vowel, {@code Y}, {@code H} or {@code W}).
     */
    private static char digitFor(char letter) {
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

    /**
     * Returns the full SHA-256 digest of {@code input}, rendered as lower-case hexadecimal.
     */
    private static String sha256Hex(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is required but unavailable", ex);
        }
    }
}
