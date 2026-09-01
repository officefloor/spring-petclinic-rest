package org.springframework.samples.petclinic.model;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Emits the immutable structured {@code OWNER_CREATED} event to the dedicated
 * {@code AUDIT} logger, one per created owner, as a JSON object
 * {@code {seq, ownerId, customerCode, membershipLevel, event}}. {@code seq} is a
 * monotonically increasing integer across creates.
 *
 * <p>The event carries the owner's current primary identifier via
 * {@link #primaryId(Owner)} — the customerCode today; when the customerCode is
 * unified into the memberId, only that method changes and the event follows.
 */
public final class OwnerCreatedEvent {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private static final AtomicLong SEQ = new AtomicLong();

    private OwnerCreatedEvent() {
    }

    /** Emit the structured create event for {@code owner}. */
    public static void emit(Owner owner) {
        AUDIT.info(
            "{\"seq\":{},\"ownerId\":{},\"customerCode\":\"{}\",\"membershipLevel\":{},\"event\":\"OWNER_CREATED\"}",
            SEQ.incrementAndGet(), owner.getId(), primaryId(owner), MembershipLevels.levelOf(owner));
    }

    /** The owner's current primary identifier carried by the event. */
    private static String primaryId(Owner owner) {
        return owner.getCustomerCode();
    }
}
