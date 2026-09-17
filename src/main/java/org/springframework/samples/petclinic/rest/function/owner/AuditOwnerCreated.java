package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits an audit trail entry once an owner has been persisted. Runs after {@link SaveOwner}, so the
 * owner carries its generated id alongside the derived customerCode and registrationDate. Writes to
 * the dedicated {@code AUDIT} logger rather than the class logger, keeping audit events separable
 * from ordinary application logging.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner) {
        AUDIT.info("Owner created: id={} customerCode={} registrationDate={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate());
    }
}
