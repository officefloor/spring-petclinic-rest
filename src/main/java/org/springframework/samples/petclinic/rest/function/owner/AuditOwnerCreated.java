package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;

public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner) {
        int membershipLevel = Math.min(3, 1
                + (owner.getEmail() != null && !owner.getEmail().isBlank() ? 1 : 0)
                + (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0 ? 1 : 0));
        AUDIT.info("Owner created id={} customerCode={} registrationDate={} membershipLevel={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(), membershipLevel);
    }
}
