package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records the new owner's locality at the moment of creation (before it is saved): {@code "local"}
 * if the owner's city is the single most common city among the existing owners (a unique city with
 * strictly more owners than any other), otherwise {@code "remote"}. Cities are grouped ignoring
 * letter case. Runs after {@code normalizeCity} so the comparison uses the canonical city spelling,
 * and before {@code save}.
 */
public class AssignLocality {

    static final String LOCAL = "local";
    static final String REMOTE = "remote";

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        if (city == null) {
            owner.setLocality(REMOTE);
            return;
        }

        Map<String, Long> counts = new HashMap<>();
        for (Owner existing : ownerRepository.findAll()) {
            String existingCity = existing.getCity();
            if (existingCity != null) {
                counts.merge(existingCity.toLowerCase(Locale.ROOT), 1L, Long::sum);
            }
        }

        long max = 0;
        long tiesAtMax = 0;
        String mostCommon = null;
        for (Map.Entry<String, Long> entry : counts.entrySet()) {
            long count = entry.getValue();
            if (count > max) {
                max = count;
                tiesAtMax = 1;
                mostCommon = entry.getKey();
            } else if (count == max) {
                tiesAtMax++;
            }
        }

        boolean local = tiesAtMax == 1 && city.toLowerCase(Locale.ROOT).equals(mostCommon);
        owner.setLocality(local ? LOCAL : REMOTE);
    }
}
