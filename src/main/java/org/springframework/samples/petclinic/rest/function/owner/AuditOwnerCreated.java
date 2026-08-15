package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.function.common.Membership;

/**
 * Step of {@code POST /api/owners} that emits an audit record for a successful create. Runs after
 * {@link SaveOwner} (so the owner's generated id is available) and before
 * {@link RespondWithOwnerCreated}.
 *
 * <p>The line is published to the dedicated {@code AUDIT} logger and carries the owner id, the
 * assigned {@code customerCode}, the stored (business-day-adjusted) {@code registrationDate}, the
 * numeric {@code membershipLevel} and the assigned {@code membershipNumber}.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner) {
        AUDIT.info(
                "Owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                owner.getMembershipLevel() != null ? owner.getMembershipLevel()
                        : Membership.levelOf(owner),
                owner.getMembershipNumber());
    }
}
