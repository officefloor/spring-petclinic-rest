package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code <CITY3>-<LAST3>-<NNNN>},
 * where {@code CITY3} is the upper-cased first three letters of the city, {@code LAST3}
 * is the upper-cased first three letters of the last name and {@code NNNN} is a per-city
 * 4-digit zero-padded sequence equal to one more than the number of owners already in
 * that city (e.g. {@code LON-SMI-0007}).
 */
public class AssignOwnerCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        int sequence = (int) ownerRepository.findAll().stream()
                .filter(existing -> city.equals(existing.getCity()))
                .count() + 1;
        owner.setCustomerCode(String.format("%s-%s-%04d",
                first3Upper(city), first3Upper(owner.getLastName()), sequence));
    }

    /** The upper-cased first three letters of {@code s} (fewer if shorter). */
    private static String first3Upper(String s) {
        return s.substring(0, Math.min(3, s.length())).toUpperCase();
    }
}
