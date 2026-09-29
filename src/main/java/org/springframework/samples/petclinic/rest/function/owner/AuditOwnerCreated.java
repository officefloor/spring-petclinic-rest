package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits an audit line via the dedicated {@code AUDIT} logger on successful create, carrying
 * the persisted owner's id, {@code customerCode} and {@code registrationDate}. Wired after
 * {@link SaveOwner} so the id is assigned, and after the steps that set the customer code and
 * business-day registration date, so every value logged matches what {@code GET /owners/{id}}
 * later returns.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner) {
        AUDIT.info("owner created id={} customerCode={} registrationDate={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate());
    }
}
