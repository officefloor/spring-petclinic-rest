package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits, on successful create, both a human-readable audit line and an immutable structured
 * {@link OwnerCreatedEvent} — recording the newly-persisted owner's id, {@code customerCode},
 * {@code registrationDate}, {@code membershipLevel} and {@code membershipNumber} — to the dedicated
 * {@code AUDIT} logger. Runs after {@link SaveOwner} (so the owner has its generated id) and before
 * {@link RespondWithOwnerCreated}.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Monotonically increasing sequence across all creates, stamped onto each emitted event. */
    private static final AtomicLong SEQ = new AtomicLong();

    public void service(@Val Owner owner, OwnerMapper ownerMapper) {
        Integer membershipLevel = ownerMapper.toOwnerDto(owner).getMembershipLevel();
        AUDIT.info("owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                membershipLevel, owner.getMembershipNumber());

        OwnerCreatedEvent event = new OwnerCreatedEvent(SEQ.incrementAndGet(), owner.getId(),
                primaryIdentifier(owner), membershipLevel);
        AUDIT.info(event.toJson());
    }

    /**
     * The owner's current primary identifier carried by the event. Today the {@code customerCode};
     * once the customerCode is unified into the memberId, return the memberId here so the event
     * carries it instead — this is the one place that resolution changes.
     */
    private static String primaryIdentifier(Owner owner) {
        return owner.getCustomerCode();
    }
}
