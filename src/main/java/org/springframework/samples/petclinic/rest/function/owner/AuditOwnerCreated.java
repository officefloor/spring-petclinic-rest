package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits an audit line on successful owner creation via the dedicated {@code AUDIT}
 * logger, carrying the owner id, the {@code customerCode} and the
 * {@code registrationDate}.
 *
 * <p>Runs after {@code SaveOwner} so the owner id has been assigned.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner) {
        AUDIT.info("Owner created id={} customerCode={} registrationDate={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate());
    }
}
