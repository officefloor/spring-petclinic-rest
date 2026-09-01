package org.springframework.samples.petclinic.rest.controller.v1;

import java.util.Collection;

import org.springframework.samples.petclinic.model.IdentityKeys;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Soft-match rule for owners: a candidate is a possible duplicate of an existing owner
 * when their identityKeys differ but their last names share a soundex code and they have
 * the same postcode, so it is created rather than rejected as a hard duplicate.
 */
final class PossibleDuplicates {

    private PossibleDuplicates() {
    }

    /**
     * The id of an existing owner {@code candidate} softly matches, or {@code null} if none.
     * A candidate that declares itself a household member ({@code sharesHousehold} true) is
     * never a suspected duplicate.
     */
    static Integer matchId(Collection<Owner> existing, Owner candidate, Boolean sharesHousehold) {
        if (Boolean.TRUE.equals(sharesHousehold)) {
            return null;
        }
        return existing.stream()
            .filter(other -> softMatch(other, candidate))
            .map(Owner::getId)
            .findFirst()
            .orElse(null);
    }

    private static boolean softMatch(Owner a, Owner b) {
        String soundex = IdentityKeys.soundex(b.getLastName());
        return b.getPostcode() != null && b.getPostcode().equals(a.getPostcode())
            && !soundex.isEmpty() && soundex.equals(IdentityKeys.soundex(a.getLastName()))
            && !IdentityKeys.of(a).equals(IdentityKeys.of(b));
    }
}
