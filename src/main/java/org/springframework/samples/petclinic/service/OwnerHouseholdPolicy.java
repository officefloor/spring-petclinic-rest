package org.springframework.samples.petclinic.service;

import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Business rule: a household is keyed on (last name, postcode). Every owner is given a
 * deterministic {@code householdId} on creation &mdash; the first 12 hex characters of
 * SHA-256 over {@code normalizedLastName|postcode} &mdash; so owners with the same last name
 * and postcode share it automatically. Because they are the same household, a second such owner
 * is rejected unless the request opts in to sharing ({@code sharesHousehold == true}), which only
 * bypasses the rejection; the id is assigned either way. Kept as a small, self-contained unit so
 * the rule can be enforced from the create flow without adding complexity to the controller or
 * service.
 */
public final class OwnerHouseholdPolicy {

    private OwnerHouseholdPolicy() {
    }

    /**
     * Assign the owner's deterministic {@code householdId} so owners with the same (last name,
     * postcode) share it. Shared households are permitted &mdash; a new member simply joins, and
     * their membership level is capped by {@link OwnerMembershipCeilingPolicy}.
     *
     * @param clinicService   source of the existing owners (unused; kept for call-site stability)
     * @param owner           the owner being created
     * @param sharesHousehold whether the request opted in to a shared household (no longer required)
     */
    public static void rejectDuplicateHousehold(ClinicService clinicService, Owner owner, Boolean sharesHousehold) {
        owner.setHouseholdId(householdId(
            OwnerFieldNormalizer.name(owner.getLastName()), OwnerFieldNormalizer.orEmpty(owner.getPostcode())));
    }

    /** Derive a stable identifier shared by every owner in the same (last name, postcode) household. */
    private static String householdId(String lastName, String postcode) {
        return Sha256Hex.lower("V2|" + lastName + "|" + postcode, 6);
    }

    /** Thrown when another owner already shares the same last name and postcode. */
    @ResponseStatus(HttpStatus.CONFLICT)
    public static class DuplicateHouseholdException extends RuntimeException {
    }
}
