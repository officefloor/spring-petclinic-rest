package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;

import tools.jackson.databind.json.JsonMapper;

/**
 * Emits the create audit trail. Runs after the owner is saved (so its id is
 * assigned) and before the response is sent.
 *
 * <p>Two things are written to the dedicated {@code AUDIT} logger:
 * <ol>
 * <li>the human-readable audit line carrying the new owner's id, member id,
 *     registration date and derived membership level; and</li>
 * <li>an immutable, machine-readable {@code OWNER_CREATED} event as a single JSON
 *     object {@code {schemaVersion, seq, ownerId, memberId, ownerSegment, membershipLevel, event}}.</li>
 * </ol>
 *
 * <p>{@code seq} is a monotonically increasing integer across creates. The event is at schema
 * version 2 ({@code schemaVersion}) and carries the owner's primary identifier — the unified,
 * version-2 {@link MemberId member id} — and the {@link OwnerSegment owner segment} recomputed from
 * that version-2 identity.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Monotonically increasing sequence across all creates (process-wide). */
    private static final AtomicLong SEQ = new AtomicLong();

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    public void service(@Val Owner owner) {
        AUDIT.info("Owner created id={} memberId={} registrationDate={} membershipLevel={}",
                owner.getId(), owner.getMemberId(), owner.getRegistrationDate(),
                MembershipLevel.of(owner));

        OwnerCreatedEvent event = new OwnerCreatedEvent(SEQ.incrementAndGet(), owner.getId(),
                owner.getMemberId(), OwnerSegment.of(owner), membershipLevel(owner));
        AUDIT.info(MAPPER.writeValueAsString(event));
    }

    /** Stored membership level if the pipeline has stamped one, else the derived level. */
    private static int membershipLevel(Owner owner) {
        return owner.getMembershipLevel() != null ? owner.getMembershipLevel() : MembershipLevel.of(owner);
    }

    /** The audit event schema version. Version 2 adds {@code schemaVersion} and {@code ownerSegment}. */
    private static final int SCHEMA_VERSION = 2;

    /**
     * Immutable structured create event. Field order is the serialized JSON key order:
     * {@code {schemaVersion, seq, ownerId, memberId, ownerSegment, membershipLevel, event}}.
     */
    public record OwnerCreatedEvent(int schemaVersion, long seq, Integer ownerId, String memberId,
            String ownerSegment, int membershipLevel, String event) {

        public OwnerCreatedEvent(long seq, Integer ownerId, String memberId, String ownerSegment,
                int membershipLevel) {
            this(SCHEMA_VERSION, seq, ownerId, memberId, ownerSegment, membershipLevel, "OWNER_CREATED");
        }
    }
}
