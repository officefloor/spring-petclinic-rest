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
import java.time.LocalDate;
import java.util.Map;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's unified {@code memberId} '<REGION>V2<FY><HASH8><CHK>': the region code (from the
 * postcode band, else "UNKNOWN") carrying the fixed 'V2' identity-version tag, the 2-digit fiscal
 * year of the registration date, the first 8
 * upper-hex characters of SHA-256 over (normalizedTelephone + lastName), and a single Luhn check
 * digit over the digits of the preceding segments. Kept as a small standalone unit so the assigning
 * aspect, the response mapper and the segment rule all derive the same identity.
 */
public final class OwnerMemberId {

    private OwnerMemberId() {
    }

    /** Postcode band (4-digit / 100) -> region code. */
    private static final Map<Integer, String> REGION = Map.of(20, "NSW", 30, "VIC", 40, "QLD");

    /** The region code derived from the owner's postcode band, or "UNKNOWN". */
    public static String region(Owner owner) {
        String pc = owner.getPostcode();
        int band = pc != null && pc.matches("[0-9]{4}") ? Integer.parseInt(pc) / 100 : -1;
        return REGION.getOrDefault(band, "UNKNOWN");
    }

    /** The base memberId before any collision suffix, or {@code null} when the date is absent. */
    public static String base(Owner owner) {
        LocalDate registered = owner.getRegistrationDate();
        if (registered == null) {
            return null;
        }
        String body = region(owner) + "V2" + String.format("%02d", OwnerFiscalYear.yearOf(registered) % 100)
            + hash8(owner);
        return body + OwnerCheckDigit.luhn(body);
    }

    private static String hash8(Owner owner) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256")
                .digest((owner.getTelephone() + owner.getLastName()).getBytes(StandardCharsets.UTF_8));
            return String.format("%02X%02X%02X%02X", d[0], d[1], d[2], d[3]);
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
