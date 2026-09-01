package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * After a successful create, remember the owner id under the request's {@code Idempotency-Key}
 * (if any), so a later repeat with the same key returns this owner instead of creating a duplicate.
 */
public class RecordIdempotency {

    public void service(@Val String idempotencyKey, @Val Owner owner, IdempotencyStore store) {
        if (idempotencyKey != null) {
            store.record(idempotencyKey, owner.getId());
        }
    }
}
