package org.springframework.samples.petclinic.model;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.PostPersist;

/**
 * Emits an audit trail entry via the dedicated {@code AUDIT} logger whenever a new
 * owner is persisted, carrying the owner id, customer code and registration date.
 */
public class OwnerAuditListener {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private static final AtomicLong SEQ = new AtomicLong();

    @PostPersist
    void onCreate(Owner owner) {
        AUDIT.info("owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
            org.springframework.samples.petclinic.rest.controller.v1.MembershipLevel.of(owner),
            owner.getCustomerCode() + "-M" + String.format("%02d",
                org.springframework.samples.petclinic.rest.controller.v1.FiscalYear.of(owner.getRegistrationDate()) % 100));
        AUDIT.info(String.format(
            "{\"seq\":%d,\"ownerId\":%d,\"customerCode\":\"%s\",\"membershipLevel\":%d,\"event\":\"OWNER_CREATED\"}",
            SEQ.incrementAndGet(), owner.getId(), owner.getCustomerCode(),
            org.springframework.samples.petclinic.rest.controller.v1.MembershipLevel.of(owner)));
    }
}
