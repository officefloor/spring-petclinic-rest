package org.springframework.samples.petclinic.rest.controller.v1;

import java.util.Collection;
import java.util.OptionalInt;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.Membership;

/**
 * Applies the household level ceiling: a new owner's membershipLevel cannot exceed one
 * above the highest membershipLevel among the other members of their household (owners
 * sharing its household id). With no existing household member no cap applies.
 */
final class HouseholdLevels {

    private HouseholdLevels() {
    }

    /** The membership level {@code owner} reaches on its own merit, before any cap. */
    private static int naturalLevel(Collection<Owner> owners, Owner owner) {
        return Membership.level(Membership.points(owner,
            Households.memberCount(owners, owner) >= 3, Tenure.exceedsOneYear(owner)));
    }

    /** {@code level} capped at one above the highest natural level among {@code owner}'s
     *  other household members, or unchanged when it has none. */
    static int capped(Collection<Owner> owners, Owner owner, int level) {
        String id = Households.householdId(owner);
        OptionalInt ceiling = owners.stream()
            .filter(o -> !o.getId().equals(owner.getId()) && id.equals(Households.householdId(o)))
            .mapToInt(o -> naturalLevel(owners, o))
            .max();
        return ceiling.isPresent() ? Math.min(level, ceiling.getAsInt() + 1) : level;
    }
}
