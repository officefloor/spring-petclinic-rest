package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Sets the owner's {@code capacityWarning} flag to true when its city is approaching the
 * per-city capacity limit — the city already holds between {@value #WARN_LOW} and
 * {@value #WARN_HIGH} owners (the hard limit enforced by {@link EnsureCityCapacity} being
 * {@value EnsureCityCapacity#CAPACITY}), otherwise false. Counts all owners in the city
 * including this one, so the flag is identical whether returned from the create response
 * (runs after Save, so the new owner is counted) or a later GET, mirroring
 * {@link AssignOwnerBulkSignupWarning}.
 */
public class AssignOwnerCapacityWarning {

    /** Lowest owners-in-city count (inclusive) that raises the approaching-capacity warning. */
    static final int WARN_LOW = 40;

    /** Highest owners-in-city count (inclusive) that raises the approaching-capacity warning. */
    static final int WARN_HIGH = EnsureCityCapacity.CAPACITY - 1;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        long count = ownerRepository.findAll().stream()
                .filter(o -> city == null ? o.getCity() == null : city.equals(o.getCity()))
                .count();
        owner.setCapacityWarning(count >= WARN_LOW && count <= WARN_HIGH);
    }
}
