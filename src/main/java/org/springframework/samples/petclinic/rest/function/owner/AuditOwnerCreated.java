package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits an audit line on the dedicated {@code AUDIT} logger recording the newly
 * created owner's id, customerCode, registrationDate, membershipLevel and
 * membershipNumber. Runs after the owner has been saved so its id is available.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner, OwnerMapper mapper) {
        AUDIT.info(
                "Created owner id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                OwnerMapper.membershipLevel(owner), mapper.membershipNumber(owner));
    }
}
