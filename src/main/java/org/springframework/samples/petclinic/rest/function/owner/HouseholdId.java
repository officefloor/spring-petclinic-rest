package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

/**
 * Derives an owner's {@code householdId}: a stable identifier shared by every owner with the same
 * last name and postcode (one household). The last name is normalized (trim, collapse runs of
 * whitespace to a single space, lower-case) and joined to the postcode with a {@code '|'} separator
 * before hashing, so owners with the same last name and postcode — including one created with
 * {@code sharesHousehold} true, which by definition matches an existing owner's last name and
 * postcode — derive the identical value. This is the version-2 derivation: the version-2 region code
 * (see {@link RegionCode#v2FromPostcode(String)}, which mixes the fixed {@code "V2"} tag into the
 * postcode-derived region so it is city-independent and the shared-household invariant is preserved)
 * is mixed in ahead of the last name and postcode. Computed as the first 12 upper-case hex characters
 * of SHA-256 over {@code regionV2 + '|' + normalizedLastName + '|' + postcode}, so it is stable across
 * requests, differs from every version-1 value, and needs no persisted column.
 */
public final class HouseholdId {

    private HouseholdId() {
    }

    public static String of(String lastName, String postcode) {
        String region = RegionCode.v2FromPostcode(postcode);
        String key = region + "|" + normalize(lastName) + "|" + (postcode == null ? "" : postcode);
        return Sha256.hex(key).substring(0, 12).toUpperCase(Locale.ROOT);
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
