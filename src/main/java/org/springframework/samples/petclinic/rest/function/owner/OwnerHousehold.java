package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.OwnerRegion;

/**
 * Single source of truth for the owner-household identity rule. A household is keyed on
 * {@code (lastName, postcode)}: two owners whose normalized last names match and whose postcodes are
 * equal belong to the same household and therefore share the same {@link #householdId}. The id is
 * <em>deterministic</em> — it is computed purely from those two values, so every owner that joins a
 * household arrives at the same id independently, without coordinating through the database.
 *
 * <p>Used by {@link AssignOwnerHousehold} to stamp the id onto a new owner (which later steps read
 * for household size, membership-level capping and the like) — so the definition of "same
 * household" lives here and nowhere else. It is no longer part of the owner identity key; see
 * {@link OwnerIdentity}.
 */
final class OwnerHousehold {

    private OwnerHousehold() {
    }

    /**
     * Reduces a value to its comparison form: trimmed, inner runs of whitespace collapsed to a
     * single space, and lower-cased. A {@code null} value yields an empty string.
     */
    static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * Derives the stable household identifier shared by every owner in the same household. Owners
     * whose normalized last name and postcode match produce the same value, so the id can be computed
     * independently for each joining owner without coordinating through the database. The value is
     * the first 12 upper-case hex characters of the SHA-256 of the normalized
     * {@code "<region>|<lastName>|<postcode>"}, where {@code region} is the version-2 identity region
     * code (see {@link OwnerRegion#identityRegion}). Mixing the {@code V2}-tagged region in means no
     * id matches the one version 1 would have produced from last name and postcode alone.
     *
     * <p>A household requires a postcode: when the postcode is {@code null} or blank the owner
     * belongs to no household and this returns {@code null}.
     */
    static String householdId(Owner owner) {
        String postcode = owner.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return null;
        }
        String region = OwnerRegion.identityRegion(owner.getCity(), postcode);
        String key = region + "|" + normalize(owner.getLastName()) + "|" + postcode.trim();
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
