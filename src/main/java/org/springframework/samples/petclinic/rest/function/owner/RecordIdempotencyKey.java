package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Records the freshly created owner against the request's {@code Idempotency-Key} (when
 * one was supplied), so a later repeat with the same key returns this owner.
 */
public class RecordIdempotencyKey {

    public void service(@Val String key, @Val Owner owner, IdempotencyStore store) {
        if (key != null && !key.isEmpty()) {
            store.record(key, owner.getId());
        }
    }
}
