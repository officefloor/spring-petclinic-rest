package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Records the {@code Idempotency-Key} (published by {@link CheckIdempotencyKey}) against the
 * just-created owner, so a later repeat carrying the same key replays this owner. Runs after
 * {@link SaveOwner} so the generated id is available; a request without a key is a no-op.
 */
public class RecordIdempotency {

    public void service(@Val String idempotencyKey, @Val Owner owner, IdempotencyStore store) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            store.record(idempotencyKey, owner.getId());
        }
    }
}
