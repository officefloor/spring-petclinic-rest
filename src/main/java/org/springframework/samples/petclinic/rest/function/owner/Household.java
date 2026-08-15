package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.util.IdentityVersion;

/**
 * Shared, deterministic household identity for the create-owner pipeline.
 *
 * <p>An owner's household is keyed purely on its last name and postcode: the {@code householdId} is
 * the first 12 hex characters (upper-case) of SHA-256 over {@code normalizedLastName + "|" +
 * postcode + "|" + "V2"}, where the trailing {@code "V2"} is the fixed version-2 identity tag.
 * Because it is derived from nothing but those two values (plus the fixed tag), every owner with the
 * same last name and postcode receives the same identifier automatically — no owner has to opt in,
 * and the id is identical before and after the owner is saved.
 *
 * <p>The last name is normalized (trimmed, internal whitespace collapsed, lower-cased) so that
 * cosmetic differences do not split a household; the postcode is compared as its trimmed value.
 *
 * <p>Used by {@link AssignHousehold} (which stores the id on the new owner), by
 * {@link CheckOwnerIdentityUnique} (which rejects a second owner already in the household unless the
 * request declares {@code sharesHousehold}) and by {@link OwnerIdentityKey}.
 */
final class Household {

    private Household() {
    }

    /** The deterministic household id the given create-owner request belongs to. */
    static String idFor(OwnerFieldsDto request) {
        return deriveId(normalizeName(request.getLastName()), normalizePostcode(request.getPostcode()));
    }

    /** The deterministic household id an existing, stored owner belongs to. */
    static String idFor(Owner owner) {
        return deriveId(normalizeName(owner.getLastName()), normalizePostcode(owner.getPostcode()));
    }

    /**
     * Whether the request declares itself a member of a shared household. This only bypasses the
     * duplicate block (see {@link CheckOwnerIdentityUnique}); it never affects which household id an
     * owner is assigned, which is fixed by its last name and postcode.
     */
    static boolean sharesHousehold(OwnerFieldsDto request) {
        return Boolean.TRUE.equals(request.getSharesHousehold());
    }

    /**
     * The number of stored owners in {@code owner}'s household — those sharing its
     * {@code householdId}, the owner itself included. Zero when the owner has no household id.
     */
    static int memberCount(Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return 0;
        }
        int count = 0;
        for (Owner other : ownerRepository.findAll()) {
            if (householdId.equals(other.getHouseholdId())) {
                count++;
            }
        }
        return count;
    }

    /**
     * First 12 upper-hex characters of SHA-256 over
     * {@code lastName + "|" + postcode + "|" + IdentityVersion.TAG}. The version-2 tag is mixed in
     * so every household id differs from its version-1 value.
     */
    private static String deriveId(String lastName, String postcode) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((lastName + "|" + postcode + "|" + IdentityVersion.TAG)
                            .getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(12);
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }

    /** Lower-case and collapse all runs of whitespace to a single space, trimmed. */
    private static String normalizeName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /** Trimmed postcode, or empty when none is supplied. */
    private static String normalizePostcode(String postcode) {
        return (postcode == null) ? "" : postcode.trim();
    }
}
