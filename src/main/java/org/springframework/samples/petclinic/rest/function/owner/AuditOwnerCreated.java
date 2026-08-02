package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Records every successful owner creation to the dedicated {@code AUDIT} logger,
 * capturing the authenticated user together with the new owner's id and
 * membership number. Runs after the owner has been saved (and so has an id).
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String user = (authentication != null) ? authentication.getName() : "anonymous";
        java.util.Collection<Owner> allOwners = ownerRepository.findAll();
        Integer membershipNumber = OwnerMapper.membershipNumber(owner, allOwners);
        AUDIT.info("owner created user={} id={} membershipNumber={}", user, owner.getId(), membershipNumber);

        String areaCode = areaCode(owner.getTelephone());
        if (areaCode != null) {
            long existingSharing = allOwners.stream()
                    .filter(o -> o.getId() == null || !o.getId().equals(owner.getId()))
                    .filter(o -> areaCode.equals(areaCode(o.getTelephone())))
                    .count();
            if (existingSharing >= 5) {
                AUDIT.warn("possible bulk signup user={} id={} areaCode={} existingSharing={}",
                        user, owner.getId(), areaCode, existingSharing);
            }
        }
    }

    /** First three digits of a telephone number, or {@code null} if unavailable. */
    private static String areaCode(String telephone) {
        return (telephone != null && telephone.length() >= 3) ? telephone.substring(0, 3) : null;
    }
}
