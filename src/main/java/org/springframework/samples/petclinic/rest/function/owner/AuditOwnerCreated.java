package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits an audit line on successful owner creation via the dedicated {@code AUDIT}
 * logger, carrying the owner id, the {@code customerCode}, the
 * {@code registrationDate}, the {@code membershipLevel} and the
 * {@code membershipNumber}.
 *
 * <p>Runs after {@code SaveOwner} so the owner id has been assigned.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner, OwnerMapper ownerMapper) {
        AUDIT.info("Owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                ownerMapper.toMembershipLevel(owner), ownerMapper.toMembershipNumber(owner));
    }
}
