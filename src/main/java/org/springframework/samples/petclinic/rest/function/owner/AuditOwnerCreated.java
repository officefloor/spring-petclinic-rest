package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits, via the dedicated {@code AUDIT} logger, both the human-readable audit line — recording the
 * new owner's id, its assigned {@code customerCode}, its {@code registrationDate}, its
 * {@code membershipLevel} and its {@code membershipNumber} — and, in addition, an immutable structured
 * {@link OwnerCreatedEvent} serialized as JSON ({@code {seq, ownerId, customerCode, membershipLevel,
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
        AUDIT.info("owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                owner.getMembershipLevel(), owner.getMembershipNumber());
        AUDIT.info(OwnerCreatedEvent.of(owner).toJson());
    }
}
