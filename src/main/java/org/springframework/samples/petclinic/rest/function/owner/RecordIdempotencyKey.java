package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.idempotency.IdempotencyStore;

/**
 * Records the just-saved owner against its {@code Idempotency-Key}, so a later create with
 * the same key replays this owner (see {@link CheckIdempotencyKey}) rather than creating a
 * duplicate. A no-op when no key was supplied. Runs after {@link SaveOwner} so the owner id
 * is available; leaves the owner unchanged for the responder.
 */
public class RecordIdempotencyKey {

    public void service(@Val IdempotencyKey key, @Val Owner owner, IdempotencyStore store) {
        if (key.isPresent()) {
            store.record(key.value(), owner.getId());
        }
    }
}
