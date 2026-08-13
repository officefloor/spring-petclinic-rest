package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code '<CITY3>-<LAST3>-<NNNN>'}
 * where {@code CITY3} is the upper-cased first three letters of {@code city},
 * {@code LAST3} the upper-cased first three letters of {@code lastName} and
 * {@code NNNN} a per-city 4-digit zero-padded sequence equal to one more than the
 * number of owners already in that city (e.g. {@code 'SYD-SMI-0007'}).
 *
 * <p>Runs after {@link BuildOwner} and before {@link SaveOwner} in the
 * {@code POST /api/owners} pipeline, so the count it reads excludes the owner being
 * created; the code is then persisted with the new owner and returned by later reads.
 * It mutates the built {@link Owner} in place (see {@code @Val} semantics).
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        long inCity = ownerRepository.findAll().stream()
            .filter(existing -> city == null
                ? existing.getCity() == null
                : city.equalsIgnoreCase(existing.getCity()))
            .count();
        long sequence = inCity + 1;
        owner.setCustomerCode(
            String.format("%s-%s-%04d", first3(city), first3(owner.getLastName()), sequence));
    }

    private static String first3(String value) {
        String prefix = value == null ? "" : value;
        return prefix.substring(0, Math.min(3, prefix.length())).toUpperCase();
    }
}
