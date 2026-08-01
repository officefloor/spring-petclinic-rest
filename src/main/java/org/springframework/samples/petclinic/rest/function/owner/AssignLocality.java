package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records whether this owner's city is <em>the</em> single most common city among the
 * owners that already exist at the moment of creation. Runs before the owner is saved,
 * so {@link OwnerRepository#findAll()} yields only the existing owners.
 *
 * <p>The locality is {@code "local"} when the owner's city is strictly the most common
 * existing city (no other city shares the top count); otherwise, including when the top
 * count is tied between cities, it is {@code "remote"}.
 */
public class AssignLocality {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();

        Map<String, Long> counts = new HashMap<>();
        for (Owner existing : ownerRepository.findAll()) {
            String existingCity = existing.getCity();
            if (existingCity != null) {
                counts.merge(existingCity, 1L, Long::sum);
            }
        }

        long max = 0;
        long tiedAtMax = 0;
        for (long count : counts.values()) {
            if (count > max) {
                max = count;
                tiedAtMax = 1;
            }
            else if (count == max) {
                tiedAtMax++;
            }
        }

        boolean singleMostCommon = max > 0 && tiedAtMax == 1;
        boolean local = singleMostCommon && city != null && counts.getOrDefault(city, 0L) == max;
        owner.setLocality(local ? "local" : "remote");
    }
}
