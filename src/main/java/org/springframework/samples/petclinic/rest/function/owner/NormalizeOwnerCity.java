package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Normalizes the city of a newly built owner. If another owner already lives in the
 * same city (matched case-insensitively, ignoring surrounding or repeated whitespace),
 * the new owner reuses that existing owner's exact spelling of the city name; otherwise
 * the city is title-cased (see {@link OwnerFieldNormalizer}).
 */
public class NormalizeOwnerCity {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        if (city == null) {
            return;
        }
        String key = OwnerFieldNormalizer.normalize(city);
        for (Owner existing : ownerRepository.findAll()) {
            if (!Objects.equals(existing.getId(), owner.getId())
                && Objects.equals(key, OwnerFieldNormalizer.normalize(existing.getCity()))) {
                owner.setCity(existing.getCity());
                return;
            }
        }
        owner.setCity(OwnerFieldNormalizer.titleCase(city));
    }
}
