package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import tools.jackson.databind.ObjectMapper;

/**
 * Emits audit side effects for each successful owner create, keeping them out of the responder.
 * Runs after the owner is saved (so the id is populated) and before the responder.
 *
 * <p>Two things are emitted on the dedicated {@code AUDIT} channel:
 * <ol>
 * <li>a human-readable audit line carrying the owner id, the customerCode, the registrationDate,
 * the membershipLevel and the membershipNumber; and</li>
 * <li>an immutable structured {@link OwnerCreatedEvent} rendered as single-line JSON, carrying a
 * monotonically increasing {@code seq}, the owner id, the owner's current primary identifier and
 * its membershipLevel.</li>
 * </ol>
 */
public class AuditOwnerCreated {

    /** Dedicated audit channel; one line per successful create. */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Source of the structured event's monotonically increasing {@code seq}, across all creates. */
    private static final AtomicLong SEQUENCE = new AtomicLong();

    public void service(@Val Owner owner, ObjectMapper objectMapper) {
        AUDIT.info("Owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                owner.getMembershipLevel(), owner.getMembershipNumber());

        // customerCode is the owner's current primary identifier (see OwnerCreatedEvent).
        OwnerCreatedEvent event = new OwnerCreatedEvent(SEQUENCE.incrementAndGet(), owner.getId(),
                owner.getCustomerCode(), owner.getMembershipLevel());
        AUDIT.info(objectMapper.writeValueAsString(event));
    }
}
