package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.audit.OwnerCreatedEvent;
import org.springframework.samples.petclinic.rest.audit.OwnerEventSequence;
import org.springframework.samples.petclinic.util.MembershipLevels;
import org.springframework.samples.petclinic.util.MembershipNumbers;

/**
 * Emits the audit trail for a newly created owner. Runs after {@link SaveOwner} has persisted
 * the entity, so the generated id is available. On the dedicated {@code AUDIT} logger it emits
 * both a human-readable line (owner id, assigned customer code, effective registration date,
 * derived membership points, level and number) and an immutable structured
 * {@link OwnerCreatedEvent}, stamped with the next sequence number. Purely a side-effect step;
 * it leaves the owner unchanged for the responder.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner, OwnerEventSequence sequence) {
        LocalDate asOf = LocalDate.now();
        int points = MembershipLevels.pointsOf(owner, asOf);
        AUDIT.info(
                "Owner created: id={} customerCode={} registrationDate={} membershipPoints={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                points, MembershipLevels.levelOf(owner, asOf), MembershipNumbers.of(owner));
        AUDIT.info(OwnerCreatedEvent.of(sequence.next(), owner, asOf).toJson());
    }
}
