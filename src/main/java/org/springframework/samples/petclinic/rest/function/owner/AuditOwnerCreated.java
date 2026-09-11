package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits an audit line on the dedicated {@code AUDIT} logger for a successfully
 * created owner, carrying the owner id, customerCode and registrationDate. Runs
 * after {@link SaveOwner} so the generated id is available.
 */
public class AuditOwnerCreated {

    private static final Logger audit = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner) {
        audit.info("Owner created id={} customerCode={} registrationDate={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate());
    }
}
