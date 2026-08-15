package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.function.common.CustomerCodes;

/**
 * Step of {@code POST /api/owners} that assigns the owner's {@code customerCode}, formatted
 * {@code '<REGION>-<HASH8>'} where {@code REGION} is the region derived from the postcode (falling
 * back to the city, see {@link org.springframework.samples.petclinic.rest.function.common.Localities})
 * and {@code HASH8} is the first eight upper-case hex characters of
 * {@code SHA-256(normalizedTelephone + lastName)} (e.g. {@code 'NSW-1A2B3C4D'}).
 *
 * <p>Runs after {@link BuildOwner} and before {@link SaveOwner}; {@code @Val} yields the built owner so
 * the code is mutated in place and persisted by the save step. The identity is derived entirely from
 * the owner's own fields — the telephone is already normalized to E.164 by this point — so it is
 * seed-independent.
 *
 * <p>Should the computed code collide with an existing owner's {@code customerCode}, {@code '-<n>'} is
 * appended with the smallest {@code n >= 2} that makes it unique, so distinct owners always receive
 * distinct codes.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        Set<String> taken = new HashSet<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getCustomerCode() != null) {
                taken.add(existing.getCustomerCode());
            }
        }
        owner.setCustomerCode(CustomerCodes.deduplicate(CustomerCodes.of(owner), taken));
    }
}
