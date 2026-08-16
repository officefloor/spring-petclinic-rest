package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.ObjectMapper;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits an immutable, structured {@code OWNER_CREATED} event on the dedicated {@code AUDIT} logger,
 * in addition to the human-readable line from {@link AuditOwnerCreated}. Runs after {@link SaveOwner}
 * (so the owner has its persisted id) and after {@link AssignMemberId} / {@link AssignMembershipLevel}.
 *
 * <p>The event is a JSON object
 * {@code {seq, schemaVersion, ownerId, memberId, membershipLevel, event}} where {@code seq} is a
 * monotonically increasing integer across creates and {@code schemaVersion} is 2 (the version-2
 * schema). It carries the owner's <em>current primary identifier</em>, the version-2 unified
 * {@code memberId} (see {@link #primaryIdentifier}).
 */
public class AuditOwnerCreatedEvent {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Monotonic across all creates in this process; not reset by per-request transactions. */
    private static final AtomicLong SEQ = new AtomicLong();

    /** Immutable structured create event. Field order is the serialized JSON order. */
    public record OwnerCreatedEvent(long seq, int schemaVersion, Integer ownerId, String memberId,
            Integer membershipLevel, String event) {
    }

    public void service(@Val Owner owner, ObjectMapper mapper) {
        OwnerCreatedEvent event = new OwnerCreatedEvent(SEQ.incrementAndGet(),
                OwnerIdentityVersion.SCHEMA_VERSION, owner.getId(), primaryIdentifier(owner),
                owner.getMembershipLevel(), "OWNER_CREATED");
        AUDIT.info(mapper.writeValueAsString(event));
    }

    /** The owner's primary identifier: the unified {@code memberId}. */
    private static String primaryIdentifier(Owner owner) {
        return owner.getMemberId();
    }
}
