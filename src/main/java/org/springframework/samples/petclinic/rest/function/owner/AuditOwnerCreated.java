package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits an audit line for a newly created owner via the dedicated {@code AUDIT} logger,
 * carrying the owner's id, customerCode, registrationDate and membershipLevel.
 *
 * <p>Runs after {@link SaveOwner} so the generated id is present, within the same write
 * transaction as the insert.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner) {
        AUDIT.info("Owner created id={} customerCode={} registrationDate={} membershipLevel={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                OwnerMapper.membershipLevel(owner));
    }
}
