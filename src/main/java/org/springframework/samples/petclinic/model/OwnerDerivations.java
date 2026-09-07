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
import java.time.LocalDate;
import java.time.Period;
import java.util.Map;

/**
 * Pure derivations of an {@link Owner}'s read-only attributes.
 *
 * <p>Each method computes one derived value from an owner's own field values and
 * nothing else, so the computations stay stateless and side-effect free. {@link Owner}
 * exposes these through thin {@code @Transient} getters that pass in the relevant
 * fields; keeping the arithmetic here rather than on the entity leaves {@code Owner}
 * to describe its persistent state and relationships while its growing family of
 * derived attributes lives together in one place.
 */
final class OwnerDerivations {

    /** Fixed city-to-region table used to derive {@link #region}. */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region -> inclusive 4-digit postcode range {low, high} used to derive
     *  {@link #region} in preference to the city. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    private OwnerDerivations() {
    }

    /**
     * The owner's locality, i.e. the canonical region it belongs to. This is read
     * from the region-and-hash {@code customerCode} identity: the region is the
     * {@code <REGION>} component that precedes the first {@code '-'} of the assigned
     * customer code (e.g. {@code NSW} for {@code NSW-9F86D081}). Until an owner has
     * been assigned a customer code (for example while its create request is still
     * being validated) this falls back to the region derived directly from its own
     * fields (see {@link #region(String, String)}).
     *
     * @return the canonical region string, or {@code "UNKNOWN"}
     */
    static String locality(String customerCode, String postcode, String city) {
        if (customerCode != null) {
            int dash = customerCode.indexOf('-');
            if (dash >= 0) {
                return customerCode.substring(0, dash);
            }
        }
        return region(postcode, city);
    }

