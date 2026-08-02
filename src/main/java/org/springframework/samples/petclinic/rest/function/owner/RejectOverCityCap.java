package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.BadRequestException;

/**
 * Rejects creating an owner when their city already contains {@value #CITY_CAP}
 * owners, with a 400. Cities are matched case-insensitively and ignoring
 * surrounding or repeated whitespace (see {@link OwnerFieldNormalizer}).
 */
public class RejectOverCityCap {

    static final int CITY_CAP = 8;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws BadRequestException {
        String city = owner.getCity();
        if (city == null) {
            return;
        }
        String key = OwnerFieldNormalizer.normalize(city);
        long inCity = ownerRepository.findAll().stream()
                .filter(existing -> !Objects.equals(existing.getId(), owner.getId()))
                .filter(existing -> Objects.equals(key, OwnerFieldNormalizer.normalize(existing.getCity())))
                .count();
        if (inCity >= CITY_CAP) {
            throw new BadRequestException("City owner limit reached");
        }
    }
}
