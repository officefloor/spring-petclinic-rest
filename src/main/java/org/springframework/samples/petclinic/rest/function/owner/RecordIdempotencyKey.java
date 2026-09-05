package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.HttpHeaderParameter;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Runs after the owner is saved. When the request carried an {@code Idempotency-Key}, remembers that
 * this key created the just-saved owner, so a later create repeating the same key returns this owner
 * (see {@link CheckIdempotencyKey}) rather than creating a duplicate.
 */
public class RecordIdempotencyKey {

    public void service(@HttpHeaderParameter("Idempotency-Key") String idempotencyKey,
            @Val Owner owner, IdempotencyStore store) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            store.record(idempotencyKey, owner.getId());
        }
    }
}
