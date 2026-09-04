package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Household identity for owners. The {@code householdId} is now <em>deterministic</em>: it is derived
 * purely from the normalized last name and the postcode (see {@link #householdId(String, String)}), so
 * every owner sharing a last name and postcode computes the same value automatically — no explicit
 * linking is required. Duplicate detection ({@link EnsureUniqueIdentity}) and household size
 * ({@link AssignHouseholdSize}) both key off this computed id.
 *
 * <p>This class also exposes the read-only {@code identityKey} shown on the owner response,
 * {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}. That string embeds the
 * deterministic household id; it is descriptive only and no longer the basis of the duplicate check.
 *
 * <ul>
 * <li><b>normalizedTelephone</b> — the E.164 form (see {@link E164Telephone}); the stored telephone
 * is already E.164 and re-normalizing is idempotent.</li>
 * <li><b>email</b> — the lower-cased address, or empty when the owner has none.</li>
 * <li><b>householdId</b> — the owner's deterministic household id.</li>
 * </ul>
 */
public final class OwnerIdentityKey {

    private OwnerIdentityKey() {
    }

    /** The descriptive identity key of an already-built/stored owner, for the response. */
    public static String of(Owner owner) {
        return build(owner.getTelephone(), owner.getEmail(), owner.getHouseholdId());
    }

    /** The identity key from raw parts. Null telephone or email contribute an empty segment. */
    static String build(String telephone, String email, String householdId) {
        String tel = E164Telephone.toE164OrNull(telephone);
        return (tel == null ? "" : tel) + "|"
                + (email == null ? "" : email.trim().toLowerCase()) + "|"
                + (householdId == null ? "" : householdId);
    }

    /** The deterministic household id of a stored/built owner, from its last name and postcode. */
    public static String householdIdOf(Owner owner) {
        return householdId(normalizeName(owner.getLastName()), normalizePostcode(owner.getPostcode()));
    }

    /** The deterministic household id a create request would receive, from its last name and postcode. */
    static String householdIdOf(OwnerFieldsDto request) {
        return householdId(normalizeName(request.getLastName()), normalizePostcode(request.getPostcode()));
    }

    /** Last-name normalization used for household grouping: trim, collapse whitespace, lower-case. */
    static String normalizeName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /** Postcode normalization used for household grouping: trim; {@code null} becomes an empty string. */
    static String normalizePostcode(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Deterministic household id: the first 12 hex characters (upper-case) of SHA-256 over
     * {@code normalizedLastName + '|' + postcode}. Owners with the same last name and postcode share it.
     */
    static String householdId(String normalizedLastName, String postcode) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((normalizedLastName + "|" + postcode).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(12);
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02x", digest[i]));
            }
            return sb.toString().toUpperCase();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
