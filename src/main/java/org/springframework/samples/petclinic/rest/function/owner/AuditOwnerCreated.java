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
 * Emits audit records for a freshly created owner via the dedicated {@code AUDIT}
 * logger. Runs after {@link SaveOwner}, so the owner id has been assigned.
 *
 * <p>Two things are emitted:
 * <ol>
 * <li>a human-readable audit line carrying the id, the memberId, the registrationDate
 * and the membershipLevel; and</li>
 * <li>an immutable structured {@link OwnerCreatedEvent}, serialised to JSON with the
 * shared Jackson {@link ObjectMapper}, carrying a monotonically increasing
 * {@code seq}, the {@code ownerId}, the owner's primary identifier (its memberId) and
 * the {@code membershipLevel}.</li>
 * </ol>
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Monotonically increasing sequence across all creates (process-wide). */
    private static final AtomicLong SEQ = new AtomicLong();

    public void service(@Val Owner owner, ObjectMapper objectMapper) {
        AUDIT.info("Owner created: id={} memberId={} registrationDate={} membershipLevel={}",
                owner.getId(), owner.getMemberId(), owner.getRegistrationDate(),
                owner.getMembershipLevel());

        // Immutable structured event carrying the owner's memberId.
        OwnerCreatedEvent event = new OwnerCreatedEvent(SEQ.incrementAndGet(), owner.getId(),
                owner.getMemberId(), owner.getMembershipLevel());
        AUDIT.info(objectMapper.writeValueAsString(toFields(event)));
    }

    /** Ordered field map matching the event's JSON shape. */
    private static Map<String, Object> toFields(OwnerCreatedEvent event) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("seq", event.seq());
        fields.put("ownerId", event.ownerId());
        fields.put("memberId", event.memberId());
        fields.put("membershipLevel", event.membershipLevel());
        fields.put("event", event.event());
        return fields;
    }
}
