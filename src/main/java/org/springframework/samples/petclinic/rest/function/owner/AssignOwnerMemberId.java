package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.util.FiscalYear;
import org.springframework.samples.petclinic.util.MemberId;
import org.springframework.samples.petclinic.util.OwnerRegion;

/**
 * Assigns a newly built owner's {@code memberId}, formatted {@code '<REGION><FY><HASH8><CHK>'} where
 * REGION is the owner's {@link OwnerRegion region} (derived from its postcode), FY is the 2-digit
 * {@link FiscalYear fiscal year} of its registration date, HASH8 is the first 8 upper-case hex
 * characters of {@code SHA-256(normalizedTelephone + lastName)} and CHK is the Luhn check digit over
 * the preceding digits (e.g. 'NSW271A2B3C4D5'). The id is a deterministic function of the owner's
 * region, fiscal year and identity, so on the rare collision with an existing owner's id it is
 * de-duplicated by appending {@code '-<n>'} with the smallest {@code n >= 2} that stays unique. The
 * telephone has already been normalized to E.164 earlier in the pipeline, so
 * {@link Owner#getTelephone()} is the normalized telephone the hash covers, and the registration
 * date has already been resolved so its fiscal year is final. Runs before the owner is persisted, so
 * {@link OwnerRepository#findAll()} returns only the ids taken by existing owners and never the
 * owner itself. The id is stored in place on the built entity so it is persisted and returned; the
 * fiscal year and the locality are derived from it. Runs after {@link BuildOwner}.
 */
public class AssignOwnerMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = OwnerRegion.of(owner.getPostcode(), owner.getCity());
        int fiscalYear = FiscalYear.of(owner.getRegistrationDate());
        String memberId = MemberId.of(region, fiscalYear, owner.getTelephone(), owner.getLastName());
        Set<String> taken = ownerRepository.findAll().stream()
                .map(Owner::getMemberId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        owner.setMemberId(MemberId.deduplicate(memberId, taken));
    }
}
