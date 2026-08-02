package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records the owner's locality on creation: {@code "local"} when their city is the
 * single most common city among the owners that already exist, otherwise
 * {@code "remote"}.
 *
 * <p>Cities are counted ignoring letter case and surrounding or repeated whitespace,
 * consistent with the rest of the owner-creation pipeline. "Single most common" means a
 * unique winner: if two or more cities are tied for the highest count there is no single
 * most common city, so the owner is {@code "remote"}. Runs before the owner is saved, so
 * the counts exclude this owner itself.
 */
public class ComputeLocality {

    static final String LOCAL = "local";
    static final String REMOTE = "remote";

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        if (city == null || city.isBlank()) {
            owner.setLocality(REMOTE);
            return;
        }

        Map<String, Long> counts = new HashMap<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (isDifferentOwner(existing, owner)) {
                String existingCity = existing.getCity();
                if (existingCity != null && !existingCity.isBlank()) {
                    counts.merge(matchKey(existingCity), 1L, Long::sum);
                }
            }
        }

        long max = 0;
        int winners = 0;
        String topCity = null;
        for (Map.Entry<String, Long> entry : counts.entrySet()) {
            long count = entry.getValue();
            if (count > max) {
                max = count;
                winners = 1;
                topCity = entry.getKey();
            } else if (count == max) {
                winners++;
            }
        }

        boolean local = winners == 1 && matchKey(city).equals(topCity);
        owner.setLocality(local ? LOCAL : REMOTE);
    }

    private static boolean isDifferentOwner(Owner existing, Owner candidate) {
        return existing.getId() == null || !existing.getId().equals(candidate.getId());
    }

    /** Case- and whitespace-insensitive key used to match owners in the same city. */
    private static String matchKey(String value) {
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
