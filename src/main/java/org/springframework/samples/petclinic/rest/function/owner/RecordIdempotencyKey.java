package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.support.IdempotencyStore;

/**
 * Records the just-created owner against the request's {@code Idempotency-Key} (published by
 * {@link CheckIdempotencyKey}) so a later repeat with the same key replays this owner instead
 * of creating a duplicate. Runs after {@link SaveOwner} — the id is assigned by then — and is
 * a no-op when the request carried no key.
 */
public class RecordIdempotencyKey {

    public void service(@Val String idempotencyKey, @Val Owner owner, IdempotencyStore idempotencyStore) {
        idempotencyStore.record(idempotencyKey, owner.getId());
    }
}
