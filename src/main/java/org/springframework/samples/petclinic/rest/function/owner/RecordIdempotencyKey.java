package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Step of {@code POST /api/owners} that runs after {@link SaveOwner} (so the owner's generated id is
 * available) and records the request's {@code Idempotency-Key} against that id. A later create
 * repeating the same key is then recognised by {@link CheckIdempotencyKey} and returns this owner.
 *
 * <p>The key is published by {@link CheckIdempotencyKey}; an empty value means the request carried no
 * key, so nothing is recorded.
 */
public class RecordIdempotencyKey {

    public void service(@Val String idempotencyKey, @Val Owner owner, IdempotencyStore store) {
        if (idempotencyKey != null && !idempotencyKey.isEmpty()) {
            store.record(idempotencyKey, owner.getId());
        }
    }
}
