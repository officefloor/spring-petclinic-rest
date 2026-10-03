package org.springframework.samples.petclinic.rest.function.owner;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import tools.jackson.databind.ObjectMapper;

/**
 * Emits audit records for a freshly created owner via the dedicated {@code AUDIT}
 * logger. Runs after {@link SaveOwner}, so the owner id has been assigned.
 *
 * <p>Two things are emitted:
 * <ol>
 * <li>a human-readable audit line carrying the id, the customerCode, the
 * registrationDate, the membershipLevel and the membershipNumber; and</li>
 * <li>an immutable structured {@link OwnerCreatedEvent}, serialised to JSON with the
 * shared Jackson {@link ObjectMapper}, carrying a monotonically increasing
 * {@code seq}, the {@code ownerId}, the owner's current primary identifier and the
 * {@code membershipLevel}.</li>
 * </ol>
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Monotonically increasing sequence across all creates (process-wide). */
    private static final AtomicLong SEQ = new AtomicLong();

    public void service(@Val Owner owner, ObjectMapper objectMapper) {
        AUDIT.info("Owner created: id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                owner.getMembershipLevel(), owner.getMembershipNumber());

        // Immutable structured event. The identifier slot carries the owner's current
        // primary identifier, so it follows the eventual customerCode -> memberId switch.
        OwnerCreatedEvent event = new OwnerCreatedEvent(SEQ.incrementAndGet(), owner.getId(),
                owner.getPrimaryIdentifier(), owner.getMembershipLevel());
        AUDIT.info(objectMapper.writeValueAsString(toFields(event)));
    }

    /** Ordered field map matching the event's JSON shape. */
    private static Map<String, Object> toFields(OwnerCreatedEvent event) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("seq", event.seq());
        fields.put("ownerId", event.ownerId());
        fields.put("customerCode", event.customerCode());
        fields.put("membershipLevel", event.membershipLevel());
        fields.put("event", event.event());
        return fields;
    }
}
