package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.util.CityLocality;
import org.springframework.samples.petclinic.util.FiscalYear;
import org.springframework.samples.petclinic.util.MemberId;
import org.springframework.samples.petclinic.util.Sha256;

/**
 * Assigns the new owner's {@code memberId}, formatted {@code <REGION><FY><HASH8><CHK>}: the
 * canonical region derived from the postcode (falling back to the city, see
 * {@link CityLocality}), the two-digit fiscal year of the registration date (fiscal year
 * starting 1 July), the first eight upper-case hex characters of the SHA-256 of the
 * normalized telephone concatenated with the last name, and a single Luhn check digit over
 * the digits of {@code <REGION><FY><HASH8>} (e.g. {@code NSW261A2B3C4D7}). Should the
 * composed id collide with an existing owner's, it is de-duplicated with a {@code -<n>}
 * suffix (see {@link MemberId#deduplicate}). Runs after {@link BuildOwner} (so the entity,
 * its postcode, telephone, last name and registration date exist) and before
 * {@link SaveOwner} persists the id.
 */
public class AssignOwnerMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = CityLocality.forPostcodeOrCity(owner.getPostcode(), owner.getCity());
        int shortFiscalYear = FiscalYear.shortYearOf(owner.getRegistrationDate());
        String hash8 = Sha256.upperHexPrefix(owner.getTelephone() + owner.getLastName(), 8);
        String memberId = MemberId.of(region, shortFiscalYear, hash8);
        Set<String> existing = ownerRepository.findAll().stream()
                .map(Owner::getMemberId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        owner.setMemberId(MemberId.deduplicate(memberId, existing));
    }
}
