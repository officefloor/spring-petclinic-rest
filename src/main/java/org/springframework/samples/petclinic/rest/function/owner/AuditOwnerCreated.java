package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;

import tools.jackson.databind.json.JsonMapper;

/**
 * Emits, on the dedicated {@code AUDIT} logger, the record of a newly created owner. Runs after the
 * owner has been saved so its id is available. Two things are emitted:
 *
 * <ol>
 * <li>a human-readable audit line (id, memberId, registrationDate, membershipLevel); and</li>
 * <li>an immutable structured {@link OwnerCreatedEvent} serialised to JSON, so audit consumers have a
 * machine-parseable, ordered event to react to.</li>
 * </ol>
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private static final JsonMapper JSON = JsonMapper.builder().build();

    /** Monotonically increasing sequence stamped onto each event, shared across all creates. */
    private static final AtomicLong SEQUENCE = new AtomicLong();

    public void service(@Val Owner owner) {
        int membershipLevel = OwnerMembership.level(owner);

        AUDIT.info(
                "Created owner id={} memberId={} registrationDate={} membershipLevel={}",
                owner.getId(), owner.getMemberId(), owner.getRegistrationDate(), membershipLevel);

        OwnerCreatedEvent event = new OwnerCreatedEvent(SEQUENCE.incrementAndGet(), owner.getId(),
                primaryIdentifier(owner), membershipLevel);
        AUDIT.info(JSON.writeValueAsString(event));
    }

    /**
     * The owner's primary identifier carried by the {@link OwnerCreatedEvent} — the unified
     * {@code memberId}. This single method is the one place the event's identity source is defined.
     */
    private static String primaryIdentifier(Owner owner) {
        return owner.getMemberId();
    }
}
