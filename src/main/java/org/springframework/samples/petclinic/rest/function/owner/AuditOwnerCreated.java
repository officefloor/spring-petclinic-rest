package org.springframework.samples.petclinic.rest.function.owner;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import tools.jackson.databind.ObjectMapper;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits audit output on successful owner creation via the dedicated {@code AUDIT}
 * logger.
 *
 * <p>Two things are emitted per create:
 * <ol>
 * <li>a human-readable audit line carrying the owner id, the {@code memberId},
 * the {@code registrationDate} and the {@code membershipLevel}; and
 * <li>an immutable structured event, a version-2 JSON object
 * {@code {schemaVersion:2, seq, ownerId, memberId, membershipLevel, event:'OWNER_CREATED'}},
 * where {@code schemaVersion} is the event schema version (2) and {@code seq} is a
 * monotonically increasing integer across all creates.
 * </ol>
 *
 * <p>The event carries the owner's <em>primary identifier</em>, the unified
 * {@code memberId}, read through {@link #primaryIdentifier(Owner)}.
 *
 * <p>Runs after {@code SaveOwner} so the owner id has been assigned.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** The audit event schema version. Version 2 adds this {@code schemaVersion} field itself
     *  and carries the version-2 {@code memberId}. */
    private static final int AUDIT_SCHEMA_VERSION = 2;

    /** Monotonically increasing sequence shared across every owner-create event. */
    private static final AtomicLong SEQUENCE = new AtomicLong();

    private static final ObjectMapper JSON = new ObjectMapper();

    public void service(@Val Owner owner, OwnerMapper ownerMapper) {
        AUDIT.info("Owner created id={} memberId={} registrationDate={} membershipLevel={}",
                owner.getId(), owner.getMemberId(), owner.getRegistrationDate(),
                ownerMapper.toMembershipLevel(owner));

        Map<String, Object> event = new LinkedHashMap<>();
        event.put("schemaVersion", AUDIT_SCHEMA_VERSION);
        event.put("seq", SEQUENCE.incrementAndGet());
        event.put("ownerId", owner.getId());
        event.put("memberId", primaryIdentifier(owner));
        event.put("membershipLevel", ownerMapper.toMembershipLevel(owner));
        event.put("event", "OWNER_CREATED");
        AUDIT.info(JSON.writeValueAsString(event));
    }

    /**
     * The owner's primary identifier, the unified {@code memberId}.
     */
    private static String primaryIdentifier(Owner owner) {
        return owner.getMemberId();
    }
}
