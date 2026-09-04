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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Builds an owner's unified {@code memberId}, formatted
 * {@code <REGION><FY><HASH8><CHK>}: the region derived from the owner's postcode,
 * the 2-digit fiscal year of the registration date, the first 8 upper-case hex
 * characters of {@code SHA-256(normalizedTelephone + lastName)} and a single Luhn
 * check digit over the digits of {@code <REGION><FY><HASH8>}, e.g.
 * {@code NSW261A2B3C4D5}.
 */
public final class MemberId {

    private MemberId() {
    }

    /**
     * @param owner          the owner being coded (postcode, telephone, last name and
     *                       registration date are used)
     * @param existingOwners the current owners, used only for collision handling
     * @return the formatted member id, e.g. {@code NSW261A2B3C4D5}
     */
    public static String of(Owner owner, Collection<Owner> existingOwners) {
        String region = Locality.of(owner.getCity(), owner.getPostcode());
        String base = region + String.format("%02d", FiscalYear.of(owner.getRegistrationDate()) % 100)
            + hash8(owner.getTelephone() + owner.getLastName());
        return deduplicate(base + CheckDigit.of(base), existingOwners);
    }

    /** The leading {@code <REGION>} letters of a member id, or {@code "UNKNOWN"} when absent. */
    public static String regionOf(String memberId) {
        if (memberId == null) {
            return "UNKNOWN";
        }
        int i = 0;
        while (i < memberId.length() && Character.isLetter(memberId.charAt(i))) {
            i++;
        }
        return i == 0 ? "UNKNOWN" : memberId.substring(0, i);
    }

    /** Appends {@code -<n>} with the smallest {@code n >= 2} that avoids any existing
     *  owner's member id, or returns {@code base} unchanged when already unique. */
    private static String deduplicate(String base, Collection<Owner> existingOwners) {
        Set<String> taken = new HashSet<>();
        for (Owner existing : existingOwners) {
            taken.add(existing.getMemberId());
        }
        String unique = base;
        for (int n = 2; taken.contains(unique); n++) {
            unique = base + "-" + n;
        }
        return unique;
    }

    private static String hash8(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
