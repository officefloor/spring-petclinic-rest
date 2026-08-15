package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode}, the region-and-hash identity {@code '<REGION>-<HASH8>'}
 * where REGION is the region code derived from the postcode (falling back to the city, then
 * {@code 'UNKNOWN'}) and HASH8 is the first 8 upper-case hex characters of SHA-256 over
 * {@code normalizedTelephone + lastName} (e.g. {@code 'NSW-1A2B3C4D'}). See {@link CustomerCode}.
 *
 * <p>The base code is a pure function of the owner's already-normalized telephone, last name,
 * postcode and city. When that base collides with an existing owner's {@code customerCode}, this
 * step de-duplicates it by appending {@code '-<n>'} with the smallest {@code n >= 2} that makes the
 * result unique across all persisted owners. Runs after {@link BuildOwner} has mapped the request
 * onto the entity (telephone already normalized to E.164 by {@link ValidateOwnerFields}) and before
 * {@link SaveOwner}, within the same write transaction so the uniqueness check and the insert see
 * one consistent view.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String base = CustomerCode.of(
                owner.getTelephone(), owner.getLastName(), owner.getPostcode(), owner.getCity());

        Set<String> taken = new HashSet<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing != owner && existing.getCustomerCode() != null) {
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
