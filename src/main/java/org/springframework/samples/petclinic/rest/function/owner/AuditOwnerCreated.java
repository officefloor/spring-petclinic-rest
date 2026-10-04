package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits an audit trail line to the dedicated {@code AUDIT} logger once a new owner
 * has been persisted, recording the assigned owner id, the region-and-hash
 * {@code customerCode} and {@code registrationDate}.
 *
 * <p>Runs after {@link SaveOwner}, so the generated id is available.
 */
public class AuditOwnerCreated {

    private static final Logger logger = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner) {
        logger.info("Owner created: id={} customerCode={} registrationDate={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate());
    }
}
