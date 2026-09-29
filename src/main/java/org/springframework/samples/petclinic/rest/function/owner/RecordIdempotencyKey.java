package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.HttpHeaderParameter;

import org.springframework.samples.petclinic.model.Owner;

/**
 * On a successful create, remembers the {@code Idempotency-Key} (when supplied) against the id of the
 * owner just saved, so a later repeat with the same key returns this owner instead of creating a
 * duplicate. Wired after {@link SaveOwner} so the owner's id is assigned.
 */
public class RecordIdempotencyKey {

    public void service(@HttpHeaderParameter("Idempotency-Key") String idempotencyKey, @Val Owner owner,
            IdempotencyStore idempotencyStore) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            idempotencyStore.record(idempotencyKey, owner.getId());
        }
    }
}
