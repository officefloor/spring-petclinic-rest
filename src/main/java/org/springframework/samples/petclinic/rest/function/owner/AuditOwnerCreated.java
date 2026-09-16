package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits the audit trail for a successful owner create. Runs after {@link SaveOwner} has
 * assigned the owner's id, logging the id, customer code, registration date and membership
 * level to the dedicated {@code AUDIT} logger.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner, OwnerMapper ownerMapper) {
        AUDIT.info("Owner created id={} customerCode={} registrationDate={} membershipLevel={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                ownerMapper.membershipLevel(owner));
    }
}
