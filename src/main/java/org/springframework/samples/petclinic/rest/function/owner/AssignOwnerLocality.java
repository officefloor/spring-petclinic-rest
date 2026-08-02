package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns a newly created owner its locality: {@code "local"} when, at the moment
 * this owner is created, the owner's city is the single most common city among the
 * owners that already exist, otherwise {@code "remote"}. The owner being created is
 * not yet saved, so it is never counted among the existing owners. When two or more
 * cities are tied for most common there is no single most common city, so the owner
 * is {@code "remote"}. Cities are compared case-insensitively.
 */
public class AssignOwnerLocality {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();

        Map<String, Long> countsByCity = new HashMap<>();
        for (Owner existing : ownerRepository.findAll()) {
            String existingCity = existing.getCity();
            if (existingCity == null) {
                continue;
            }
            countsByCity.merge(existingCity.toLowerCase(Locale.ROOT), 1L, Long::sum);
        }

        owner.setLocality(isSingleMostCommon(city, countsByCity) ? "local" : "remote");
    }

    private static boolean isSingleMostCommon(String city, Map<String, Long> countsByCity) {
        if (city == null || countsByCity.isEmpty()) {
            return false;
        }
        long max = countsByCity.values().stream().mapToLong(Long::longValue).max().orElse(0L);
        long citiesAtMax = countsByCity.values().stream().filter(count -> count == max).count();
        if (citiesAtMax != 1) {
            return false; // a tie: no single most common city
        }
        Long cityCount = countsByCity.get(city.toLowerCase(Locale.ROOT));
        return cityCount != null && cityCount == max;
    }
}
