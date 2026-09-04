package org.springframework.samples.petclinic.util;

import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Decides the GOLD membership tier: an owner qualifies once their household (owners
 * sharing the same {@link Household} id, i.e. the same normalized last name and
 * address) has three or more members. When it does not apply, the mapper's existing
 * BRONZE/SILVER rules stand.
 */
public final class GoldTier {

    private GoldTier() {
    }

    /** True when {@code owner}'s household has 3+ members among {@code allOwners}. */
    public static boolean qualifies(Owner owner, Collection<Owner> allOwners) {
        String household = Household.idFor(owner.getLastName(), owner.getAddress());
        return allOwners.stream()
            .filter(o -> household.equals(Household.idFor(o.getLastName(), o.getAddress())))
            .count() >= 3;
    }
}
