package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.Regions;
import org.springframework.samples.petclinic.model.FiscalYear;
import org.springframework.samples.petclinic.model.MemberId;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Runs after {@link BuildOwner} and before {@link SaveOwner} on {@code POST /api/owners}. Assigns the
 * new owner's {@code memberId} formatted {@code <REGION><FY><HASH8><CHK>}, where REGION is the
 * version-2 region code derived from the postcode (see {@link Regions#regionForPostcodeV2(String)}: the
 * plain region with the {@code V2} identity-version tag mixed in, e.g. {@code NSWV2}, or
 * {@code UNKNOWNV2} when the postcode resolves to no known region), FY is the two-digit fiscal-year start
 * year of the (business-day-adjusted) registration date, HASH8 is the first 8 upper-case hex characters
 * of {@code SHA-256(normalizedTelephone + lastName)} and CHK is a single Luhn check digit over the
 * digits of {@code <REGION><FY><HASH8>} (see {@link MemberId#format(String, String, String)}, e.g.
 * {@code NSWV2261A2B3C4D7}). The version tag lives only inside the member id; the user-facing
 * {@code locality} keeps the plain region.
 *
 * <p>When the computed member id collides with an existing owner's {@code memberId}, {@code -<n>} is
 * appended with the smallest {@code n} of 2 or more that makes it unique (e.g. {@code NSW261A2B3C4D7-2}),
 * so distinct owners always get distinct member ids. The new owner is still unsaved, so it is not among
 * the owners compared against here and cannot collide with itself.
 */
public class AssignMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = Regions.regionForPostcodeV2(owner.getPostcode());
        String fiscalYear = FiscalYear.twoDigitStartYear(owner.getRegistrationDate());
        String hash8 = MemberId.hash8(owner.getTelephone(), owner.getLastName());
        String base = MemberId.format(region, fiscalYear, hash8);

        Set<String> existing = new HashSet<>();
        for (Owner other : ownerRepository.findAll()) {
            String memberId = other.getMemberId();
            if (memberId != null) {
                existing.add(memberId);
            }
        }

        String memberId = base;
        for (int n = 2; existing.contains(memberId); n++) {
            memberId = MemberId.withSuffix(base, n);
        }
        owner.setMemberId(memberId);
    }
}
