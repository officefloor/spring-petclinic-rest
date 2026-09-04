package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;

import tools.jackson.databind.json.JsonMapper;

/**
 * Emits, on the dedicated {@code AUDIT} logger, the record of a newly created owner. Runs after the
 * owner has been saved so its id is available. Two things are emitted:
 *
 * <ol>
 * <li>a human-readable audit line (id, customerCode, registrationDate, membershipLevel,
 * membershipNumber); and</li>
 * <li>an immutable structured {@link OwnerCreatedEvent} serialised to JSON, so audit consumers have a
 * machine-parseable, ordered event to react to.</li>
 * </ol>
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private static final JsonMapper JSON = JsonMapper.builder().build();

    /** Monotonically increasing sequence stamped onto each event, shared across all creates. */
    private static final AtomicLong SEQUENCE = new AtomicLong();

    public void service(@Val Owner owner, OwnerMapper mapper) {
        int membershipLevel = OwnerMembership.level(owner);

        AUDIT.info(
                "Created owner id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                membershipLevel, mapper.membershipNumber(owner));

        OwnerCreatedEvent event = new OwnerCreatedEvent(SEQUENCE.incrementAndGet(), owner.getId(),
                primaryIdentifier(owner), membershipLevel);
        AUDIT.info(JSON.writeValueAsString(event));
    }

    /**
     * The owner's current primary identifier carried by the {@link OwnerCreatedEvent}. Today that is the
     * {@code customerCode}; when the customerCode is unified into the memberId, return the memberId here
     * and the event will carry it instead. This single method is the one place the event's identity
     * source is defined, so that later switch is a one-line change.
     */
    private static String primaryIdentifier(Owner owner) {
        return owner.getCustomerCode();
    }
}
