package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Sets {@code capacityWarning} to {@code true} when this owner's city already holds between
 * {@link #WARNING_FLOOR} and {@link RejectCityAtCapacity#CITY_CAPACITY} minus one owners
 * (approaching the capacity limit of {@value RejectCityAtCapacity#CITY_CAPACITY}), otherwise
 * {@code false}. Cities are matched the same way {@link RejectCityAtCapacity} matches them:
 * case-insensitively after trimming and collapsing runs of whitespace to a single space. Runs
 * before the owner is saved, so {@link OwnerRepository#findAll()} sees only the owners that
 * existed before this create. The hard rejection at capacity is left to {@link RejectCityAtCapacity}.
 */
public class FlagCityCapacityWarning {

    /** The warning is raised once at least this many owners already exist for the city. */
    static final int WARNING_FLOOR = 40;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = normalize(owner.getCity());
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (city.equals(normalize(existing.getCity()))) {
                count++;
            }
        }
        owner.setCapacityWarning(count >= WARNING_FLOOR && count < RejectCityAtCapacity.CITY_CAPACITY);
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
