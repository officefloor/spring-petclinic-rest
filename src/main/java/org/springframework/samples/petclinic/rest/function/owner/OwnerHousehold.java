package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * The identity of an owner's household: its {@code lastName} and {@code address} compared
 * case-insensitively, with surrounding whitespace stripped and internal runs collapsed to a single
 * space. Two owners belong to the same household when their {@code OwnerHousehold} values are equal.
 *
 * <p>This is the single definition of "same household" shared by {@link CheckOwnerHouseholdUnique}
 * (which iterates to reject a second owner joining a household) and {@link AssignHousehold} (which
 * iterates to find the donor owner whose {@code householdId} a new member inherits). Changing the
 * matching rule — e.g. trimming punctuation or including city — happens here, once.
 */
public record OwnerHousehold(String lastName, String address) {

    public OwnerHousehold {
        lastName = normalize(lastName);
        address = normalize(address);
    }

    public static OwnerHousehold of(Owner owner) {
        return new OwnerHousehold(owner.getLastName(), owner.getAddress());
    }

    public static OwnerHousehold of(OwnerFieldsDto fields) {
        return new OwnerHousehold(fields.getLastName(), fields.getAddress());
    }

    /** Lower-cases and collapses whitespace so comparison ignores case and incidental spacing. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
