package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code memberId}, the unified identity {@code '<REGION><FY><HASH8><CHK>'}
 * where REGION is the region code derived from the postcode (falling back to the city, then
 * {@code 'UNKNOWN'}), FY the two-digit fiscal year of the registrationDate, HASH8 the first 8
 * upper-case hex characters of SHA-256 over {@code normalizedTelephone + lastName} and CHK a single
 * Luhn check digit over the digits of {@code <REGION><FY><HASH8>} (e.g. {@code 'NSW271A2B3C4D5'}).
 * See {@link MemberId}.
 *
 * <p>The base memberId is a pure function of the owner's already-normalized telephone, last name,
 * postcode, city and registrationDate. When that base collides with an existing owner's
 * {@code memberId}, this step de-duplicates it by appending {@code '-<n>'} with the smallest
 * {@code n >= 2} that makes the result unique across all persisted owners. Runs after
 * {@link BuildOwner} has mapped the request onto the entity (telephone already normalized to E.164
 * by {@link ValidateOwnerFields}) and {@link DefaultRegistrationDate} has resolved the date, and
 * before {@link SaveOwner}, within the same write transaction so the uniqueness check and the insert
 * see one consistent view.
 */
public class AssignMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String base = MemberId.of(owner.getTelephone(), owner.getLastName(), owner.getPostcode(),
                owner.getCity(), owner.getRegistrationDate());

        Set<String> taken = new HashSet<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing != owner && existing.getMemberId() != null) {
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
