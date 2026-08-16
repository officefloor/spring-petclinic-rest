package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code memberId}, formatted {@code <REGION><FY><HASH8><CHK>} (see
 * {@link MemberId}): REGION is the version-2 region code — the plain region derived from the
 * postcode/city (see {@link CityRegion#localityOf(String, String)}) with the fixed
 * {@link OwnerIdentity#VERSION_TAG} appended, FY is the 2-digit fiscal year of the registrationDate
 * (see {@link FiscalYear}), HASH8 is the first 8 upper-case hex characters of SHA-256 over
 * {@code VERSION_TAG + normalizedTelephone + lastName} (see {@link OwnerIdentity#memberIdHash}) and
 * CHK is a single Luhn check digit over the digits of {@code <REGION><FY><HASH8>}. For example
 * {@code NSWV2261A2B3C4D7}. The {@code V2} tag lives only inside the id: the derived
 * {@code locality}/{@code timezone} and owner segment read the plain region back off the prefix.
 *
 * <p>Runs after {@link BuildOwner} (which defaults the registration date) and
 * {@link ResolveRegistrationDate}, so the fiscal year is known. The base id depends only on this
 * owner's own fields and is stable regardless of how many owners exist. When that base collides with
 * an existing owner's {@code memberId}, this step de-duplicates it by appending {@code -<n>} with the
 * smallest {@code n} of 2 or more that makes it unique (e.g. {@code NSW261A2B3C4D7-2}). Every value
 * derived from the member id — the create audit line (see {@link AuditOwnerCreated}), the response
 * {@code locality} and the {@code ownerSegment} — follows the de-duplicated id.
 */
public class AssignMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = CityRegion.localityOf(owner.getCity(), owner.getPostcode())
                + OwnerIdentity.VERSION_TAG;
        int fiscalYearYY = FiscalYear.endingYearOf(owner.getRegistrationDate()) % 100;
        String hash8 = OwnerIdentity.memberIdHash(owner.getTelephone(), owner.getLastName());
        String base = MemberId.of(region, fiscalYearYY, hash8);

        Set<String> existing = new HashSet<>();
        for (Owner other : ownerRepository.findAll()) {
            if (other.getMemberId() != null) {
                existing.add(other.getMemberId());
            }
        }

        String memberId = base;
        for (int n = 2; existing.contains(memberId); n++) {
            memberId = base + "-" + n;
        }
        owner.setMemberId(memberId);
    }
}
