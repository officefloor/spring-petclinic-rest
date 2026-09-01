package org.springframework.samples.petclinic.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.PostPersist;

/**
 * Emits an audit trail entry to the dedicated {@code AUDIT} logger whenever a new
 * {@link Owner} is created, capturing its id, memberId and registrationDate.
 */
public class OwnerAuditListener {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    @PostPersist
    public void onCreate(Owner owner) {
        AUDIT.info("owner created id={} memberId={} registrationDate={} membershipLevel={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
            MembershipLevels.levelOf(owner));
        NOTIFY.info("welcome owner id={} memberId={}", owner.getId(), owner.getCustomerCode());
        OwnerCreatedEvent.emit(owner);
    }
}
