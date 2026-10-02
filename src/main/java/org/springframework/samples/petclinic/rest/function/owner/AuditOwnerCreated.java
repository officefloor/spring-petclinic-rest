package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits one audit line per successful owner create, carrying the owner id, the
 * customerCode, the registrationDate, the membershipLevel and the membershipNumber.
 * Runs after the owner is saved (so the id is populated) and before the responder,
 * keeping the audit side effect out of the responder step.
 */
public class AuditOwnerCreated {

    /** Dedicated audit channel; one line per successful create. */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner) {
        AUDIT.info("Owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                owner.getMembershipLevel(), owner.getMembershipNumber());
    }
}
