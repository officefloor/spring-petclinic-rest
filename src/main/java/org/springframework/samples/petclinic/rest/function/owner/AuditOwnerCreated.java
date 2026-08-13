package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits an audit line to the dedicated {@code AUDIT} logger on a successful
 * create, carrying the new owner's id, {@code customerCode},
 * {@code registrationDate}, numeric {@code membershipLevel} and the derived
 * {@code membershipNumber}.
 *
 * <p>Runs after {@link SaveOwner} (so the id is assigned) and before
 * {@link RespondWithOwnerCreated} in the {@code POST /api/owners} pipeline.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner) {
        String membershipNumber = String.format("%s-M%02d",
            owner.getCustomerCode(), owner.getRegistrationDate().getYear() % 100);
        AUDIT.info("owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
            owner.getMembershipLevel(), membershipNumber);
    }
}
