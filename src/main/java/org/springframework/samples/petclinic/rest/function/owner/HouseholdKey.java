package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.util.IdentityVersion;
import org.springframework.samples.petclinic.util.Sha256;

/**
 * Canonical key identifying a household by its owner's last name and postcode. Two owners
 * belong to the same household when their keys are equal: the last name is normalized
 * (leading/trailing and repeated whitespace collapsed to a single space, then lower-cased) so
 * incidental formatting differences do not hide a match, while the postcode is taken verbatim.
 */
final class HouseholdKey {

    /** Number of leading hex characters of the digest kept as the household id. */
    private static final int ID_LENGTH = 12;

    private HouseholdKey() {
    }

    static String of(String lastName, String postcode) {
        return normalize(lastName) + '|' + (postcode == null ? "" : postcode);
    }

    /**
     * Stable identifier shared by every owner of a household: the first {@value #ID_LENGTH} hex
     * characters of the SHA-256 digest of the canonical {@link #of(String, String) key}, mixed
     * with the version-2 {@link IdentityVersion#TAG identity tag} so no version-1 value recurs.
     * Purely derived from last name and postcode (and the fixed tag), so two owners in the same
     * household always get the same value regardless of creation order.
     */
    static String id(String lastName, String postcode) {
        return Sha256.hex(IdentityVersion.TAG + '|' + of(lastName, postcode)).substring(0, ID_LENGTH);
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
