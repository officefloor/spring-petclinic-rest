package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.CityRegion;
import org.springframework.samples.petclinic.mapper.FiscalYear;
import org.springframework.samples.petclinic.mapper.MemberId;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Step of {@code POST /api/owners} that assigns the owner's {@code memberId}.
 *
 * <p>The ID is formatted {@code '<REGION><FY><HASH8><CHK>'}, where REGION is the canonical region
 * derived from the postcode (postcode-preferred, city-table fallback; see {@link CityRegion}), FY is
 * the two-digit fiscal year of the owner's (already resolved/defaulted) registration date, HASH8 is
 * the first 8 upper-case hex characters of SHA-256 over {@code normalizedTelephone + lastName}, and
 * CHK is a single Luhn check digit over the digits of {@code <REGION><FY><HASH8>}
 * (e.g. {@code 'NSW261A2B3C4D7'}). The telephone has already been normalized to E.164 form by
 * {@link NormalizeOwnerTelephone}, so it feeds the hash verbatim.
 *
 * <p>When the computed ID collides with an existing owner's {@code memberId}, {@code '-<n>'} is
 * appended with the smallest {@code n} of 2 or more that makes it unique (e.g. {@code 'NSW261A2B3C4D7-2'}).
 */
public class AssignMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = CityRegion.locality(owner.getPostcode(), owner.getCity());
        String fiscalYear = owner.getRegistrationDate() == null ? ""
                : FiscalYear.yearSegment(owner.getRegistrationDate());
        String base = MemberId.of(region, fiscalYear, owner.getTelephone(), owner.getLastName());

        Set<String> existing = new HashSet<>();
        for (Owner other : ownerRepository.findAll()) {
            String id = other.getMemberId();
            if (id != null) {
                existing.add(id);
            }
        }

        String candidate = base;
        for (int n = 2; existing.contains(candidate); n++) {
            candidate = base + "-" + n;
        }
        owner.setMemberId(candidate);
    }
}
