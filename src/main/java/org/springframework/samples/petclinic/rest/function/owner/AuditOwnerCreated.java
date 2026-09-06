package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Emits, once the owner has been persisted, both a human-readable audit line and an immutable
 * structured {@link OwnerCreatedEvent} to the dedicated {@code AUDIT} logger. The line records the
 * assigned id, the {@code memberId}, the {@code registrationDate}, the {@code membershipPoints} and
 * the {@code membershipLevel}; the event carries the sequence, the owner id, the primary identifier
 * (the {@code memberId}) and the membership level as JSON.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int points = Memberships.membershipPoints(owner, Households.size(owner, ownerRepository));
        int level = Memberships.cappedLevel(owner, ownerRepository);
        AUDIT.info("owner created: id={} memberId={} registrationDate={} membershipPoints={} "
                + "membershipLevel={}",
                owner.getId(), owner.getMemberId(), owner.getRegistrationDate(), points, level);
        AUDIT.info("{}", OwnerCreatedEvent.next(owner, level).toJson());
    }
}
