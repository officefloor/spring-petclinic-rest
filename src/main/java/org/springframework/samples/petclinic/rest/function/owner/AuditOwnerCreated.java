package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;

import tools.jackson.databind.json.JsonMapper;

/**
 * Emits audit side-effects to the dedicated {@code AUDIT} logger on successful create:
 * <ol>
 * <li>a human-readable audit line carrying the newly assigned owner id, the memberId, the
 * registrationDate and the membershipLevel; and</li>
 * <li>an immutable structured {@link OwnerCreatedEvent} serialized as JSON, carrying a
 * monotonically increasing {@code seq}, the owner id, the current primary identifier and the
 * membershipLevel.</li>
 * </ol>
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Monotonically increasing sequence, shared across every create. */
    private static final AtomicLong SEQ = new AtomicLong();

    private static final JsonMapper JSON = JsonMapper.builder().build();

    public void service(@Val Owner owner) {
        AUDIT.info("Owner created id={} memberId={} registrationDate={} membershipLevel={}",
                owner.getId(), owner.getMemberId(), owner.getRegistrationDate(),
                owner.getMembershipLevel());

        OwnerCreatedEvent event = new OwnerCreatedEvent(SEQ.incrementAndGet(), owner.getId(),
                primaryIdentifier(owner), owner.getMembershipLevel(), OwnerCreatedEvent.OWNER_CREATED);
        AUDIT.info(JSON.writeValueAsString(event));
    }

    /** The owner's primary identifier: the unified {@code memberId}. */
    private static String primaryIdentifier(Owner owner) {
        return owner.getMemberId();
    }
}
