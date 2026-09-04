package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records, on the owner being created, whether its city is approaching the per-city capacity limit
 * of 50. The flag is true when the city already holds between 40 and 49 owners (inclusive) at the
 * moment this owner is created — the count is a snapshot taken before this owner is saved, counted
 * by normalized city exactly as {@link EnsureCityCapacity} counts the hard cap (case-insensitive
 * with collapsed whitespace). The hard rejection at 50 stays with {@link EnsureCityCapacity}, so by
 * the time this step runs the count is guaranteed below 50. Runs before {@link SaveOwner}; the
 * resulting flag is echoed back on the owner as {@code capacityWarning}.
 */
public class AssignCapacityWarning {

    private static final int WARNING_THRESHOLD = 40;

    private static final int CITY_CAPACITY = 50;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = ComparisonText.of(owner.getCity());
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (ComparisonText.of(existing.getCity()).equals(city)) {
                count++;
            }
        }
        owner.setCapacityWarning(count >= WARNING_THRESHOLD && count < CITY_CAPACITY);
    }
}
