package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.util.CustomerCode;
import org.springframework.samples.petclinic.util.Locality;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code '<REGION>-<HASH8>'} where REGION is the
 * region derived from the owner's postcode (falling back to the city table, see {@link Locality}) and
 * HASH8 is the first eight upper-case hex characters of SHA-256 over the {@code normalizedTelephone +
 * lastName} (e.g. 'NSW-1A2B3C4D'). There are no per-city sequence numbers: the identity is a pure
 * function of the owner's region and the telephone/last-name hash. Runs after the owner is built (so the
 * telephone is already normalized to E.164) and before it is saved, mutating the code in place via
 * {@code @Val} for the save/respond steps to persist and return.
 *
 * <p>When the computed code collides with an existing owner's {@code customerCode}, {@code '-<n>'} is
 * appended with the smallest {@code n} of 2 or more that makes it unique, and the de-duplicated code is
 * assigned (e.g. a second 'NSW-1A2B3C4D' becomes 'NSW-1A2B3C4D-2', a third 'NSW-1A2B3C4D-3').
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = Locality.of(owner.getCity(), owner.getPostcode());
        String base = CustomerCode.of(region, owner.getTelephone(), owner.getLastName());
        Set<String> existing = ownerRepository.findAll().stream()
                .map(Owner::getCustomerCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        String code = base;
        for (int n = 2; existing.contains(code); n++) {
            code = base + "-" + n;
        }
        owner.setCustomerCode(code);
    }
}
