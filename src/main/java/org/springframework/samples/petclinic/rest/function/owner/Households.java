package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Single source of truth for what a "household" is: two owners share one when their
 * last name and address match after normalization. The last name is trimmed, has runs
 * of whitespace collapsed to a single space and is lower-cased; the address uses the
 * canonical {@link OwnerAddresses} normalization (the same form that is stored and
 * returned), so the comparison always runs on the normalized address. Also derives the
 * stable {@code householdId} for a household, used both when {@link AssignHousehold}
 * joins an owner to one and when the create-time duplicate check builds an owner's
 * identity key, so the two cannot drift apart.
 */
final class Households {

    private Households() {
    }

    /** Lower-cases and collapses runs of whitespace to a single space so the comparison
     *  ignores case and incidental spacing differences. */
    static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /** The canonical normalized address (see {@link OwnerAddresses}), used for every
     *  address comparison so it matches the stored and returned form. Never null. */
    static String normalizeAddress(String value) {
        String normalized = OwnerAddresses.normalize(value);
        return normalized == null ? "" : normalized;
    }

    /** Whether {@code existing} belongs to the household identified by the already
     *  normalized {@code lastName} and {@code address}. */
    static boolean matches(Owner existing, String normalizedLastName, String normalizedAddress) {
        return normalizedLastName.equals(normalize(existing.getLastName()))
                && normalizedAddress.equals(normalizeAddress(existing.getAddress()));
    }

    /** The {@code householdId} for the household an owner with this last name and address
     *  belongs to, derived from the raw values (normalized here first). */
    static String householdId(String rawLastName, String rawAddress) {
        return deriveHouseholdId(normalize(rawLastName), normalizeAddress(rawAddress));
    }

    /** Stable identifier for a household: the first 12 upper-case hex characters of the
     *  SHA-256 of the already-normalized last name and address joined by a NUL separator. */
    static String deriveHouseholdId(String normalizedLastName, String normalizedAddress) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((normalizedLastName + "\u0000" + normalizedAddress).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.substring(0, 12).toUpperCase(Locale.ROOT);
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
