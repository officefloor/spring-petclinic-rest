package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Derives an owner's {@code identityKey}, the single value all duplicate detection is expressed
 * through: {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}. Two owners are
 * duplicates only when their whole keys match; because the telephone is part of the key, household
 * members with different telephones have different keys and are both allowed.
 */
public final class IdentityKey {

    private IdentityKey() {
    }

    /** The key a create request would take once saved (its would-be householdId included). */
    public static String forRequest(OwnerFieldsDto request) {
        String householdId = Boolean.TRUE.equals(request.getSharesHousehold())
                ? household(request.getLastName(), request.getAddress()) : null;
        return key(request.getTelephone(), request.getEmail(), householdId);
    }

    /** The key of an existing owner. */
    public static String forOwner(Owner owner) {
        return key(owner.getTelephone(), owner.getEmail(), owner.getHouseholdId());
    }

    private static String key(String telephone, String email, String householdId) {
        return E164.toE164(telephone) + "|" + lower(email) + "|" + orEmpty(householdId);
    }

    // Mirrors AssignHousehold's derivation so a request's would-be household matches a stored one.
    private static String household(String lastName, String address) {
        String normalized = normalize(lastName) + "|" + normalize(address);
        return String.format("HH-%08X", normalized.hashCode());
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    private static String lower(String value) {
        return value == null ? "" : value.toLowerCase();
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
