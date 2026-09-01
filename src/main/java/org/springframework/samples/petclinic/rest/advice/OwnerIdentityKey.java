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

package org.springframework.samples.petclinic.rest.advice;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Derives an owner's {@code identityKey}, the single value all duplicate detection is based on:
 * {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}. Kept as a small
 * standalone unit so the request advice and the response mapper derive the key identically.
 */
public final class OwnerIdentityKey {

    private OwnerIdentityKey() {
    }

    static String of(OwnerFieldsDto owner) {
        return of(owner.getTelephone(), owner.getEmail(), owner.getLastName(), owner.getPostcode());
    }

    public static String of(Owner owner) {
        return of(owner.getTelephone(), owner.getEmail(), owner.getLastName(), owner.getPostcode());
    }

    static String of(String telephone, String email, String lastName, String postcode) {
        return telephone(telephone) + '|' + email(email) + '|' + householdId(lastName, postcode);
    }

    static String telephone(String telephone) {
        return OwnerTelephoneNormalizationAdvice.toE164(telephone);
    }

    static String email(String email) {
        return (email == null || email.isBlank()) ? "" : email.strip().toLowerCase(Locale.ROOT);
    }

    /**
     * Stable household identifier: the first 12 hex characters of SHA-256 over
     * {@code normalizedLastName + '|' + postcode}, so owners with the same last name and postcode
     * share it automatically.
     */
    public static String householdId(String lastName, String postcode) {
        String normalized = lastName.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT)
            + '|' + (postcode == null ? "" : postcode.trim());
        return sha256Hex(normalized).substring(0, 12);
    }

    private static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
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
}
