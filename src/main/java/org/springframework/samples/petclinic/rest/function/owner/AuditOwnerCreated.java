package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Step of {@code POST /api/owners} that records a successful create on the dedicated {@code AUDIT}
 * logger. Runs after {@link SaveOwner} so the owner has its persisted id, and emits a single line
 * carrying the owner id, the assigned {@code customerCode}, the effective {@code registrationDate},
 * the assigned {@code membershipLevel} and the derived {@code membershipNumber}.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner) {
        String membershipNumber = owner.getCustomerCode() + "-M"
                + String.format("%02d", owner.getRegistrationDate().getYear() % 100);
        AUDIT.info("owner created id={} customerCode={} registrationDate={} membershipLevel={} "
                        + "membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                owner.getMembershipLevel(), membershipNumber);
    }
}
