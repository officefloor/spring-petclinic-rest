package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits an audit line on the dedicated {@code AUDIT} logger for a successful create. Runs after
 * {@link SaveOwner}, so the owner has its persisted id, and after {@link AssignMemberId}, so
 * the {@code memberId} is set. The line carries the owner id, the memberId and the
 * (business-day-adjusted) registrationDate and the numeric membershipLevel.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner) {
        AUDIT.info(
                "owner created id={} memberId={} registrationDate={} membershipLevel={}",
                owner.getId(), owner.getMemberId(), owner.getRegistrationDate(),
                owner.getMembershipLevel());
    }
}
