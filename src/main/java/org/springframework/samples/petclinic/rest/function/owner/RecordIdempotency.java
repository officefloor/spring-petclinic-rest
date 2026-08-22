package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Remembers the {@code Idempotency-Key} (published by {@link CheckIdempotency}) against the id of
 * the owner just saved, so a later repeat with the same key replays this owner instead of creating
 * a duplicate. A no-op when the request carried no key.
 */
public class RecordIdempotency {

    public void service(@Val String idempotencyKey, @Val Owner owner, IdempotencyStore store) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            store.record(idempotencyKey, owner.getId());
        }
    }
}
