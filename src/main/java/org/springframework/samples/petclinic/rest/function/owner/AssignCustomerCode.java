package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Step of {@code POST /api/owners} that assigns the owner's {@code customerCode}, formatted
 * {@code '<CITY3>-<LAST3>-<NNNN>'} where {@code CITY3} is the upper-cased first three letters of
 * city, {@code LAST3} the upper-cased first three letters of lastName and {@code NNNN} is a
 * per-city 4-digit zero-padded sequence equal to one more than the number of owners already in
 * that city (e.g. {@code 'SYD-SMI-0007'}).
 *
 * <p>Runs after {@link BuildOwner} and before {@link SaveOwner}; {@code @Val} yields the built
 * owner so the code is mutated in place and persisted by the save step. The sequence is counted
 * before this owner is saved, so successive creates in the same city receive consecutive numbers.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        String cityPrefix = city.substring(0, Math.min(3, city.length())).toUpperCase(Locale.ROOT);
        String lastName = owner.getLastName();
        String lastPrefix = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase(Locale.ROOT);
        int sequence = (int) ownerRepository.findAll().stream()
                .filter(o -> o.getCity() != null && o.getCity().equalsIgnoreCase(city))
                .count() + 1;
        owner.setCustomerCode(String.format("%s-%s-%04d", cityPrefix, lastPrefix, sequence));
    }
}
