package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Derives an owner's {@code identityKey}, the single value all duplicate detection is expressed
 * through: the SHA-256 hex of {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}.
 * Two owners are duplicates only when their whole keys match; because the telephone is part of the key,
 * household members with different telephones have different keys and are both allowed.
 */
public final class IdentityKey {

    private IdentityKey() {
    }

    /** The key a create request would take once saved. */
    public static String forRequest(OwnerFieldsDto request) {
        return key(request.getTelephone(), request.getEmail(), request.getLastName());
    }

    /** The key of an existing owner. */
    public static String forOwner(Owner owner) {
        return key(owner.getTelephone(), owner.getEmail(), owner.getLastName());
    }

    private static String key(String telephone, String email, String lastName) {
        return sha256Hex(E164.toE164(telephone) + "|" + lower(email) + "|" + Soundex.of(lastName));
    }

    /**
     * The deterministic household id: the first 12 hex characters of SHA-256 over
     * {@code normalizedLastName + '|' + postcode}. Owners with the same last name and postcode
     * therefore compute the same value and share a household automatically. Null when no postcode.
     */
    public static String household(String lastName, String postcode) {
        if (postcode == null || postcode.isEmpty()) {
            return null;
        }
        return sha256Hex12(normalize(lastName) + "|" + postcode);
    }

    private static String sha256Hex12(String value) {
        return sha256Hex(value).substring(0, 12);
    }

    private static String sha256Hex(String value) {
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        }
        catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    private static String lower(String value) {
        return value == null ? "" : value.toLowerCase();
    }
}
