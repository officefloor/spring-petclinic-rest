package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.util.CityLocality;
import org.springframework.samples.petclinic.util.CustomerCode;
import org.springframework.samples.petclinic.util.Sha256;

/**
 * Assigns the new owner's {@code customerCode}, formatted {@code <REGION>-<HASH8>}: the
 * canonical region derived from the postcode (falling back to the city, see
 * {@link CityLocality}), a hyphen, and the first eight upper-case hex characters of the
 * SHA-256 of the normalized telephone concatenated with the last name (e.g.
 * {@code NSW-1A2B3C4D}). Should the composed code collide with an existing owner's, it is
 * de-duplicated with a {@code -<n>} suffix (see {@link CustomerCode#deduplicate}). Runs
 * after {@link BuildOwner} (so the entity, its postcode, telephone and last name exist)
 * and before {@link SaveOwner} persists the code.
 */
public class AssignOwnerCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = CityLocality.forPostcodeOrCity(owner.getPostcode(), owner.getCity());
        String hash8 = Sha256.upperHexPrefix(owner.getTelephone() + owner.getLastName(), 8);
        String code = CustomerCode.of(region, hash8);
        Set<String> existing = ownerRepository.findAll().stream()
                .map(Owner::getCustomerCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        owner.setCustomerCode(CustomerCode.deduplicate(code, existing));
    }
}
