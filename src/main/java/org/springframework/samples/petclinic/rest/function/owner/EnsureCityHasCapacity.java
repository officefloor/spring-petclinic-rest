package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create request whose city already holds {@link #CAPACITY} or more owners, so the
 * request maps to a 409. Existing owners are counted case-insensitively on their trimmed city
 * (see {@link CityRegistrations}), so incidental formatting differences neither hide nor inflate
 * the count. Runs among the other create guards, before {@link BuildOwner} maps the request to an
 * entity. The softer approaching-capacity warning below this cap is set by {@link FlagCapacityWarning}.
 */
public class EnsureCityHasCapacity {

    /** Maximum number of owners allowed to share a single city. */
    static final int CAPACITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        if (CityRegistrations.countIn(ownerRepository, request.getCity()) >= CAPACITY) {
            throw new CityAtCapacityException(request.getCity(), CAPACITY);
        }
    }
}
