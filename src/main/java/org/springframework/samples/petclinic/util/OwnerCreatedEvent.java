package org.springframework.samples.petclinic.util;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.LoggerFactory;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits the immutable {@code OWNER_CREATED} structured event to the {@code AUDIT} logger,
 * carrying a monotonically increasing {@code seq}, the owner id, the owner's current
 * primary identifier and the membership level.
 *
 * <p>The primary identifier is resolved via {@link #primaryIdentifier(Owner)}: today the
 * owner's {@code customerCode}, and whatever replaces it later (e.g. the memberId).
 */
public final class OwnerCreatedEvent {

    private static final AtomicLong SEQ = new AtomicLong();

    private OwnerCreatedEvent() {
    }

    /** The owner's current primary identifier. */
    public static String primaryIdentifier(Owner owner) {
        return owner.getCustomerCode();
    }

    /** Emits {@code {seq, ownerId, customerCode, membershipLevel, event:'OWNER_CREATED'}}. */
    public static void emit(Owner owner, Object membershipLevel) {
        LoggerFactory.getLogger("AUDIT").info(
            "{\"seq\":{},\"ownerId\":{},\"customerCode\":\"{}\",\"membershipLevel\":\"{}\",\"event\":\"OWNER_CREATED\"}",
            SEQ.incrementAndGet(), owner.getId(), primaryIdentifier(owner), membershipLevel);
    }
}
