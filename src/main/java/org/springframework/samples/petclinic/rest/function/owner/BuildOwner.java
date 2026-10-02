package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

public class BuildOwner {

    public void service(@Val OwnerFieldsDto request, @Val Household.Id householdId,
            OwnerMapper ownerMapper, OwnerRepository ownerRepository, Out<Owner> built) {
        Owner owner = ownerMapper.toOwner(request);
        // Default an unsupplied registration date to the server's current date.
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(LocalDate.now());
        }
        owner.setCustomerCode(nextCustomerCode(owner.getCity(), owner.getLastName(), ownerRepository));
        // Record how many existing owners already share this first+last name (case-insensitive),
        // counted before this owner is persisted so the value is fixed at creation time.
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName(), ownerRepository));
        // Join a shared household when the gate resolved an identifier.
        if (householdId != null && householdId.isPresent()) {
            owner.setHouseholdId(householdId.value());
        }
        built.set(owner);
    }

    /**
     * Build the '<CITY3>-<LAST3>-<NNNN>' customer code: CITY3 is the upper-cased first three
     * letters of the city, LAST3 the upper-cased first three letters of the last name, and NNNN
     * a per-city 4-digit zero-padded sequence equal to one more than the number of owners
     * already in that city (e.g. 'LON-SMI-0007').
     */
    private static String nextCustomerCode(String city, String lastName, OwnerRepository ownerRepository) {
        String city3 = city.substring(0, Math.min(3, city.length())).toUpperCase();
        String last3 = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase();
        int sequence = countOwnersInCity(city, ownerRepository) + 1;
        return String.format("%s-%s-%04d", city3, last3, sequence);
    }

    /**
     * Count the existing owners in the given city, compared case-insensitively.
     */
    private static int countOwnersInCity(String city, OwnerRepository ownerRepository) {
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (city.equalsIgnoreCase(existing.getCity())) {
                count++;
            }
        }
        return count;
    }

    /**
     * Count the existing owners whose first and last names both match the given names,
     * compared case-insensitively.
     */
    private static int countNamesakes(String firstName, String lastName,
            OwnerRepository ownerRepository) {
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (firstName.equalsIgnoreCase(existing.getFirstName())
                    && lastName.equalsIgnoreCase(existing.getLastName())) {
                count++;
            }
        }
        return count;
    }
}
