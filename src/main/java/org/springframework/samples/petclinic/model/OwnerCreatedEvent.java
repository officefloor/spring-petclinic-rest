package org.springframework.samples.petclinic.model;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.samples.petclinic.mapper.OwnerSegments;

/**
 * Emits the immutable structured {@code OWNER_CREATED} event to the dedicated
 * {@code AUDIT} logger, one per created owner, as a schema-version-2 JSON object
 * {@code {seq, ownerId, memberId, membershipLevel, ownerSegment, schemaVersion, event}}.
 * {@code seq} is a monotonically increasing integer across creates; {@code memberId} and
 * {@code ownerSegment} are recomputed from the owner's version-2 identity.
 *
 * <p>The event carries the owner's primary identifier — the memberId — via
 * {@link #primaryId(Owner)}.
 */
public final class OwnerCreatedEvent {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private static final AtomicLong SEQ = new AtomicLong();

    private OwnerCreatedEvent() {
    }

    /** Emit the structured create event for {@code owner}. */
    public static void emit(Owner owner) {
        AUDIT.info(
            "{\"seq\":{},\"ownerId\":{},\"memberId\":\"{}\",\"membershipLevel\":{},"
                + "\"ownerSegment\":\"{}\",\"schemaVersion\":2,\"event\":\"OWNER_CREATED\"}",
            SEQ.incrementAndGet(), owner.getId(), primaryId(owner), MembershipLevels.levelOf(owner),
            OwnerSegments.of(owner));
    }

    /** The owner's current primary identifier carried by the event. */
    private static String primaryId(Owner owner) {
        return owner.getCustomerCode();
    }
}
