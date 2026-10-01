package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;

import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Emits audit records on the dedicated {@code AUDIT} logger once a new owner has been persisted.
 *
 * <p>Two records are emitted:
 * <ul>
 * <li>a human-readable audit line carrying the owner id, the {@code customerCode}, the
 * {@code registrationDate}, the {@code membershipLevel} and the {@code membershipNumber}; and</li>
 * <li>an immutable structured JSON event
 * {@code {seq, ownerId, customerCode, membershipLevel, event:"OWNER_CREATED"}}, where {@code seq}
 * is a monotonically increasing integer across every create in this process.</li>
 * </ul>
 *
 * <p>The event's identifier is read through {@link OwnerPrimaryIdentifier}, so it always carries the
 * owner's current primary identifier &mdash; the {@code customerCode} today, and whatever replaces
 * it later (the {@code memberId}).
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private static final JsonMapper JSON = JsonMapper.shared();

    /** Monotonically increasing sequence across every owner created in this process. */
    private static final AtomicLong SEQ = new AtomicLong();

    public void service(@Val Owner owner) {
        AUDIT.info("Owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                owner.getMembershipLevel(), owner.getMembershipNumber());

        ObjectNode event = JSON.createObjectNode();
        event.put("seq", SEQ.incrementAndGet());
        event.put("ownerId", owner.getId());
        event.put("customerCode", OwnerPrimaryIdentifier.of(owner));
        event.put("membershipLevel", owner.getMembershipLevel());
        event.put("event", "OWNER_CREATED");
        AUDIT.info(JSON.writeValueAsString(event));
    }
}
