package org.springframework.samples.petclinic.rest.function.owner;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import tools.jackson.databind.ObjectMapper;

/**
 * Runs in the create-owner pipeline after {@link SaveOwner}, once the owner has been persisted and
 * its id assigned. Emits two things on the dedicated {@code AUDIT} logger:
 *
 * <ol>
 * <li>a human-readable audit line recording the new owner's id, {@code memberId},
 * {@code registrationDate} (ISO 'YYYY-MM-DD') and {@code membershipLevel}; and</li>
 * <li>an immutable structured event, a JSON object
 * {@code {seq, ownerId, memberId, membershipLevel, event:'OWNER_CREATED'}} where {@code seq} is a
 * monotonically increasing integer across every create. The event carries the owner's current
 * {@link Owner#getPrimaryIdentifier() primary identifier} — the memberId — so downstream consumers
 * always see the owner's live external handle without this step changing.</li>
 * </ol>
 *
 * Read-only: it inspects the stored owner without mutating it, then hands off to the responder.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Monotonically increasing sequence number across every create, process-wide. */
    private static final AtomicLong SEQUENCE = new AtomicLong();

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public void service(@Val Owner owner) {
        AUDIT.info(
                "Owner created: id={} memberId={} registrationDate={} membershipLevel={}",
                owner.getId(), owner.getMemberId(), owner.getRegistrationDate(),
                owner.getMembershipLevel());

        // Insertion-ordered so the serialized event reads {seq, ownerId, memberId, ...}.
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("seq", SEQUENCE.incrementAndGet());
        event.put("ownerId", owner.getId());
        event.put("memberId", owner.getPrimaryIdentifier());
        event.put("membershipLevel", owner.getMembershipLevel());
        event.put("event", "OWNER_CREATED");
        AUDIT.info(MAPPER.writeValueAsString(event));
    }
}
