package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.MemberId;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's unified {@code memberId}, formatted
 * {@code <REGION><FY><HASH8><CHK>} (see {@link MemberId}). When the computed id collides
 * with an existing owner's {@code memberId}, {@code -<n>} is appended with the smallest
 * {@code n} of 2 or more that makes it unique. Runs after {@link BuildOwner} (so the
 * telephone is normalized and the registrationDate is set) and before {@link SaveOwner}.
 */
public class AssignMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String base = MemberId.of(owner);
        owner.setMemberId(deduplicate(base, existingMemberIds(ownerRepository)));
    }

    private static Set<String> existingMemberIds(OwnerRepository ownerRepository) {
        Set<String> ids = new HashSet<>();
        for (Owner existing : ownerRepository.findAll()) {
            String id = existing.getMemberId();
            if (id != null) {
                ids.add(id);
            }
        }
        return ids;
    }

    /** Returns {@code base}, or {@code base-<n>} with the smallest {@code n >= 2} not in {@code taken}. */
    private static String deduplicate(String base, Set<String> taken) {
        if (!taken.contains(base)) {
            return base;
        }
        for (int n = 2; ; n++) {
            String candidate = base + "-" + n;
            if (!taken.contains(candidate)) {
                return candidate;
            }
        }
    }
}
