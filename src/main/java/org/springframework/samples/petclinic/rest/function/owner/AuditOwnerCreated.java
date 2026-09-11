package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits an audit record for a freshly created owner via the dedicated {@code AUDIT}
 * logger. Runs after the owner is saved, so its generated id is populated. The line
 * carries the owner id, the assigned customerCode and the registrationDate.
 */
public class AuditOwnerCreated {

    private static final Logger audit = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner) {
        audit.info("Owner created id={} customerCode={} registrationDate={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate());
    }
}
