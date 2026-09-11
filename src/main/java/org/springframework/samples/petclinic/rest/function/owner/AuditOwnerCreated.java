package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.mapper.MembershipLevels;
import org.springframework.samples.petclinic.model.Owner;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Emits an audit record for a freshly created owner via the dedicated {@code AUDIT}
 * logger. Runs after the owner is saved, so its generated id is populated. The line
 * carries the owner id, the assigned memberId, the registrationDate and the assigned
 * membershipLevel.
 *
 * <p>Besides that human-readable line, it emits an immutable structured event as a
 * JSON object {@code {seq, ownerId, memberId, membershipLevel, event}} with
 * {@code event = "OWNER_CREATED"}, where {@code seq} is a process-wide monotonically
 * increasing integer across creates. The event carries the owner's <em>current
 * primary identifier</em> ({@link #primaryIdentifier(Owner)}): the unified memberId.
 */
public class AuditOwnerCreated {

    private static final Logger audit = LoggerFactory.getLogger("AUDIT");

    private static final ObjectMapper JSON = new ObjectMapper();

    /** Process-wide sequence, monotonically increasing across every create. */
    private static final AtomicLong SEQ = new AtomicLong(0);

    public void service(@Val Owner owner) {
        int membershipLevel = MembershipLevels.forOwner(owner);
        audit.info("Owner created id={} memberId={} registrationDate={} membershipLevel={}",
                owner.getId(), owner.getMemberId(), owner.getRegistrationDate(), membershipLevel);

        ObjectNode event = JSON.createObjectNode();
        event.put("seq", SEQ.incrementAndGet());
        event.put("ownerId", owner.getId());
        event.put("memberId", primaryIdentifier(owner));
        event.put("membershipLevel", membershipLevel);
        event.put("event", "OWNER_CREATED");
        audit.info(JSON.writeValueAsString(event));
    }

    /** The owner's current primary identifier: the unified memberId. */
    private static String primaryIdentifier(Owner owner) {
        return owner.getMemberId();
    }
}
