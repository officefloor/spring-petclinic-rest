package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

import tools.jackson.databind.ObjectMapper;

/**
 * Emits, on successful create, two things via the dedicated {@code AUDIT} logger:
 * <ol>
 * <li>the human-readable audit line, carrying the newly assigned owner id together
 * with its {@code memberId}, {@code registrationDate} and {@code membershipLevel}; and</li>
 * <li>an immutable structured event —
 * {@code {seq, ownerId, memberId, membershipLevel, event:'OWNER_CREATED'}} — where
 * {@code seq} is a monotonically increasing integer across creates.</li>
 * </ol>
 * Runs after the owner has been saved so the id is set.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private static final ObjectMapper JSON = new ObjectMapper();

    /** Monotonically increasing sequence number, shared across all creates in the JVM. */
    private static final AtomicLong SEQ = new AtomicLong();

    public void service(@Val Owner owner) {
        AUDIT.info("owner created id={} memberId={} registrationDate={} membershipLevel={}",
                owner.getId(), owner.getMemberId(), owner.getRegistrationDate(),
                owner.getMembershipLevel());

        OwnerCreatedEvent event = new OwnerCreatedEvent(SEQ.incrementAndGet(), owner.getId(),
                primaryIdentifier(owner), owner.getMembershipLevel());
        AUDIT.info(JSON.writeValueAsString(event));
    }

    /**
     * The owner's primary identifier — the unified {@code memberId}.
     */
    private static String primaryIdentifier(Owner owner) {
        return owner.getMemberId();
    }

    /**
     * Immutable structured audit event for a created owner. Field order matches the
     * documented shape {@code {seq, ownerId, memberId, membershipLevel, event}}; the
     * {@code event} marker is fixed at {@code OWNER_CREATED}.
     */
    public record OwnerCreatedEvent(long seq, Integer ownerId, String memberId, Integer membershipLevel,
            String event) {
        public OwnerCreatedEvent(long seq, Integer ownerId, String memberId, Integer membershipLevel) {
            this(seq, ownerId, memberId, membershipLevel, "OWNER_CREATED");
        }
    }
}
