package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;
import java.util.Locale;

import org.springframework.samples.petclinic.model.IdentityVersion;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Sha256;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.util.StringUtils;

/**
 * Household identity shared by the create-owner steps. A household is the set of owners with the same
 * last name (normalized case-insensitively with collapsed whitespace) and the same postcode. Its
 * {@code householdId} is derived deterministically from those two values, so any two owners with the
 * same last name and postcode share it automatically without an explicit link. Used by
 * {@link AssignHouseholdId} to stamp each owner and by the steps that key off the household —
 * {@link MarkDeclaredHouseholdMember}, {@link CountHouseholdMembers} and {@link CapMembershipLevel} —
 * via {@link #existingMembers}.
 */
final class Household {

    /** The number of leading hex characters of the SHA-256 digest that form a household id. */
    private static final int ID_LENGTH = 12;

    private Household() {
    }

    /** Case-insensitive form with leading/trailing and repeated inner whitespace collapsed to one space. */
    static String normalizeLastName(String lastName) {
        return lastName == null ? "" : lastName.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * The deterministic version-2 household id for the given last name and postcode: the first
     * {@value #ID_LENGTH} hex characters of {@code SHA-256} over the {@link IdentityVersion#taggedInput
     * version-tagged} {@code normalizedLastName + '|' + postcode}. Mixing in the {@code V2} tag ensures
     * no version-1 household id is ever reproduced. Returns {@code null} when no postcode is supplied,
     * since a household is keyed on postcode and an owner without one belongs to no household.
     */
    static String idFor(String lastName, String postcode) {
        if (!StringUtils.hasText(postcode)) {
            return null;
        }
        return Sha256.hex(IdentityVersion.taggedInput(normalizeLastName(lastName) + '|' + postcode))
            .substring(0, ID_LENGTH);
    }

    /**
     * The existing owners sharing the given owner's household, i.e. those already persisted with the
     * same {@link Owner#getHouseholdId() householdId}. The owner itself is not yet saved, so it never
     * appears. An owner belonging to no household (no id) has no existing members.
     */
    static List<Owner> existingMembers(Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        if (householdId == null || householdId.isBlank()) {
            return List.of();
        }
        return ownerRepository.findByLastName(owner.getLastName()).stream()
            .filter(member -> householdId.equals(member.getHouseholdId()))
            .toList();
    }
}
