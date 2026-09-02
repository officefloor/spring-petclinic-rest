package org.springframework.samples.petclinic.service;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Business rule: on successful creation an audit line is emitted via the dedicated {@code AUDIT}
 * logger carrying the new owner's id, its {@code customerCode}, its {@code registrationDate}, its
 * {@code membershipLevel} and its {@code membershipNumber}.
 * Kept as a small, self-contained unit so the rule can be applied from the create flow after the
 * owner has been saved without adding complexity to the controller or service.
 */
public final class OwnerAuditPolicy {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Monotonically increasing sequence across all creates, for the structured event. */
    private static final AtomicLong SEQ = new AtomicLong();

    private OwnerAuditPolicy() {
    }

    /**
     * The owner's current primary identifier carried by the structured event: the
     * {@code customerCode} today, and whatever replaces it later (e.g. the memberId).
     */
    private static String primaryIdentifier(Owner owner) {
        return owner.getCustomerCode();
    }

    /**
     * Emit the immutable structured {@code OWNER_CREATED} event carrying the create sequence,
     * the owner id, its current primary identifier and its membership level.
     */
    static void auditCreatedEvent(Owner owner) {
        AUDIT.info("{\"seq\":{},\"ownerId\":{},\"customerCode\":\"{}\",\"membershipLevel\":{},\"event\":\"OWNER_CREATED\"}",
            SEQ.incrementAndGet(), owner.getId(), primaryIdentifier(owner),
            OwnerMembershipLevelPolicy.membershipLevel(owner));
    }

    /** Emit the create audit line for a freshly saved owner. */
    public static void auditCreate(Owner owner) {
        AUDIT.info("owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
            OwnerMembershipLevelPolicy.membershipLevel(owner),
            OwnerMembershipPolicy.membershipNumber(owner));
        auditCreatedEvent(owner);
    }
}
