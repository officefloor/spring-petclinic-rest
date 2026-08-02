package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Records a successful owner creation to the dedicated {@code AUDIT} logger,
 * capturing the authenticated user together with the new owner's id and
 * membership number.
 */
public class AuditOwnerCreation {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String user = (authentication != null) ? authentication.getName() : "anonymous";
        AUDIT.info("Owner created by user={} id={} membershipNumber={}",
                user, owner.getId(), owner.getMembershipNumber());
    }
}
