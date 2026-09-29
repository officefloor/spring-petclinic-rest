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

package org.springframework.samples.petclinic.rest.validation;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Utility for deriving an owner's {@code identityKey}, the single value into which all
 * duplicate detection is consolidated. The key is the lower-case hexadecimal SHA-256 digest of
 * {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}: the owner's telephone
 * in {@link TelephoneNormalizer E.164} form, the lower-cased e-mail address (or an empty string
 * when absent), and the {@link Soundex Soundex} code of the last name.
 *
 * <p>Because the telephone is part of the key, two owners with the same last name and postcode but
 * different telephones have different identity keys; only an exact whole-key match between a new
 * owner and an existing one is a hard duplicate. Owners that merely share the last name's Soundex
 * code and postcode are surfaced as a soft match instead.
 */
public final class IdentityKeyNormalizer {

    private IdentityKeyNormalizer() {
    }

    /**
     * Derive the owner's identity key.
     *
     * @param owner the owner (may be {@code null})
     * @return the derived identity key, or {@code null} when the owner is {@code null}
     */
    public static String derive(Owner owner) {
        if (owner == null) {
            return null;
        }
        String telephone = TelephoneNormalizer.toComparable(owner.getTelephone());
        String email = owner.getEmail() == null ? "" : owner.getEmail().toLowerCase(Locale.ROOT);
        String soundex = Soundex.encode(owner.getLastName());
        return sha256Hex(telephone + "|" + email + "|" + soundex);
    }

    /**
     * Returns the full lower-case hexadecimal SHA-256 digest of the UTF-8 bytes of {@code value}.
     */
    private static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required but unavailable", e);
        }
    }
}
