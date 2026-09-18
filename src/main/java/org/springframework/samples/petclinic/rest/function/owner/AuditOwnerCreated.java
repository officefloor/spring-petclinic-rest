package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.audit.OwnerAuditTrail;

/**
 * Emits the audit trail for a successful owner create in {@code POST /api/owners}: the
 * human-readable audit line and an immutable structured {@code OWNER_CREATED} event, both published
 * to the dedicated {@code AUDIT} logger by {@link OwnerAuditTrail}. Runs after {@link SaveOwner} so
 * the generated id is available, and before the response is sent.
 */
public class AuditOwnerCreated {

    public void service(@Val Owner owner, OwnerAuditTrail auditTrail) {
        auditTrail.ownerCreated(owner);
    }
}
