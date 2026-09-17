package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.MembershipLevel;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.OwnerPrimaryIdentifier;

/**
 * Emits an audit trail entry once an owner has been persisted, capturing the
 * generated id together with the assigned memberId (the unified
 * {@code <REGION><FY><HASH8><CHK>} identity), registration date and membership level.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner) {
        AUDIT.info(
                "Owner created: id={} memberId={} registrationDate={} membershipLevel={}",
                owner.getId(), OwnerPrimaryIdentifier.of(owner), owner.getRegistrationDate(),
                MembershipLevel.effective(owner));
    }
}
