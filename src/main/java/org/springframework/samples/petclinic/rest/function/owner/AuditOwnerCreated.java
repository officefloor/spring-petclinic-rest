package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicInteger;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;

import tools.jackson.databind.json.JsonMapper;

/**
 * Emits, on the dedicated {@code AUDIT} logger, the audit side-effects for a successfully created owner:
 * <ul>
 * <li>a human-readable audit line carrying the owner id (assigned by the preceding save), the
 * memberId, the registrationDate and the membershipLevel; and</li>
 * <li>an immutable structured {@link OwnerCreatedEvent} serialised to JSON — {@code
 * {seq, ownerId, memberId, membershipLevel, event:'OWNER_CREATED'}} — where {@code seq} is a
 * monotonically increasing integer across creates.</li>
 * </ul>
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private static final JsonMapper JSON = JsonMapper.builder().build();

    /** Monotonically increasing sequence, shared across all creates. */
    private static final AtomicInteger SEQUENCE = new AtomicInteger();

    public void service(@Val Owner owner) {
        AUDIT.info("Owner created id={} memberId={} registrationDate={} membershipLevel={}",
                owner.getId(), owner.getMemberId(), owner.getRegistrationDate(),
                owner.getMembershipLevel());
        OwnerCreatedEvent event = OwnerCreatedEvent.from(SEQUENCE.incrementAndGet(), owner);
        AUDIT.info(JSON.writeValueAsString(event));
    }
}
