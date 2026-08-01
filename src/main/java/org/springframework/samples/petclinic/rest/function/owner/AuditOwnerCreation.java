package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Logs every successful owner creation to the dedicated {@code AUDIT} logger,
 * recording the authenticated user together with the new owner's id and
 * membership number.
 */
public class AuditOwnerCreation {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String user = authentication != null ? authentication.getName() : "anonymous";
        AUDIT.info("owner created: user={} id={} membershipNumber={}", user, owner.getId(),
                owner.getMembershipNumber());
    }
}
