package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Step of {@code POST /api/owners} that records a successful create on the dedicated {@code AUDIT}
 * logger. Runs after {@link SaveOwner} so the owner has its persisted id, and emits two things:
 * <ul>
 * <li>a human-readable audit line carrying the owner id, the assigned {@code customerCode}, the
 * effective {@code registrationDate}, the assigned {@code membershipLevel} and the derived
 * {@code membershipNumber}; and</li>
 * <li>an immutable structured {@link OwnerCreatedEvent} rendered as JSON, stamped with a
 * process-wide {@code seq} that increases monotonically across every create.</li>
 * </ul>
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Monotonically increasing sequence stamped onto each structured event, across all creates. */
    private static final AtomicLong SEQUENCE = new AtomicLong();

    public void service(@Val Owner owner) {
        String membershipNumber = owner.getCustomerCode() + "-M"
                + FiscalYear.twoDigit(owner.getRegistrationDate());
        AUDIT.info("owner created id={} customerCode={} registrationDate={} membershipLevel={} "
                        + "membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                owner.getMembershipLevel(), membershipNumber);

        OwnerCreatedEvent event = OwnerCreatedEvent.forOwner(SEQUENCE.incrementAndGet(), owner);
        AUDIT.info(event.toJson());
    }
}
