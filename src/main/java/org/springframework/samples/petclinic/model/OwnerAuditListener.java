package org.springframework.samples.petclinic.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.PostPersist;

/**
 * Emits an audit trail entry via the dedicated {@code AUDIT} logger whenever a new
 * owner is persisted, carrying the owner id, customer code and registration date.
 */
public class OwnerAuditListener {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    @PostPersist
    void onCreate(Owner owner) {
        AUDIT.info("owner created id={} customerCode={} registrationDate={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate());
    }
}
