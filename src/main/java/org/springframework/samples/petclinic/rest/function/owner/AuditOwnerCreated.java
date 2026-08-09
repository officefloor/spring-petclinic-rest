package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits an audit line to the dedicated {@code AUDIT} logger recording a successful owner
 * create. Runs after {@link SaveOwner} (so the owner id is assigned) and before the
 * responder. The line carries the owner id, the {@code customerCode}, the
 * {@code registrationDate}, the {@code membershipLevel} and the
 * {@code membershipNumber}.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner) {
        AUDIT.info("owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                owner.getMembershipLevel(), owner.getMembershipNumber());
    }
}
