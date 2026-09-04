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
package org.springframework.samples.petclinic.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Derives an owner's duplicate-detection identity key: the SHA-256 hex of the normalized
 * telephone, lower-case email and {@link #soundex(String) soundex} of the last name, joined
 * with {@code '|'}. Two owners are the same identity only when this whole key matches.
 */
public final class IdentityKey {

    /** Soundex code for A..Z: vowels, H, W and Y map to 0 (non-coding). */
    private static final String CODES = "01230120022455012623010202";

    private IdentityKey() {
    }

    /** SHA-256 hex over {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}. */
    public static String of(String telephone, String email, String lastName) {
        String key = (telephone == null ? "" : telephone) + "|"
            + (email == null ? "" : email.toLowerCase()) + "|"
            + soundex(lastName);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
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

    /** Standard Soundex code (an initial letter followed by three digits) for the given name. */
    public static String soundex(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder letters = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = Character.toUpperCase(value.charAt(i));
            if (c >= 'A' && c <= 'Z') {
                letters.append(c);
            }
        }
        if (letters.length() == 0) {
            return "0000";
        }
        StringBuilder out = new StringBuilder().append(letters.charAt(0));
        int prev = code(letters.charAt(0));
        for (int i = 1; i < letters.length() && out.length() < 4; i++) {
            int c = code(letters.charAt(i));
            if (c > 0 && c != prev) {
                out.append((char) ('0' + c));
            }
            prev = c;
        }
        while (out.length() < 4) {
            out.append('0');
        }
        return out.toString();
    }

    private static int code(char c) {
        return CODES.charAt(c - 'A') - '0';
    }
}
