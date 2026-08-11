package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code identityKey}: the single value all duplicate detection is expressed
 * through. Formatted {@code normalizedTelephone|email|householdId}, where the email and householdId
 * segments are empty when the owner has none. Two owners are the same identity when their whole
 * identityKey is equal; the telephone being part of the key means two members of one household
 * (same householdId) with different telephones have different keys and are both allowed.
 *
 * <p>The telephone is normalized to E.164 (the same way as {@link E164Telephone}) and the email is
 * trimmed and lower-cased, so differently-formatted values still produce the same key. The key is
 * computed identically for a not-yet-saved owner (in {@link CheckOwnerIdentityUnique}) and for the
 * response, so what a client reads back is exactly what collisions are judged on.
 */
public final class OwnerIdentityKey {

    private OwnerIdentityKey() {
    }

    /** The owner's identity key: {@code normalizedTelephone|email|householdId}. */
    public static String of(Owner owner) {
        return normalizeTelephone(owner.getTelephone()) + "|" + normalizeEmail(owner.getEmail())
                + "|" + (owner.getHouseholdId() == null ? "" : owner.getHouseholdId());
    }

    private static String normalizeTelephone(String raw) {
        if (raw == null) {
            return "";
        }
        String normalized = E164Telephone.normalize(raw);
        return normalized != null ? normalized : raw.trim();
    }

    private static String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        String trimmed = email.trim();
        return trimmed.isEmpty() ? "" : trimmed.toLowerCase(Locale.ROOT);
    }
}
