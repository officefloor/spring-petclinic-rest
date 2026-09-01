package org.springframework.samples.petclinic.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.PostPersist;

/**
 * Emits an audit trail entry to the dedicated {@code AUDIT} logger whenever a new
 * {@link Owner} is created, capturing its id, customerCode and registrationDate.
 */
public class OwnerAuditListener {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    @PostPersist
    public void onCreate(Owner owner) {
        AUDIT.info("owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
            MembershipLevels.levelOf(owner), membershipNumber(owner));
        OwnerCreatedEvent.emit(owner);
    }

    private static String membershipNumber(Owner owner) {
        return owner.getCustomerCode() + "-M"
            + String.format("%02d", owner.getRegistrationDate().getYear() % 100);
    }
}
