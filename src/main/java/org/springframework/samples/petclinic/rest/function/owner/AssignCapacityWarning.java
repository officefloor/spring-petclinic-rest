package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Sets {@code capacityWarning} true when this owner's city already holds between 40 and 49
 * <em>other</em> owners — i.e. it is approaching, but has not reached, the hard capacity limit of 50
 * enforced by {@link CheckOwnerCityCapacity} — otherwise false. The city is compared
 * case-insensitively (matching {@link CheckOwnerCityCapacity} and {@link AssignCustomerCode}) and the
 * owner itself is excluded, so the count reflects the owners that preceded it. Runs on both the create
 * and read pipelines so the flag is derived consistently from current data.
 */
public class AssignCapacityWarning {

    private static final int WARN_FROM = 40;
    private static final int WARN_TO = 49;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        long others = ownerRepository.findAll().stream()
                .filter(existing -> !existing.getId().equals(owner.getId()))
                .filter(existing -> city != null && city.equalsIgnoreCase(existing.getCity()))
                .count();
        owner.setCapacityWarning(others >= WARN_FROM && others <= WARN_TO);
    }
}
