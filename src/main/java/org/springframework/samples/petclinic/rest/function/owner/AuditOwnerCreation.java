package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Logs every successful owner creation to the dedicated {@code AUDIT} logger,
 * recording the authenticated user together with the new owner's id and
 * membership number.
 *
 * <p>Additionally, when the new owner's telephone shares its area code (the
 * first three digits) with five or more <em>existing</em> owners, emits a WARN
 * flagging a possible bulk signup. This step runs after the owner is saved, so
 * {@link OwnerRepository#findAll()} includes the new owner; it is excluded from
 * the count by id.
 */
public class AuditOwnerCreation {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Owners sharing an area code at or above this threshold trigger a bulk-signup WARN. */
    private static final int BULK_SIGNUP_THRESHOLD = 5;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String user = authentication != null ? authentication.getName() : "anonymous";
        AUDIT.info("owner created: user={} id={} membershipNumber={}", user, owner.getId(),
                owner.getMembershipNumber());

        String areaCode = areaCode(owner.getTelephone());
        if (areaCode != null) {
            long existingSharing = ownerRepository.findAll().stream()
                    .filter(existing -> existing.getId() == null || !existing.getId().equals(owner.getId()))
                    .filter(existing -> areaCode.equals(areaCode(existing.getTelephone())))
                    .count();
            if (existingSharing >= BULK_SIGNUP_THRESHOLD) {
                AUDIT.warn("possible bulk signup: user={} id={} areaCode={} existingSharing={}", user,
                        owner.getId(), areaCode, existingSharing);
            }
        }
    }

    /** The first three digits of a telephone, or {@code null} if there are fewer than three. */
    private static String areaCode(String telephone) {
        return telephone != null && telephone.length() >= 3 ? telephone.substring(0, 3) : null;
    }
}
