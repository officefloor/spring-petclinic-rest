package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code memberId}, formatted {@code <REGION><FY><HASH8><CHK>} (see
 * {@link MemberIds}). The identity depends only on the owner's own fields; there are no sequence
 * numbers.
 *
 * <p>When the computed id collides with an existing owner's {@code memberId}, {@code -<n>} is
 * appended with the smallest {@code n} of 2 or more that makes it unique.
 */
public class AssignMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String base = MemberIds.build(owner);
        Set<String> existing = new HashSet<>();
        for (Owner other : ownerRepository.findAll()) {
            if (other.getId() != null && other.getId().equals(owner.getId())) {
                continue;
            }
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
