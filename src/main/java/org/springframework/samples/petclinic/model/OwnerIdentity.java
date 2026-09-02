/*
 * Copyright 2016 the original author or authors.
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
 * The single derived duplicate-detection key for an owner: the lower-case SHA-256 hex digest over the
 * normalized telephone, the lower-case email (or empty) and the {@link #soundex soundex} of the last
 * name, joined with '|'. Two owners are duplicates only when this whole key matches.
 */
public final class OwnerIdentity {

    /** Soundex code for each letter A-Z (index c - 'A'); '0' means "not coded". */
    private static final String SOUNDEX_CODES = "01230120022455012623010202";

    private OwnerIdentity() {
    }

    public static String key(Owner owner) {
        String email = owner.getEmail();
        String source = "V2|" + owner.getTelephone() + "|" + (email == null ? "" : email.toLowerCase())
            + "|" + soundex(owner.getLastName());
        StringBuilder sb = new StringBuilder();
        for (byte b : sha256(source)) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /** The American Soundex code (a letter followed by three digits) for the given name, or empty. */
    static String soundex(String name) {
        String letters = name == null ? "" : name.toUpperCase().replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder().append(letters.charAt(0));
        char prev = SOUNDEX_CODES.charAt(letters.charAt(0) - 'A');
        for (int i = 1; i < letters.length() && sb.length() < 4; i++) {
            char c = letters.charAt(i);
            char code = SOUNDEX_CODES.charAt(c - 'A');
            if (code != '0' && code != prev) {
                sb.append(code);
            }
            if (c != 'H' && c != 'W') {
                prev = code;
            }
        }
        while (sb.length() < 4) {
            sb.append('0');
        }
        return sb.toString();
    }

    private static byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
