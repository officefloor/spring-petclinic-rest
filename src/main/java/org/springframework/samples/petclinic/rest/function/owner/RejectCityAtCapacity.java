package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create-owner request whose city already contains 50 or more owners. Runs
 * before {@link BuildOwner}, counting existing owners in the requested city (same-city
 * equality as {@link AssignCustomerCode}). At capacity the request is rejected 409 via
 * {@link CityAtCapacityException}.
 */
public class RejectCityAtCapacity {

    private static final int CITY_CAPACITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        String city = request.getCity();
        long count = ownerRepository.findAll().stream()
                .filter(o -> city == null ? o.getCity() == null : city.equals(o.getCity()))
                .count();
        if (count >= CITY_CAPACITY) {
            throw new CityAtCapacityException(
                    "City already contains the maximum number of owners");
        }
    }
}
