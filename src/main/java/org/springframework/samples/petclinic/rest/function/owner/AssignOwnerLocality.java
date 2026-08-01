package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns a newly created owner its locality: {@code "local"} when the owner's city is
 * the single most common city among the owners that already existed at the moment the
 * owner is created, otherwise {@code "remote"}. A city is the single most common only
 * when strictly more existing owners live there than in any other city (a tie for the
 * top count yields {@code "remote"}).
 */
public class AssignOwnerLocality {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();

        // Tally how many existing owners live in each city (excluding this owner).
        Map<String, Integer> cityCounts = new HashMap<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (isSameOwner(owner, existing)) {
                continue;
            }
            String existingCity = existing.getCity();
            if (existingCity == null) {
                continue;
            }
            cityCounts.merge(existingCity, 1, Integer::sum);
        }

        int ownerCityCount = cityCounts.getOrDefault(city, 0);

        // Local only when the owner's city has strictly more existing owners than
        // every other city.
        boolean local = ownerCityCount > 0;
        for (Map.Entry<String, Integer> entry : cityCounts.entrySet()) {
            if (Objects.equals(entry.getKey(), city)) {
                continue;
            }
            if (entry.getValue() >= ownerCityCount) {
                local = false;
                break;
            }
        }

        owner.setLocality(local ? "local" : "remote");
    }

    private static boolean isSameOwner(Owner a, Owner b) {
        return a.getId() != null && Objects.equals(a.getId(), b.getId());
    }
}
