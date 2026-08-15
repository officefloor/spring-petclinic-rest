package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.function.common.IdempotencyStore;

/**
 * Records the create request's {@code Idempotency-Key} against the just-created owner, so a later
 * repeat of the same key replays this owner (see {@link CheckIdempotencyKey}). Runs after the insert
 * so the owner id is assigned; a no-op when the request carried no key.
 */
public class RecordIdempotencyKey {

    public void service(@Val IdempotencyKey key, @Val Owner owner, IdempotencyStore store) {
        store.record(key.value(), owner.getId());
    }
}
