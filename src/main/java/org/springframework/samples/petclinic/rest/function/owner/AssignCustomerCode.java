package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.Locality;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code '<REGION>-<HASH8>'} where REGION is the
 * region code derived from the owner's postcode (via {@link Locality}, postcode first then city) and
 * HASH8 is the first 8 upper-case hex characters of SHA-256 over the normalised telephone
 * concatenated with the last name. There is no per-city sequence number.
 *
 * <p>When the computed code collides with an existing owner's {@code customerCode}, {@code '-<n>'}
 * is appended with the smallest {@code n} of 2 or more that makes it unique, so distinct owners
 * always end up with distinct customer codes. Mutates the built owner in place before it is saved.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = Locality.of(owner.getCity(), owner.getPostcode());
        String hash8 = OwnerIdentity.customerHash(owner.getTelephone(), owner.getLastName());
        String base = region + "-" + hash8;

        Set<String> taken = new HashSet<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getCustomerCode() != null) {
                taken.add(existing.getCustomerCode());
            }
        }

        String code = base;
        for (int n = 2; taken.contains(code); n++) {
            code = base + "-" + n;
        }
        owner.setCustomerCode(code);
    }
}
