package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;

import tools.jackson.databind.json.JsonMapper;

/**
 * Emits audit side-effects to the dedicated {@code AUDIT} logger on successful create:
 * <ol>
 * <li>a human-readable audit line carrying the newly assigned owner id, the memberId, the
 * registrationDate and the membershipLevel; and</li>
 * <li>an immutable structured {@link OwnerCreatedEvent} serialized as JSON, carrying a
 * monotonically increasing {@code seq}, the owner id, the current primary identifier and the
 * membershipLevel.</li>
 * </ol>
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Monotonically increasing sequence, shared across every create. */
    private static final AtomicLong SEQ = new AtomicLong();

    private static final JsonMapper JSON = JsonMapper.builder().build();

    public void service(@Val Owner owner) {
        AUDIT.info("Owner created id={} memberId={} registrationDate={} membershipLevel={}",
                owner.getId(), owner.getMemberId(), owner.getRegistrationDate(),
                owner.getMembershipLevel());

        OwnerCreatedEvent event = new OwnerCreatedEvent(OwnerCreatedEvent.SCHEMA_VERSION,
                SEQ.incrementAndGet(), owner.getId(), primaryIdentifier(owner),
                owner.getMembershipLevel(), ownerSegment(owner), OwnerCreatedEvent.OWNER_CREATED);
        AUDIT.info(JSON.writeValueAsString(event));
    }

    /** The owner's primary identifier: the unified version-2 {@code memberId}. */
    private static String primaryIdentifier(Owner owner) {
        return owner.getMemberId();
    }

    /**
     * The owner segment recomputed for the version-2 owner. Its region is the plain region code (no
     * {@code "V2"} tag), so the tag stays inside the identifiers and never leaks into the segment.
     */
    private static String ownerSegment(Owner owner) {
        String region = Locality.of(owner.getCity(), owner.getPostcode());
        return OwnerSegment.of(region, owner.getMembershipLevel());
    }
}
