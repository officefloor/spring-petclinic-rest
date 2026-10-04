package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code <CITY3>-<LAST3>-<NNNN>} where
 * {@code CITY3} is the upper-cased first three letters of {@code city}, {@code LAST3} the
 * upper-cased first three letters of {@code lastName} and {@code NNNN} a per-city 4-digit
 * zero-padded sequence equal to one more than the number of owners already in that city
 * (e.g. {@code SYD-SMI-0007}).
 *
 * <p>Runs after {@link BuildOwner} and before {@link SaveOwner}, so the per-city count
 * reflects the owners that already exist and excludes the owner being created.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        int sequence = (int) ownerRepository.findAll().stream()
            .filter(existing -> city.equalsIgnoreCase(existing.getCity()))
            .count() + 1;
        owner.setCustomerCode(String.format("%s-%s-%04d", prefix3(city), prefix3(owner.getLastName()), sequence));
    }

    /** The upper-cased first three letters of {@code value} (or fewer if it is shorter). */
    private static String prefix3(String value) {
        return value.substring(0, Math.min(3, value.length())).toUpperCase();
    }
}
