package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Runs after {@link SaveOwner} on {@code POST /api/owners}, once the owner has been persisted and
 * assigned an id. Emits an audit line via the dedicated {@code AUDIT} logger carrying the new owner's
 * id, customerCode and registrationDate.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner) {
        AUDIT.info("Created owner id={} customerCode={} registrationDate={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate());
    }
}
