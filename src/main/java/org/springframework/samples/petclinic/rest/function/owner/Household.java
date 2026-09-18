package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.util.OwnerIdentityVersion;
import org.springframework.samples.petclinic.util.Sha256;

/**
 * Identity of an owner's household. Two owners live in the same household when they share the
 * same last name and postcode. The last name is compared case-insensitively with collapsed
 * whitespace and the postcode as supplied.
 *
 * <p>The {@link #id(String, String) household id} is derived deterministically from that
 * canonical key, so every owner with the same last name and postcode computes the same stable
 * identifier automatically — no owner has to opt in for the link to form.
 */
final class Household {

    /** Number of leading hex characters of the digest that make up the household id. */
    private static final int ID_LENGTH = 12;

    private Household() {
    }

    /**
     * Canonical household key: the {@link OwnerIdentityVersion#TAG version tag} then the last name
     * trimmed, internal whitespace collapsed and lower-cased, joined to the postcode with
     * {@code '|'} so distinct pairs never collide. The version tag is mixed in so a version-2
     * household id can never repeat a version-1 one.
     */
    static String key(String lastName, String postcode) {
        return OwnerIdentityVersion.TAG + "|" + canonical(lastName) + "|" + orEmpty(postcode);
    }

    /**
     * Stable identifier shared by every owner in the household: the first {@value #ID_LENGTH} hex
     * characters of the SHA-256 digest of the canonical {@link #key(String, String) key}, so any
     * two owners with the same last name and postcode derive the same value.
     */
    static String id(String lastName, String postcode) {
        return Sha256.hex(key(lastName, postcode)).substring(0, ID_LENGTH);
    }

    /** Case-insensitive form with leading/trailing and repeated internal whitespace collapsed. */
    private static String canonical(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
