package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.Membership;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits an audit line to the dedicated {@code AUDIT} logger on successful create,
 * carrying the newly-assigned owner id, its {@code customerCode}, its
 * {@code registrationDate}, its numeric {@code membershipLevel} and its
 * {@code membershipNumber}. Runs after {@link SaveOwner} (so the id exists) and
 * before {@link RespondWithOwnerCreated}.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner) {
        AUDIT.info("owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                Membership.levelOf(owner), owner.getMembershipNumber());
    }
}
