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
 * <li>a human-readable audit line carrying the owner id, the {@code customerCode},
 * the {@code registrationDate}, the {@code membershipLevel} and the
 * {@code membershipNumber}; and
 * <li>an immutable structured event, a JSON object
 * {@code {seq, ownerId, customerCode, membershipLevel, event:'OWNER_CREATED'}},
 * where {@code seq} is a monotonically increasing integer across all creates.
 * </ol>
 *
 * <p>The event carries the owner's <em>current primary identifier</em>. Today that
 * identifier is the {@code customerCode}; when it is later unified into the
 * {@code memberId}, only {@link #primaryIdentifier(Owner)} changes and the event
 * follows without touching the emission below.
 *
 * <p>Runs after {@code SaveOwner} so the owner id has been assigned.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Monotonically increasing sequence shared across every owner-create event. */
    private static final AtomicLong SEQUENCE = new AtomicLong();

    private static final ObjectMapper JSON = new ObjectMapper();

    public void service(@Val Owner owner, OwnerMapper ownerMapper) {
        AUDIT.info("Owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                ownerMapper.toMembershipLevel(owner), ownerMapper.toMembershipNumber(owner));

        Map<String, Object> event = new LinkedHashMap<>();
        event.put("seq", SEQUENCE.incrementAndGet());
        event.put("ownerId", owner.getId());
        event.put("customerCode", primaryIdentifier(owner));
        event.put("membershipLevel", ownerMapper.toMembershipLevel(owner));
        event.put("event", "OWNER_CREATED");
        AUDIT.info(JSON.writeValueAsString(event));
    }

    /**
     * The owner's current primary identifier. Today this is the {@code customerCode};
     * change this single method when the identifier is unified into the {@code memberId}.
     */
    private static String primaryIdentifier(Owner owner) {
        return owner.getCustomerCode();
    }
}
