package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits, via the dedicated {@code AUDIT} logger, both the human-readable audit line — recording the
 * new owner's id, its assigned {@code memberId}, its {@code registrationDate} and its
 * {@code membershipLevel} — and, in addition, an immutable structured
 * {@link OwnerCreatedEvent} serialized as JSON ({@code {seq, ownerId, memberId, membershipLevel,
 * event:'OWNER_CREATED'}}) whose {@code seq} increases monotonically across creates. Runs after
 * {@link SaveOwner} (so the id is populated) and before {@link RespondWithOwnerCreated}.
 *
 * <p>It reads no repository, but stays under the pipeline's {@code transaction} governance
 * so the create commits as a single boundary spanning {@code save} through {@code respond}
 * (the responder maps the entity's lazy relations); an ungoverned step here would split
 * that boundary.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner) {
        AUDIT.info("owner created id={} memberId={} registrationDate={} membershipLevel={}",
                owner.getId(), owner.getMemberId(), owner.getRegistrationDate(),
                owner.getMembershipLevel());
        AUDIT.info(OwnerCreatedEvent.of(owner).toJson());
    }
}
