package org.springframework.samples.petclinic.rest.controller.v1;

import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.IdentityKeys;
import org.springframework.samples.petclinic.util.Soundex;

/**
 * Detects a "soft" duplicate: an owner whose whole {@code identityKey} differs from an
 * existing owner's, yet shares that owner's {@code soundex(lastName)} and postcode. The
 * differing identity key excludes an owner from matching itself.
 */
final class PossibleDuplicates {

    private PossibleDuplicates() {
    }

    /** The id of the earliest existing owner softly matching the candidate, or null when none. */
    static Integer matchId(Collection<Owner> existing, Owner candidate) {
        if (candidate.getPostcode() == null) {
            return null;
        }
        String soundex = Soundex.of(candidate.getLastName());
        String key = IdentityKeys.of(candidate);
        return existing.stream()
            .filter(o -> soundex.equals(Soundex.of(o.getLastName()))
                && candidate.getPostcode().equals(o.getPostcode())
                && !key.equals(IdentityKeys.of(o)))
            .map(Owner::getId)
            .min(Integer::compareTo)
            .orElse(null);
    }
}
