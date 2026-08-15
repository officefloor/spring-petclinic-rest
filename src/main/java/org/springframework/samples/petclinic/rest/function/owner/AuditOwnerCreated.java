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
 *   <li>the human-readable audit line carrying the owner's id, customerCode, registrationDate,
 *       membershipLevel and membershipNumber; and</li>
 *   <li>an immutable structured {@link OwnerCreatedEvent} as one JSON line
 *       ({@code {seq, ownerId, customerCode, membershipLevel, event:'OWNER_CREATED'}}), where
 *       {@code seq} increases monotonically across creates and {@code customerCode} carries the
 *       owner's current {@linkplain #primaryIdentifier(Owner) primary identifier}.</li>
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

    public void service(@Val Owner owner, OwnerMapper ownerMapper) {
        AUDIT.info(
                "Owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                OwnerMapper.membershipLevel(owner), ownerMapper.membershipNumber(owner));

        OwnerCreatedEvent event = new OwnerCreatedEvent(SEQ.incrementAndGet(), owner.getId(),
                primaryIdentifier(owner), OwnerMapper.membershipLevel(owner));
        AUDIT.info(JSON.writeValueAsString(event));
    }

    /**
     * The owner's current primary identifier carried by the structured event. Today this is the
     * {@code customerCode}; when the customerCode is later unified into the memberId, change this
     * single accessor so every {@code OWNER_CREATED} event carries the memberId instead.
     */
    private static String primaryIdentifier(Owner owner) {
        return owner.getCustomerCode();
    }
}
