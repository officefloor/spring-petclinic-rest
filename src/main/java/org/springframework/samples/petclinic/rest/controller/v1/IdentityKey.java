package org.springframework.samples.petclinic.rest.controller.v1;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Single source of duplicate detection: an owner's identity is the SHA-256 hex of
 * {@code normalizedTelephone|lowerEmail|soundex(lastName)} (email empty when absent). Two owners
 * collide only when their whole keys are equal, so members of one household (same last name and
 * postcode) with different telephones stay distinct and surface only as soft matches.
 */
public final class IdentityKey {

    private IdentityKey() {
    }

    /** The derived identity key of {@code owner}; telephone is already E.164-normalized upstream. */
    public static String of(Owner owner) {
        String raw = blank(owner.getTelephone()) + "|" + lower(owner.getEmail()) + "|"
            + Soundex.code(owner.getLastName());
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** {@code true} when an existing owner has the exact same identity key as {@code candidate}. */
    static boolean isDuplicate(Owner candidate, Collection<Owner> existing) {
        String key = of(candidate);
        return existing.stream().anyMatch(other -> of(other).equals(key));
    }

    private static String blank(String value) {
        return value == null ? "" : value;
    }

    private static String lower(String value) {
        return value == null ? "" : value.toLowerCase();
    }
}
