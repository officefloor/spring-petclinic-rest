package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Emits audit side-effects to the dedicated {@code AUDIT} logger once an owner has been
 * persisted. Runs after {@link SaveOwner} so the owner id is assigned.
 *
 * <p>Two things are emitted:
 * <ul>
 * <li>a human-readable audit line carrying the id, {@code memberId},
 * {@code registrationDate} and {@code membershipLevel}; and
 * <li>an immutable structured {@link OwnerCreatedEvent}, serialized to JSON, whose
 * {@code seq} increases monotonically across every owner created by this process. The
 * event carries the owner's primary identifier — the unified {@code memberId} (see
 * {@link #primaryIdentifier}/{@link #PRIMARY_IDENTIFIER_KEY}).
 * </ul>
 * The membership level is read from the mapped {@link OwnerDto} so it matches the value
 * the endpoint reports.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private static final ObjectMapper JSON = new ObjectMapper();

    /** Monotonic sequence across every owner created by this process. */
    private static final AtomicLong SEQ = new AtomicLong();

    /** JSON key under which the primary identifier is emitted. */
    private static final String PRIMARY_IDENTIFIER_KEY = "memberId";

    public void service(@Val Owner owner, OwnerMapper ownerMapper) {
        OwnerDto dto = ownerMapper.toOwnerDto(owner);
        AUDIT.info(
                "owner created id={} memberId={} registrationDate={} membershipLevel={}",
                owner.getId(), owner.getMemberId(), owner.getRegistrationDate(),
                dto.getMembershipLevel());

        OwnerCreatedEvent event = new OwnerCreatedEvent(SEQ.incrementAndGet(), owner.getId(),
                primaryIdentifier(owner), dto.getMembershipLevel());
        AUDIT.info(toJson(event));
    }

    /** The owner's primary identifier: the unified {@code memberId}. */
    private static String primaryIdentifier(Owner owner) {
        return owner.getMemberId();
    }

    /** Serialize the event to a JSON object, emitting the identifier under its current key. */
    private static String toJson(OwnerCreatedEvent event) {
        ObjectNode node = JSON.createObjectNode();
        node.put("seq", event.seq());
        node.put("ownerId", event.ownerId());
        node.put(PRIMARY_IDENTIFIER_KEY, event.identifier());
        node.put("membershipLevel", event.membershipLevel());
        node.put("event", event.event());
        return JSON.writeValueAsString(node);
    }
}
