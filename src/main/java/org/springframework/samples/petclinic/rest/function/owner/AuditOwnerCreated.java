package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import tools.jackson.databind.ObjectMapper;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits the audit for a newly created owner via the dedicated {@code AUDIT} logger. Two lines:
 *
 * <ol>
 *   <li>the human-readable audit line carrying the owner's id, memberId, registrationDate and
 *       membershipLevel; and</li>
 *   <li>an immutable structured {@link OwnerCreatedEvent} as one JSON line
 *       ({@code {seq, ownerId, memberId, membershipLevel, schemaVersion:2, event:'OWNER_CREATED'}}),
 *       where {@code seq} increases monotonically across creates, {@code schemaVersion} is 2 and
 *       {@code memberId} carries the owner's current {@linkplain #primaryIdentifier(Owner) primary
 *       identifier} (the version-2 {@code identity.memberId}).</li>
 * </ol>
 *
 * <p>Runs after {@link SaveOwner} so the generated id is present, within the same write
 * transaction as the insert.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Process-wide, monotonically increasing sequence stamped on each {@link OwnerCreatedEvent}. */
    private static final AtomicLong SEQ = new AtomicLong();

    private static final ObjectMapper JSON = new ObjectMapper();

    public void service(@Val Owner owner) {
        AUDIT.info(
                "Owner created id={} memberId={} registrationDate={} membershipLevel={}",
                owner.getId(), owner.getMemberId(), owner.getRegistrationDate(),
                OwnerMapper.membershipLevel(owner));

        OwnerCreatedEvent event = new OwnerCreatedEvent(SEQ.incrementAndGet(), owner.getId(),
                primaryIdentifier(owner), OwnerMapper.membershipLevel(owner));
        AUDIT.info(JSON.writeValueAsString(event));
    }

    /**
     * The owner's current primary identifier carried by the structured event: the unified
     * {@code memberId}.
     */
    private static String primaryIdentifier(Owner owner) {
        return owner.getMemberId();
    }
}
