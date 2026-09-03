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

    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    private static final AtomicLong SEQ = new AtomicLong();

    @PostPersist
    void onCreate(Owner owner) {
        AUDIT.info("owner created id={} memberId={} registrationDate={} membershipLevel={}",
            owner.getId(), owner.getMemberId(), owner.getRegistrationDate(),
            org.springframework.samples.petclinic.rest.controller.v1.MembershipLevel.of(owner));
        AUDIT.info(String.format(
            "{\"schemaVersion\":2,\"seq\":%d,\"ownerId\":%d,\"memberId\":\"%s\",\"membershipLevel\":%d,"
                + "\"ownerSegment\":\"%s\",\"event\":\"OWNER_CREATED\"}",
            SEQ.incrementAndGet(), owner.getId(), owner.getMemberId(),
            org.springframework.samples.petclinic.rest.controller.v1.MembershipLevel.of(owner),
            org.springframework.samples.petclinic.rest.controller.v1.OwnerSegment.of(owner)));
        NOTIFY.info("welcome owner id={} memberId={}", owner.getId(), owner.getMemberId());
    }
}
