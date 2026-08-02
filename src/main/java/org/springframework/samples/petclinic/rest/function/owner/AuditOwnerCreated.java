package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Logs every successful owner creation to the dedicated {@code AUDIT} logger,
 * recording the authenticated user together with the new owner's id and
 * membership number. Runs after the owner is saved, so its id is assigned.
 *
 * <p>Additionally, when the new owner's telephone area code (its first three
 * digits) is already shared by five or more existing owners, emits a {@code WARN}
 * to the same logger flagging a possible bulk signup.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Existing owners sharing the area code at or above which a bulk signup is flagged. */
    static final int BULK_SIGNUP_THRESHOLD = 5;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String user = (authentication != null) ? authentication.getName() : "anonymous";
        AUDIT.info("Owner created by user={} id={} membershipNumber={}",
                user, owner.getId(), owner.getMembershipNumber());

        String areaCode = areaCode(owner.getTelephone());
        if (areaCode == null) {
            return;
        }
        long sharing = ownerRepository.findAll().stream()
                .filter(existing -> isDifferentOwner(existing, owner))
                .filter(existing -> areaCode.equals(areaCode(existing.getTelephone())))
                .count();
        if (sharing >= BULK_SIGNUP_THRESHOLD) {
            AUDIT.warn("Possible bulk signup: owner id={} telephone={} shares area code {} "
                    + "with {} existing owners",
                    owner.getId(), owner.getTelephone(), areaCode, sharing);
        }
    }

    private static boolean isDifferentOwner(Owner existing, Owner candidate) {
        return existing.getId() == null || !existing.getId().equals(candidate.getId());
    }

    /** The first three digits of a telephone number, or {@code null} if unavailable. */
    private static String areaCode(String telephone) {
        return (telephone != null && telephone.length() >= 3) ? telephone.substring(0, 3) : null;
    }
}
