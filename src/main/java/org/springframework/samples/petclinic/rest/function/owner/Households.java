package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Shared household identity. A household is keyed on an owner's last name and postcode: every
 * owner whose (normalized) last name and postcode match belongs to the same household and
 * shares one stable {@link #id household id}, derived purely from that key. The id is therefore
 * deterministic — two owners in the same household compute the same id independently, with no
 * coordination — so it drives both duplicate detection and the household-size count off a single
 * source of truth.
 */
final class Households {

    private Households() {
    }

    /**
     * Canonicalizes a last name for case-insensitive, whitespace-insensitive comparison:
     * trims the ends, collapses every run of whitespace to a single space and lower-cases
     * the result. Returns an empty string when {@code lastName} is {@code null}.
     */
    private static String normalizeName(String lastName) {
        if (lastName == null) {
            return "";
        }
        return lastName.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /**
     * The stable household id shared by every owner with the same last name and postcode: the
     * first 12 hex characters of the SHA-256 digest of {@code normalizedLastName + "|" +
     * postcode}, with the {@link IdentityVersion identity version} tag mixed in. A {@code null}
     * postcode contributes an empty segment.
     */
    static String id(String lastName, String postcode) {
        String key = normalizeName(lastName) + "|" + (postcode == null ? "" : postcode);
        return Hashes.upperHexPrefix(IdentityVersion.stamp(key), 12);
    }

    /** The household id a create request resolves to, from its last name and postcode. */
    static String id(OwnerFieldsDto request) {
        return id(request.getLastName(), request.getPostcode());
    }

    /** The household id of an existing owner, from its stored last name and postcode. */
    static String id(Owner owner) {
        return id(owner.getLastName(), owner.getPostcode());
    }

    /** Whether the two owners belong to the same household (identical household id). */
    static boolean sameHousehold(Owner a, Owner b) {
        return id(a).equals(id(b));
    }
}
