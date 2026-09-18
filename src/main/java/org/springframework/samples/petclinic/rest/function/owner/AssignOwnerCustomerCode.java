package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.util.CustomerCode;
import org.springframework.samples.petclinic.util.OwnerRegion;

/**
 * Assigns a newly built owner's {@code customerCode}, formatted {@code '<REGION>-<HASH8>'} where
 * REGION is the owner's {@link OwnerRegion region} (derived from its postcode) and HASH8 is the
 * first 8 upper-case hex characters of {@code SHA-256(normalizedTelephone + lastName)} (e.g.
 * 'NSW-1A2B3C4D'). The code is a deterministic function of the owner's region and identity, so on
 * the rare collision with an existing owner's code it is de-duplicated by appending {@code '-<n>'}
 * with the smallest {@code n >= 2} that stays unique. The telephone has already been normalized to
 * E.164 earlier in the pipeline, so {@link Owner#getTelephone()} is the normalized telephone the
 * hash covers. Runs before the owner is persisted, so {@link OwnerRepository#findAll()} returns
 * only the codes taken by existing owners and never the owner itself. The code is stored in place
 * on the built entity so it is persisted and returned; the membership number, its check digit and
 * the locality are all derived from it. Runs after {@link BuildOwner}.
 */
public class AssignOwnerCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = OwnerRegion.of(owner.getPostcode(), owner.getCity());
        String code = CustomerCode.of(region, owner.getTelephone(), owner.getLastName());
        Set<String> taken = ownerRepository.findAll().stream()
                .map(Owner::getCustomerCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        owner.setCustomerCode(CustomerCode.deduplicate(code, taken));
    }
}
