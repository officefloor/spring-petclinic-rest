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
import java.util.Locale;

/**
 * Helpers for the "household" a pet owner belongs to. Two owners share a household when they have
 * the same last name and postcode; the identity is derived deterministically from the normalized
 * last name (surrounding whitespace trimmed, internal whitespace runs collapsed to a single space,
 * lower-cased) and the postcode, so values that differ only in letter case or spacing of the last
 * name map to one household.
 */
public final class Households {

    /**
     * Fixed version-2 identity tag mixed into every derived identifier (the {@code householdId}, the
     * {@code identityKey} and the region embedded in the {@code memberId}). Because the tag is folded
     * into the hashed input of each identifier, every value differs from its version-1 predecessor and
     * no version-1 value is produced again. The tag is deliberately absent from the user-facing
     * {@code locality}, {@code timezone} and owner-segment region, which stay the plain region code.
     */
    public static final String IDENTITY_VERSION_TAG = "V2";

    private Households() {
    }

    /**
     * Normalizes a value for household comparison: {@code null} becomes an empty string, surrounding
     * whitespace is trimmed, internal runs of whitespace collapse to a single space and the result is
     * lower-cased.
     */
    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * Returns the stable, shared identifier for the household of the owner with the given last name
     * and postcode. The value is the first 12 hex characters of the SHA-256 digest of the fixed
     * {@link #IDENTITY_VERSION_TAG version-2 tag}, a {@code '|'} separator, the normalized last name, a
     * {@code '|'} separator and the postcode, so every owner sharing a last name and postcode (the two
     * values the household is keyed on) is deterministically assigned the same identifier. Folding in
     * the version tag makes every value differ from its version-1 predecessor.
     *
     * @param lastName the owner's last name
     * @param postcode the owner's postcode, or {@code null} when none
     * @return the household identifier
     */
    public static String householdId(String lastName, String postcode) {
        String key = IDENTITY_VERSION_TAG + "|" + normalize(lastName) + "|" + (postcode == null ? "" : postcode);
        return sha256hex(key).substring(0, 12);
    }

    /**
     * Builds an owner's {@code identityKey}: the single derived value all duplicate detection is
     * expressed through. It is the full lower-case hex SHA-256 digest of
     * {@code '<V2>|<normalizedTelephone>|<lowerEmail or empty>|<soundex(lastName)>'}, where {@code <V2>}
     * is the fixed {@link #IDENTITY_VERSION_TAG version-2 tag} folded in so every key differs from its
     * version-1 predecessor (a {@code null}
     * telephone or email contributes an empty segment; the email is lower-cased and the last name is
     * reduced to its {@link #soundex(String) soundex} code). Two owners are duplicates only when their
     * whole identity keys are equal, so household members sharing a last name (hence a soundex) but
     * carrying different telephones have different keys and are not hard duplicates.
     *
     * @param telephone the owner's normalized (E.164) telephone
     * @param email     the owner's normalized email, or {@code null} when none
     * @param lastName  the owner's last name (contributes its soundex code)
     * @return the derived identity key (a 64-character lower-case hex string)
     */
    public static String identityKey(String telephone, String email, String lastName) {
        String key = IDENTITY_VERSION_TAG + "|"
            + (telephone == null ? "" : telephone) + "|"
            + (email == null ? "" : email.toLowerCase(Locale.ROOT)) + "|"
            + soundex(lastName);
        return sha256hex(key);
    }

    /**
     * Computes the American Soundex code of a name: its first letter followed by three digits encoding
     * the significant consonants, padded with {@code '0'} and truncated to four characters. Non-letter
     * characters are ignored and the input is treated case-insensitively, so names differing only in
     * case, spacing or minor spelling map to the same code. A {@code null} or letter-free value yields
     * an empty string.
     *
     * @param value the name to encode
     * @return the four-character Soundex code, or an empty string when there is no letter to encode
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
        char previous = soundexDigit(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            if (c == 'H' || c == 'W') {
                // 'H'/'W' are transparent: they neither code nor reset the previous digit, so two
                // like-coded consonants separated only by them merge into one digit.
                continue;
            }
            char digit = soundexDigit(c);
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
     * Maps a single upper-case letter to its Soundex digit, returning {@code '0'} for vowels and the
     * letters ('A', 'E', 'I', 'O', 'U', 'Y', 'H', 'W') that do not contribute a digit.
     */
    private static char soundexDigit(char c) {
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

    private static String sha256hex(String s) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
