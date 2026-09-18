package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.util.Sha256;

/**
 * Identity of an owner's household. Two owners live in the same household when they share
 * the same last name and address. The address is compared in its {@link OwnerAddress
 * normalized} form and the last name case-insensitively with collapsed whitespace.
 *
 * <p>The {@link #id(String, String) household id} is derived deterministically from that
 * canonical key, so every member of a household computes the same stable identifier without
 * needing to coordinate.
 */
final class Household {

    private Household() {
    }

    /**
     * Canonical household key: the last name trimmed, internal whitespace collapsed and
     * lower-cased, and the address in its {@link OwnerAddress normalized} form, joined so
     * distinct pairs never collide.
     */
    static String key(String lastName, String address) {
        return canonical(lastName) + "\n" + OwnerAddress.normalize(address);
    }

    /** Whether the two (last name, address) pairs belong to the same household. */
    static boolean same(String lastNameA, String addressA, String lastNameB, String addressB) {
        return key(lastNameA, addressA).equals(key(lastNameB, addressB));
    }

    /**
     * Stable identifier shared by every owner in the household — the SHA-256 hex digest of the
     * canonical {@link #key(String, String) key}, so any member derives the same value.
     */
    static String id(String lastName, String address) {
        return Sha256.hex(key(lastName, address));
    }

    /** Case-insensitive form with leading/trailing and repeated internal whitespace collapsed. */
    private static String canonical(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
