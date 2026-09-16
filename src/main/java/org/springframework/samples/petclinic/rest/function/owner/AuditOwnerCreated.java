package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.MembershipLevel;

/**
 * Emits an audit trail entry for a successfully created owner. Runs after
 * {@link SaveOwner} (so the owner has its generated id) and before the response, logging
 * the owner id, member id, registration date and membership level to the dedicated
 * {@code AUDIT} logger.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner) {
        AUDIT.info("Created owner id={} memberId={} registrationDate={} membershipLevel={}",
                owner.getId(), owner.getMemberId(), owner.getRegistrationDate(),
                MembershipLevel.of(owner));
    }
}
