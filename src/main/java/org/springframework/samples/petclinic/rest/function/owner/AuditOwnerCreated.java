package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Runs in the create-owner pipeline after {@link SaveOwner}, once the owner has been persisted and
 * its id assigned. Emits a single audit line on the dedicated {@code AUDIT} logger recording the new
 * owner's id, {@code customerCode} and {@code registrationDate} (ISO 'YYYY-MM-DD'). Read-only: it
 * inspects the stored owner without mutating it, then hands off to the responder.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner) {
        AUDIT.info("Owner created: id={} customerCode={} registrationDate={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate());
    }
}
