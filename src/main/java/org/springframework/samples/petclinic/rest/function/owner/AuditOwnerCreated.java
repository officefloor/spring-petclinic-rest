package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Step of {@code POST /api/owners}: emits the create audit trail. Runs after
 * {@link SaveOwner} so the owner carries its persisted id, logging one line to the
 * dedicated {@code AUDIT} logger with the owner id, {@code memberId} (see
 * {@link OwnerIdentity}), {@code registrationDate}, {@code membershipPoints} and
 * {@code membershipLevel}.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner) {
        AUDIT.info("Owner created id={} memberId={} registrationDate={} membershipPoints={} membershipLevel={}",
                owner.getId(), OwnerIdentity.primary(owner), owner.getRegistrationDate(),
                owner.getMembershipPoints(), owner.getMembershipLevel());
    }
}
