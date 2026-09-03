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
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Builds and reads the unified {@code <REGION><FY><HASH8><CHK>} owner member id: REGION is the
 * region resolved from the postcode, FY is the 2-digit fiscal year of the registration date, HASH8
 * is the first 8 upper-case hex characters of SHA-256 over (normalizedTelephone + lastName), and CHK
 * is a single Luhn check digit over the digits of {@code <REGION><FY><HASH8>}.
 */
public final class MemberIds {

    private MemberIds() {
    }

    /**
     * Build the {@code <REGION><FY><HASH8><CHK>} member id under the version-2 algorithm: the visible
     * REGION prefix stays the plain region (so 'locality' reads back plain), while HASH8 mixes the fixed
     * 'V2' version tag and the region into the hashed material so the id differs from every version-1 id.
     */
    public static String build(String city, String postcode, String telephone, String lastName, LocalDate registrationDate) {
        String region = Localities.regionFor(city, postcode);
        String body = region
            + String.format("%02d", FiscalYears.startYear(registrationDate) % 100)
            + hash8("V2" + region + telephone + lastName);
        return body + CheckDigits.luhn(body);
    }

    /**
     * Build the member id, then de-duplicate it against {@code existingOwners} by appending
     * {@code -<n>} with the smallest {@code n >= 2} that makes it unique.
     */
    public static String buildUnique(String city, String postcode, String telephone, String lastName,
            LocalDate registrationDate, Collection<Owner> existingOwners) {
        String base = build(city, postcode, telephone, lastName, registrationDate);
        Set<String> taken = new HashSet<>();
        for (Owner owner : existingOwners) {
            taken.add(owner.getMemberId());
        }
        String candidate = base;
        for (int n = 2; taken.contains(candidate); n++) {
            candidate = base + "-" + n;
        }
        return candidate;
    }

    /** The region portion of a member id (its leading run of letters), or null when absent. */
    public static String regionOf(String memberId) {
        if (memberId == null) {
            return null;
        }
        int end = 0;
        while (end < memberId.length() && Character.isLetter(memberId.charAt(end))) {
            end++;
        }
        return memberId.substring(0, end);
    }

    /** The {@code FY<YY>} label carried by the member id (its 2 fiscal-year digits after the region), or null when absent. */
    public static String fiscalYearLabel(String memberId) {
        String region = regionOf(memberId);
        return region == null ? null : "FY" + memberId.substring(region.length(), region.length() + 2);
    }

    private static String hash8(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return String.format("%02X%02X%02X%02X", digest[0], digest[1], digest[2], digest[3]);
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
