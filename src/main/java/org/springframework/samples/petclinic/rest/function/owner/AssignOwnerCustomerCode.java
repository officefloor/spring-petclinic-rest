package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the new owner's {@code customerCode}, formatted {@code <CITY3>-<LAST3>-<NNNN>}:
 * the upper-cased first three letters of the city, a hyphen, the upper-cased first three
 * letters of the last name, a hyphen, and a per-city 4-digit zero-padded sequence equal to
 * one more than the number of owners already in that city (e.g. {@code FRA-SMI-0007}). Runs
 * after {@link BuildOwner} (so the entity, its city and last name exist) and before
 * {@link SaveOwner} persists the code.
 */
public class AssignOwnerCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        long sequence = Cities.countIn(ownerRepository, owner.getCity()) + 1;
        owner.setCustomerCode(String.format("%s-%s-%04d",
                prefix(owner.getCity()), prefix(owner.getLastName()), sequence));
    }

    private static String prefix(String value) {
        return value.substring(0, Math.min(3, value.length())).toUpperCase(Locale.ROOT);
    }
}
