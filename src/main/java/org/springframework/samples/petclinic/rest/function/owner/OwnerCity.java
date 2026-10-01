package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Single source of truth for the "owners in a city" primitive, used wherever an owner is created.
 *
 * <p>Both the city-capacity check ({@link CheckOwnerCityCapacity}) and the per-city customer-code
 * sequence ({@link AssignOwnerCustomerCode}) depend on the same count, and are meant to stay
 * consistent, so the definition of what counts as "the same city" lives here rather than being
 * inlined in each. The city is matched exactly today; aligning it with the address/household
 * normalization would be a change in one place.
 */
final class OwnerCity {

    private OwnerCity() {
    }

    /** The number of existing owners whose city matches {@code city}. */
    static long countOwnersInCity(OwnerRepository ownerRepository, String city) {
        return ownerRepository.findAll().stream()
                .filter(existing -> city.equals(existing.getCity()))
                .count();
    }
}
