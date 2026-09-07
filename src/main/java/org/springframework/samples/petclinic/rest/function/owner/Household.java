package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Single home for how owners are grouped into a household and how a household's stable shared id is
 * derived. Parallels {@link OwnerIdentity} (the duplicate key) and {@link CustomerCode} (the
 * customer code): the create pipeline's {@link AssignOwnerHousehold} goes through here, so the
 * grouping rule and the id derivation stay defined in one place.
 *
 * <p>Two owners belong to the same household when they share a normalized last name and address
 * (see {@link #sameHousehold(Owner, Owner)}). The shared {@code householdId} reuses the value an
 * existing member already carries, otherwise it is derived deterministically from that same
 * normalized last name and address, so independent joiners compute the same value.
 */
final class Household {

    private Household() {
    }

    /** Comparison key for a last name: leading/trailing and repeated whitespace collapsed, lower-cased. */
    static String lastNameKey(String lastName) {
        if (lastName == null) {
            return "";
        }
        return lastName.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /** Whether two owners belong to the same household: same normalized last name and address. */
    static boolean sameHousehold(Owner a, Owner b) {
        return lastNameKey(a.getLastName()).equals(lastNameKey(b.getLastName()))
                && AddressNormalizer.normalize(a.getAddress())
                        .equals(AddressNormalizer.normalize(b.getAddress()));
    }

    /**
     * The household's stable shared identifier for {@code owner}, given the existing members it
     * joins: reuse the id an existing member already carries (so a household keeps one value over
     * time); otherwise derive it deterministically from the normalized last name and address, so
     * independent joiners compute the same value.
     */
    static String id(Owner owner, List<Owner> existingMembers) {
        for (Owner member : existingMembers) {
            String existing = member.getHouseholdId();
            if (existing != null && !existing.isBlank()) {
                return existing;
            }
        }
        String lastName = lastNameKey(owner.getLastName());
        String address = AddressNormalizer.normalize(owner.getAddress());
        return "H-" + ShaHex.upperPrefix(lastName + "\n" + address, 12);
    }
}
