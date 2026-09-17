package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Step of {@code POST /api/owners}: emits the create audit trail. Runs after
 * {@link SaveOwner} so the owner carries its persisted id, logging one line to the
 * dedicated {@code AUDIT} logger with the owner id, {@code customerCode},
 * {@code registrationDate}, {@code membershipPoints}, {@code membershipLevel} and
 * {@code membershipNumber}.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner, OwnerMapper ownerMapper) {
        AUDIT.info("Owner created id={} customerCode={} registrationDate={} membershipPoints={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                owner.getMembershipPoints(), owner.getMembershipLevel(),
                ownerMapper.membershipNumber(owner));
    }
}
