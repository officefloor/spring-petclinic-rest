package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits an audit line on the dedicated {@code AUDIT} logger after a successful create,
 * carrying the newly-assigned owner id, its customerCode and its registrationDate. Runs
 * after {@code save}, so the owner's generated id is available.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner) {
        AUDIT.info("Owner created id={} customerCode={} registrationDate={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate());
    }
}
