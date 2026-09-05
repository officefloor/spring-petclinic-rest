package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code householdId}: the first 12 hex characters of
 * {@code SHA-256(normalizedLastName + '|' + postcode)}. Because it is derived purely from
 * the (last name, postcode) pair — the last name compared case-insensitively with runs of
 * whitespace collapsed to a single space, the postcode trimmed — any two owners sharing a
 * last name and postcode receive the <em>same</em> identifier automatically: they are, by
 * definition, the same household. The value never changes as more members join.
 *
 * <p>One of the three owner-identity derivations, alongside {@link OwnerMemberId} (the
 * {@code memberId}) and {@link OwnerIdentity} (the {@code identityKey}); each keeps the
 * composition of its identifier in one place so callers delegate rather than re-derive it.
 */
public final class OwnerHouseholdId {

    private OwnerHouseholdId() {
    }

    /** The household id of a stored owner, using its last name and postcode. */
    public static String of(Owner owner) {
        return of(owner.getLastName(), owner.getPostcode());
    }

    /**
     * The household id for the given parts: the first 12 hex characters of
     * {@code SHA-256(normalizedLastName + '|' + postcode)}. The last name is trimmed, its
     * internal whitespace collapsed and lower-cased (an absent last name contributes empty);
     * the postcode is trimmed (an absent postcode contributes empty).
     */
    public static String of(String lastName, String postcode) {
        String normalizedLastName = normalizeLastName(lastName);
        String normalizedPostcode = postcode == null ? "" : postcode.trim();
        return Sha256.hex(normalizedLastName + "|" + normalizedPostcode).substring(0, 12);
    }

    private static String normalizeLastName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
