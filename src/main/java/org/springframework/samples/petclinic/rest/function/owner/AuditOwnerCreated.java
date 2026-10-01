package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.samples.petclinic.model.MembershipLevel;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.OwnerPrimaryIdentifier;

/**
 * Runs after {@link SaveOwner} on {@code POST /api/owners}, once the owner has been persisted and
 * assigned an id. Emits the human-readable audit line via the dedicated {@code AUDIT} logger, and then
 * an immutable structured {@link OwnerCreatedEvent} serialized to JSON on the same logger. The event
 * carries a process-wide monotonically increasing {@code seq}, the owner id, the owner's current
 * {@link OwnerPrimaryIdentifier primary identifier} (the unified, version-2 member id) and the audit
 * {@code schemaVersion} of 2.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    /** Monotonically increasing sequence across all owner creates in this process. */
    private static final AtomicLong SEQ = new AtomicLong();

    public void service(@Val Owner owner) {
        AUDIT.info(
                "Created owner id={} memberId={} registrationDate={} membershipLevel={}",
                owner.getId(), owner.getMemberId(), owner.getRegistrationDate(),
                MembershipLevel.of(owner));

        OwnerCreatedEvent event = new OwnerCreatedEvent(SEQ.incrementAndGet(), owner.getId(),
                OwnerPrimaryIdentifier.of(owner), MembershipLevel.of(owner));
        AUDIT.info(MAPPER.writeValueAsString(event));
    }
}
