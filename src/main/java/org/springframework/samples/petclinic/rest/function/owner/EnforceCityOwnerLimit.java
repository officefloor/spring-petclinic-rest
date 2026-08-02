package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.CityOwnerLimitExceededException;

/**
 * Rejects creation of an <code>Owner</code> once a single city already contains the
 * maximum number of owners. Cities are matched ignoring letter case and surrounding or
 * repeated whitespace, so {@code "  new  york "} and {@code "New York"} count as the
 * same city.
 */
public class EnforceCityOwnerLimit {

    /** Maximum number of owners that may belong to any one city. */
    static final int MAX_OWNERS_PER_CITY = 8;

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws CityOwnerLimitExceededException {
        String city = owner.getCity();
        if (city == null || city.isBlank()) {
            return;
        }
        String key = matchKey(city);
        long ownersInCity = ownerRepository.findAll().stream()
                .filter(existing -> isDifferentOwner(existing, owner))
                .map(Owner::getCity)
                .filter(existingCity -> existingCity != null && key.equals(matchKey(existingCity)))
                .count();
        if (ownersInCity >= MAX_OWNERS_PER_CITY) {
            throw new CityOwnerLimitExceededException(
                    "Cannot create more than " + MAX_OWNERS_PER_CITY
                            + " owners in " + city);
        }
    }

    private static boolean isDifferentOwner(Owner existing, Owner candidate) {
        return existing.getId() == null || !existing.getId().equals(candidate.getId());
    }

    /** Case- and whitespace-insensitive key used to match owners in the same city. */
    private static String matchKey(String value) {
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
