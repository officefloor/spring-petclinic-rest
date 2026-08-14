package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.mapper.MembershipLevel;
import org.springframework.samples.petclinic.model.Owner;

public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner) {
        AUDIT.info("Owner created id={} customerCode={} registrationDate={} membershipLevel={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                MembershipLevel.of(owner));
    }
}
