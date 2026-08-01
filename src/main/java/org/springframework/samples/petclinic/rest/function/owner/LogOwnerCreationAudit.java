package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records a successful owner creation to the dedicated {@code AUDIT} logger,
 * capturing the authenticated user together with the new owner's id and
 * membership number.
 *
 * <p>Additionally flags a possible bulk signup: when the new owner's telephone
 * shares its area code (first three digits) with {@link #BULK_AREA_CODE_THRESHOLD}
 * or more existing owners, a WARN is emitted to the same logger.
 */
public class LogOwnerCreationAudit {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Existing owners sharing the area code that trigger a bulk-signup WARN. */
    static final int BULK_AREA_CODE_THRESHOLD = 5;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String user = (authentication != null) ? authentication.getName() : "unknown";
        AUDIT.info("owner created: user={} id={} membershipNumber={}",
                user, owner.getId(), owner.getMembershipNumber());

        String areaCode = areaCodeOf(owner.getTelephone());
        if (areaCode == null) {
            return;
        }
        long sharing = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (isSameOwner(owner, existing)) {
                continue;
            }
            if (areaCode.equals(areaCodeOf(existing.getTelephone()))) {
                sharing++;
            }
        }
        if (sharing >= BULK_AREA_CODE_THRESHOLD) {
            AUDIT.warn("possible bulk signup: user={} id={} areaCode={} sharedByExistingOwners={}",
                    user, owner.getId(), areaCode, sharing);
        }
    }

    /** The first three digits of a telephone, or {@code null} when there are fewer than three. */
    private static String areaCodeOf(String telephone) {
        return (telephone != null && telephone.length() >= 3) ? telephone.substring(0, 3) : null;
    }

    private static boolean isSameOwner(Owner a, Owner b) {
        return a.getId() != null && Objects.equals(a.getId(), b.getId());
    }
}
