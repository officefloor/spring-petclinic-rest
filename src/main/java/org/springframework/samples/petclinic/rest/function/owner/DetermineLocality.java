package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns a newly created owner a {@code locality} of {@code "local"} or
 * {@code "remote"}. The owner is {@code "local"} when, at the moment of creation, their
 * city is the single most common city among the owners that already exist; otherwise
 * they are {@code "remote"}.
 *
 * <p>The city is "single most common" only when exactly one city has the strictly
 * highest owner count — a tie for the top count means no city is the single most common,
 * so every owner created in that moment is {@code "remote"}. Runs before the owner is
 * saved, so only owners that existed beforehand are counted. Cities are matched
 * case-insensitively.
 */
public class DetermineLocality {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();

        Map<String, Integer> countsByCity = new HashMap<>();
        for (Owner existing : ownerRepository.findAll()) {
            String existingCity = existing.getCity();
            if (existingCity == null) {
                continue;
            }
            countsByCity.merge(existingCity.toLowerCase(Locale.ROOT), 1, Integer::sum);
        }

        int maxCount = countsByCity.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        long citiesAtMax = countsByCity.values().stream().filter(count -> count == maxCount).count();
        boolean singleMostCommon = maxCount > 0 && citiesAtMax == 1;

        boolean local = false;
        if (singleMostCommon && city != null) {
            String mostCommonCity = countsByCity.entrySet().stream()
                    .filter(entry -> entry.getValue() == maxCount)
                    .findFirst()
                    .map(Map.Entry::getKey)
                    .orElse(null);
            local = city.toLowerCase(Locale.ROOT).equals(mostCommonCity);
        }

        owner.setLocality(local ? "local" : "remote");
    }
}
