package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags the new owner's locality: {@code "local"} when their city is the single most common city
 * among the owners that already exist at the moment of creation, {@code "remote"} otherwise. The new
 * owner is not yet persisted, so every stored owner is counted but the new one is not. A city is only
 * "most common" when it strictly outnumbers every other city (a tie means there is no single most
 * common city, so any city is {@code "remote"}). Runs after {@link NormalizeCity} so cities are
 * compared using their canonical spelling.
 */
public class AssignLocality {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        String locality = "remote";
        if (city != null) {
            Map<String, Long> counts = new HashMap<>();
            for (Owner existing : ownerRepository.findAll()) {
                if (existing.getCity() != null) {
                    counts.merge(existing.getCity(), 1L, Long::sum);
                }
            }
            long max = 0;
            int citiesAtMax = 0;
            for (long count : counts.values()) {
                if (count > max) {
                    max = count;
                    citiesAtMax = 1;
                }
                else if (count == max) {
                    citiesAtMax++;
                }
            }
            long cityCount = counts.getOrDefault(city, 0L);
            if (max > 0 && citiesAtMax == 1 && cityCount == max) {
                locality = "local";
            }
        }
        owner.setLocality(locality);
    }
}
