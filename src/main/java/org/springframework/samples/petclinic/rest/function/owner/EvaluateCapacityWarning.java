package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Sets the owner's transient {@code capacityWarning} flag for the response. The flag is true when the
 * owner's city already holds between 40 and 49 other owners (inclusive), i.e. it is approaching the
 * hard capacity limit of 50 enforced by {@link EnsureCityBelowCapacity}; otherwise false. Cities are
 * compared case-insensitively with collapsed whitespace, matching the capacity check, and the owner
 * itself is excluded from the count so the flag reflects how many owners the city "already has".
 * Mutated in place via {@code @Val} for the responding step to map onto the DTO.
 */
public class EvaluateCapacityWarning {

    /** Lower inclusive bound of the warning band (the city is nearing capacity from here). */
    private static final int WARNING_FLOOR = 40;

    /** Upper inclusive bound of the warning band (the hard limit of 50 is enforced elsewhere). */
    private static final int WARNING_CEILING = 49;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = canonical(owner.getCity());
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() != null && existing.getId().equals(owner.getId())) {
                continue; // do not count this owner towards the city it belongs to
            }
            if (city.equals(canonical(existing.getCity()))) {
                count++;
            }
        }
        owner.setCapacityWarning(count >= WARNING_FLOOR && count <= WARNING_CEILING);
    }

    /** Lower-cased, trimmed, with any run of whitespace collapsed to a single space. */
    private static String canonical(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
