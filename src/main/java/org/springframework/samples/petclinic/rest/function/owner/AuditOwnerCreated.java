package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits, to the dedicated {@code AUDIT} logger, two records for a successfully created owner:
 * the human-readable audit line (owner id, {@code customerCode}, {@code registrationDate},
 * {@code membershipLevel}, {@code membershipNumber}) and an immutable structured
 * {@link OwnerCreatedEvent} serialized as JSON. Runs after {@link SaveOwner} (so the generated
 * id exists) and before {@link RespondWithOwnerCreated} responds.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner, OwnerCreatedEventSequence sequence) {
        AUDIT.info("Owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                owner.getMembershipLevel(), owner.getMembershipNumber());
        OwnerCreatedEvent event = new OwnerCreatedEvent(sequence.next(), owner.getId(),
                owner.getPrimaryIdentifier(), owner.getMembershipLevel());
        AUDIT.info(event.toJson());
    }
}
