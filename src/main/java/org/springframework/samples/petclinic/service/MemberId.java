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
package org.springframework.samples.petclinic.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Builds an owner's memberId formatted '&lt;REGION&gt;&lt;FY&gt;&lt;HASH8&gt;&lt;CHK&gt;': the region code
 * derived from the postcode, the 2-digit fiscal year of the registrationDate, the first eight
 * upper-case hex characters of SHA-256 over the owner's normalized telephone concatenated with
 * the last name, and a single Luhn check digit over the digits of '&lt;REGION&gt;&lt;FY&gt;&lt;HASH8&gt;'.
 */
public final class MemberId {

    private MemberId() {
    }

    public static String generate(Collection<Owner> existing, Owner owner) {
        String region = LocalityResolver.locality(owner.getCity(), owner.getPostcode());
        String base = region + FiscalYear.label(owner.getRegistrationDate()).substring(2)
            + hash8(owner.getTelephone() + owner.getLastName());
        return deduplicate(base + CheckDigit.of(base), existing);
    }

    private static String deduplicate(String candidate, Collection<Owner> existing) {
        Set<String> taken = new HashSet<>();
        for (Owner o : existing) {
            taken.add(o.getMemberId());
        }
        String unique = candidate;
        for (int n = 2; taken.contains(unique); n++) {
            unique = candidate + "-" + n;
        }
        return unique;
    }

    private static String hash8(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                hex.append(String.format("%02X", digest[i]));
            }
            return hex.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
