package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits an audit line for a newly created owner via the dedicated {@code AUDIT}
 * logger. Runs after {@link SaveOwner} so the persisted owner id is available.
 * The line carries the owner id, the assigned {@code customerCode}, the
 * effective {@code registrationDate}, the numeric {@code membershipLevel} and
 * the {@code membershipNumber}.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner, OwnerMapper ownerMapper) {
        AUDIT.info("owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                ownerMapper.membershipLevel(owner), ownerMapper.membershipNumber(owner));
    }
}
