package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the locality of a newly created owner: {@code "local"} if the owner's city is
 * the single most common city among existing owners at the moment of creation, otherwise
 * {@code "remote"}. The owner being created has not yet been saved, so it is not counted.
 * If no owner exists yet, or several cities tie for the most common, there is no single
 * most common city and the owner is {@code "remote"}. Cities are grouped case- and
 * whitespace-insensitively, consistent with the other owner rules.
 */
public class AssignLocality {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        Map<String, Integer> counts = new HashMap<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() != null && existing.getId().equals(owner.getId())) {
                continue;
            }
            String key = DuplicateMatching.normalize(existing.getCity());
            if (key == null) {
                continue;
            }
            counts.merge(key, 1, Integer::sum);
        }

        int max = 0;
        int tally = 0;
        String topCity = null;
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            int count = entry.getValue();
            if (count > max) {
                max = count;
                tally = 1;
                topCity = entry.getKey();
            } else if (count == max) {
                tally++;
            }
        }

        boolean single = tally == 1 && topCity != null;
        boolean local = single && topCity.equals(DuplicateMatching.normalize(owner.getCity()));
        owner.setLocality(local ? "local" : "remote");
    }
}
