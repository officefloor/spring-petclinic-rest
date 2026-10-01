package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;

import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Emits audit records on the dedicated {@code AUDIT} logger once a new owner has been persisted.
 *
 * <p>Two records are emitted:
 * <ul>
 * <li>a human-readable audit line carrying the owner id, the {@code memberId}, the
 * {@code registrationDate} and the {@code membershipLevel}; and</li>
 * <li>an immutable structured JSON event (schema version 2)
 * {@code {schemaVersion:2, seq, ownerId, memberId, membershipLevel, ownerSegment,
 * event:"OWNER_CREATED"}}, where {@code seq} is a monotonically increasing integer across every
 * create in this process, {@code memberId} is the owner's version-2 primary identifier and
 * {@code ownerSegment} is recomputed from the version-2 identity.</li>
 * </ul>
 *
 * <p>The event's identifier is read through {@link OwnerPrimaryIdentifier}, so it always carries the
 * owner's current primary identifier &mdash; the unified {@code memberId}.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private static final JsonMapper JSON = JsonMapper.shared();

    /** Monotonically increasing sequence across every owner created in this process. */
    private static final AtomicLong SEQ = new AtomicLong();

    public void service(@Val Owner owner) {
        AUDIT.info("Owner created id={} memberId={} registrationDate={} membershipLevel={}",
                owner.getId(), owner.getMemberId(), owner.getRegistrationDate(),
                owner.getMembershipLevel());

        ObjectNode event = JSON.createObjectNode();
        event.put("schemaVersion", 2);
        event.put("seq", SEQ.incrementAndGet());
        event.put("ownerId", owner.getId());
        event.put("memberId", OwnerPrimaryIdentifier.of(owner));
        event.put("membershipLevel", owner.getMembershipLevel());
        event.put("ownerSegment", OwnerSegment.segment(owner).getValue());
        event.put("event", "OWNER_CREATED");
        AUDIT.info(JSON.writeValueAsString(event));
    }
}
