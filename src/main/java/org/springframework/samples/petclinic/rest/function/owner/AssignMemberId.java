package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.CheckDigit;
import org.springframework.samples.petclinic.mapper.FiscalYear;
import org.springframework.samples.petclinic.mapper.Locality;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's unified {@code memberId}, formatted {@code '<REGION><FY><HASH8><CHK>'}:
 * REGION is the region code derived from the owner's postcode (via {@link Locality}, postcode first
 * then city); FY is the two-digit fiscal year of the owner's registration date (via
 * {@link FiscalYear}); HASH8 is the first 8 upper-case hex characters of SHA-256 over the version-2
 * region code concatenated with the normalised telephone and last name (see
 * {@link OwnerIdentity#customerHash}) &mdash; the {@code 'V2'} tag lives inside this hash, so the
 * visible REGION prefix stays the plain region; and CHK is a single {@link CheckDigit Luhn check
 * digit} computed over the digits of {@code '<REGION><FY><HASH8>'}.
 *
 * <p>When the computed member id collides with an existing owner's {@code memberId}, {@code '-<n>'}
 * is appended with the smallest {@code n} of 2 or more that makes it unique, so distinct owners
 * always end up with distinct member ids. Mutates the built owner in place before it is saved.
 */
public class AssignMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = Locality.of(owner.getCity(), owner.getPostcode());
        String fy = String.format("%02d", FiscalYear.yearSegment(owner.getRegistrationDate()));
        String hash8 = OwnerIdentity.customerHash(region, owner.getTelephone(), owner.getLastName());
        String core = region + fy + hash8;
        String base = core + CheckDigit.of(core);

        Set<String> taken = new HashSet<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getMemberId() != null) {
                taken.add(existing.getMemberId());
            }
        }

        String memberId = base;
        for (int n = 2; taken.contains(memberId); n++) {
            memberId = base + "-" + n;
        }
        owner.setMemberId(memberId);
    }
}
