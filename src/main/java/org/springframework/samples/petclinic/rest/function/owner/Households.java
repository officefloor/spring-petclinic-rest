package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.OptionalInt;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.util.OwnerIdentityVersion;

/**
 * Shared household logic. An owner belongs to a household identified by their last name
 * and postcode. The last name is compared case-insensitively with collapsed whitespace;
 * the postcode is taken verbatim (it is already validated to a canonical four-digit form).
 * The identifier is derived deterministically from those two fields, so every owner in the
 * same household resolves to the same stable value without any coordination. An owner with
 * no postcode has no household.
 */
final class Households {

    private Households() {
    }

    /**
     * The existing live owners that share a household (same last name and postcode) with the
     * given fields. Soft-deleted owners are excluded, so a deleted owner no longer blocks or
     * flags a new one. Empty when no postcode is supplied.
     */
    static List<Owner> membersOf(OwnerRepository ownerRepository, String lastName, String postcode) {
        String householdId = idFor(lastName, postcode);
        if (householdId == null) {
            return List.of();
        }
        return ownerRepository.findByLastName(lastName).stream()
                .filter(owner -> !owner.isDeleted())
                .filter(owner -> householdId.equals(idFor(owner.getLastName(), owner.getPostcode())))
                .toList();
    }

    /**
     * The highest {@link Owner#getMembershipLevel() membership level} currently held by an
     * existing member of the household (same last name and postcode), or empty when the
     * household has no existing members.
     */
    static OptionalInt maxMembershipLevel(OwnerRepository ownerRepository, String lastName, String postcode) {
        return membersOf(ownerRepository, lastName, postcode).stream()
                .mapToInt(Owner::getMembershipLevel)
                .max();
    }

    /**
     * The stable identifier shared by every owner at the given last name and postcode: the
     * first twelve hex characters of the SHA-256 of {@code <V2> + '|' + normalizedLastName +
     * '|' + postcode}, where {@code <V2>} is the fixed {@link OwnerIdentityVersion#TAG} version
     * tag mixed in so the version-2 id differs from its version-1 form. Returns {@code null}
     * when no postcode is supplied, since an owner without a postcode has no household.
     */
    static String idFor(String lastName, String postcode) {
        if (postcode == null || postcode.isBlank()) {
            return null;
        }
        return sha256Hex(OwnerIdentityVersion.TAG + "|" + normalizeName(lastName) + "|" + postcode)
                .substring(0, 12).toUpperCase();
    }

    private static String normalizeName(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    private static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
