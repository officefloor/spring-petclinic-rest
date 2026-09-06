package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Derives the deterministic {@code householdId}: the first 12 hex characters of
 * {@code SHA-256(identifierRegion + '|' + normalizedLastName + '|' + postcode)}.
 *
 * <p>Because it depends only on the region, last name and postcode, every owner sharing those values
 * resolves to the same id automatically, without any request opting in and without back-filling
 * other members. The version-2 {@link MemberIds#identifierRegion(Owner) identifier region} is mixed
 * in so the id is rederived under version 2 and never reproduces a version-1 value. The last name is
 * normalized case-insensitively with runs of whitespace collapsed; the postcode is trimmed and
 * contributes an empty segment when absent.
 */
public final class Households {

    private Households() {
    }

    /** The householdId for the owner, derived from its version-2 identifier region, last name and postcode. */
    public static String id(Owner owner) {
        String input = MemberIds.identifierRegion(owner) + '|' + normalize(owner.getLastName()) + '|'
                + segment(owner.getPostcode());
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(12);
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02x", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * The size of the owner's household: the number of persisted owners sharing this owner's
     * {@code householdId} (members inclusive). Returns 0 when the owner has no household id.
     */
    public static long size(Owner owner, OwnerRepository repository) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return 0;
        }
        return repository.findAll().stream()
                .filter(existing -> householdId.equals(existing.getHouseholdId()))
                .count();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    private static String segment(String value) {
        return value == null ? "" : value.trim();
    }
}
