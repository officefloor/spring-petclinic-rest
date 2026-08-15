package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.function.common.MemberIds;

/**
 * Step of {@code POST /api/owners} that assigns the owner's {@code memberId}, formatted
 * {@code '<REGION><FY><HASH8><CHK>'} where {@code REGION} is the region derived from the postcode
 * (falling back to the city, see
 * {@link org.springframework.samples.petclinic.rest.function.common.Localities}), {@code FY} is the
 * two-digit fiscal year of the business-day-adjusted {@code registrationDate}, {@code HASH8} is the
 * first eight upper-case hex characters of {@code SHA-256(normalizedTelephone + lastName)} and
 * {@code CHK} is a single Luhn check digit over the digits of {@code <REGION><FY><HASH8>}
 * (e.g. {@code 'NSW261A2B3C4D7'}).
 *
 * <p>Runs after {@link BuildOwner} (which sets the {@code registrationDate}) and before
 * {@link SaveOwner}; {@code @Val} yields the built owner so the id is mutated in place and persisted
 * by the save step. The identity is derived entirely from the owner's own fields — the telephone is
 * already normalized to E.164 by this point — so it is seed-independent.
 *
 * <p>Should the computed id collide with an existing owner's {@code memberId}, {@code '-<n>'} is
 * appended with the smallest {@code n >= 2} that makes it unique, so distinct owners always receive
 * distinct ids.
 */
public class AssignMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        Set<String> taken = new HashSet<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getMemberId() != null) {
                taken.add(existing.getMemberId());
            }
        }
        owner.setMemberId(MemberIds.deduplicate(MemberIds.of(owner), taken));
    }
}
