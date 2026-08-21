package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits an audit trail line for a newly created owner. Runs after the owner has been
 * saved so the assigned id is available. The line is published to the dedicated
 * {@code AUDIT} logger and carries the owner id, the customerCode and the
 * registrationDate.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner) {
        AUDIT.info("Owner created: id={} customerCode={} registrationDate={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate());
    }
}
