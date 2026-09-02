package org.springframework.samples.petclinic.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Business rule: on successful creation an audit line is emitted via the dedicated {@code AUDIT}
 * logger carrying the new owner's id, its {@code customerCode} and its {@code registrationDate}.
 * Kept as a small, self-contained unit so the rule can be applied from the create flow after the
 * owner has been saved without adding complexity to the controller or service.
 */
public final class OwnerAuditPolicy {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private OwnerAuditPolicy() {
    }

    /** Emit the create audit line for a freshly saved owner. */
    public static void auditCreate(Owner owner) {
        AUDIT.info("owner created id={} customerCode={} registrationDate={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate());
    }
}