    /**
     * The canonical region derived for an owner from its own fields. The postcode is
     * consulted first: when it is present and falls within a known region's inclusive
     * 4-digit range ({@code NSW 2000-2099}, {@code VIC 3000-3099}, {@code QLD
     * 4000-4099}) that region is returned. Only when the postcode is absent or in no
     * known range does this fall back to the fixed city-to-region table
     * ({@code Sydney -> NSW}, {@code Melbourne -> VIC}, {@code Brisbane -> QLD}),
     * returning {@code "UNKNOWN"} when the city is not in the table either. This yields
     * the same region for the known cities while disambiguating cities that share a name
     * via their postcode.
     *
     * @return the canonical region string, or {@code "UNKNOWN"}
     */
    static String region(String postcode, String city) {
        String byPostcode = regionForPostcode(postcode);
        if (byPostcode != null) {
            return byPostcode;
        }
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

    /**
     * The region whose inclusive postcode range contains the given postcode, or
     * {@code null} when the postcode is absent, not a four-digit number, or in no
     * known range.
     */
    private static String regionForPostcode(String postcode) {
        if (postcode == null || !postcode.matches("\\d{4}")) {
            return null;
        }
        int value = Integer.parseInt(postcode);
        for (Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Whether the given {@code postcode} is acceptable for the given {@code region}: an
     * absent postcode is always accepted, a present postcode must be exactly four
     * digits and, when the region has a known inclusive postcode range
     * ({@code NSW 2000-2099}, {@code VIC 3000-3099}, {@code QLD 4000-4099}), fall within
     * it. A region with no known range (for example {@code "UNKNOWN"}) accepts any
     * four-digit postcode.
     *
     * @return {@code true} if the postcode is absent or valid for the region
     */
    static boolean postcodeMatchesRegion(String postcode, String region) {
        if (postcode == null) {
            return true;
        }
        if (!postcode.matches("\\d{4}")) {
            return false;
        }
        int[] range = REGION_POSTCODES.get(region);
        if (range == null) {
            return true;
        }
        int value = Integer.parseInt(postcode);
        return value >= range[0] && value <= range[1];
    }

    /**
     * The owner's membership level, a number from 1 to 3 assigned at creation:
     * starting at 1, add 1 when an {@code email} is present and add 1 when
     * {@code namesakeCount} is 0, capped at 3 (level 4 is reserved for tenure).
     *
     * @return the membership level, from 1 to 3
     */
    static int membershipLevel(String email, Integer namesakeCount) {
        int level = 1;
        if (email != null && !email.isBlank()) {
            level++;
        }
        if (namesakeCount != null && namesakeCount == 0) {
            level++;
        }
        return Math.min(level, 3);
    }

    /**
     * The owner's preferred contact channel: {@code "EMAIL"} when an {@code email}
     * is present, otherwise {@code "PHONE"}.
     *
     * @return {@code "EMAIL"} or {@code "PHONE"}
     */
    static String contactPreference(String email) {
        if (email != null && !email.isBlank()) {
            return "EMAIL";
        }
        return "PHONE";
    }

    /**
     * The Luhn check digit (0-9) computed over the decimal digits contained in the
     * given {@code customerCode}, processed right-to-left with every second digit
     * doubled (and reduced by 9 when the double exceeds 9). Non-digit characters are
     * ignored, and an absent or digit-free customer code yields {@code 0}.
     *
     * @return the Luhn check digit, from 0 to 9
     */
    static int checkDigit(String customerCode) {
        int sum = 0;
        boolean doubleDigit = true;
        for (int i = (customerCode == null ? 0 : customerCode.length()) - 1; i >= 0; i--) {
            char c = customerCode.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int d = c - '0';
            if (doubleDigit) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            doubleDigit = !doubleDigit;
        }
        return (10 - (sum % 10)) % 10;
    }

    /**
     * The owner's age band, derived from its {@code birthDate} measured against its
     * {@code registrationDate}: {@code "MINOR"} when the owner is under 18 on the
     * registration date, {@code "ADULT"} from 18 to 64, and {@code "SENIOR"} at 65 or
     * over. The age is the number of whole years between the two dates. Returns
     * {@code null} when either date is absent, so no band is reported for an owner
     * without a supplied birth date.
     *
     * @return {@code "MINOR"}, {@code "ADULT"}, {@code "SENIOR"}, or {@code null}
     */
    static String ageBand(LocalDate birthDate, LocalDate registrationDate) {
        if (birthDate == null || registrationDate == null) {
            return null;
        }
        int age = Period.between(birthDate, registrationDate).getYears();
        if (age < 18) {
            return "MINOR";
        }
        if (age < 65) {
            return "ADULT";
        }
        return "SENIOR";
    }

    /**
     * The single derived key used for duplicate detection, formed as
     * {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}.
     * The telephone is the owner's stored (E.164-normalised) telephone, the email
     * is the stored (lower-cased) email or the empty string when absent, and the
     * household component is the stored {@code householdId} or the empty string
     * when the owner belongs to no household.
     *
     * @return the owner's identity key
     */
    static String identityKey(String telephone, String email, String householdId) {
        String tel = telephone == null ? "" : telephone;
        String mail = email == null ? "" : email;
        String household = householdId == null ? "" : householdId;
        return tel + "|" + mail + "|" + household;
    }

    /**
     * The owner's customer code, formatted {@code <REGION>-<HASH8>}: {@code region} is
     * the owner's canonical region (see {@link #region(String, String)}) and HASH8 is the
     * first eight upper-case hexadecimal characters of the SHA-256 digest of the owner's
     * normalised telephone concatenated with its last name (e.g. {@code NSW-9F86D081}).
     *
     * @return the customer code
     */
    static String customerCode(String region, String telephone, String lastName) {
        String hash8 = sha256Hex(telephone + lastName).substring(0, 8).toUpperCase();
        return String.format("%s-%s", region, hash8);
    }

    /**
     * The stable identifier for the household an owner belongs to, derived
     * deterministically from its {@code normalizedLastName} and {@code postcode} so that
     * every owner sharing a last name and postcode resolves to the same value. It is the
     * first twelve lower-case hexadecimal characters of the SHA-256 digest of
     * {@code normalizedLastName + '|' + postcode} (an absent postcode contributes the
     * empty string).
     *
     * @return the household identifier
     */
    static String householdId(String normalizedLastName, String postcode) {
        String pc = postcode == null ? "" : postcode;
        return sha256Hex(normalizedLastName + "|" + pc).substring(0, 12);
    }

    /**
     * The SHA-256 digest of the UTF-8 bytes of the given value, as a lower-case
     * hexadecimal string. Callers take the prefix and case they need.
     */
    static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required but unavailable", e);
        }
    }
}
