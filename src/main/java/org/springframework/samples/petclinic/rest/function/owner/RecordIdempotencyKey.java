package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.function.common.IdempotencyKey;
import org.springframework.samples.petclinic.rest.function.common.IdempotencyStore;

/**
 * Remembers the id of the just-saved owner against the request's {@code Idempotency-Key}, so a
 * later create with the same key replays this owner (see {@link CheckIdempotencyKey}) instead of
 * creating a duplicate. Runs after {@link SaveOwner} so the generated id is available. Does nothing
 * when the request carried no key.
 */
public class RecordIdempotencyKey {

    public void service(@Val IdempotencyKey key, @Val Owner owner, IdempotencyStore store) {
        if (key.value() != null) {
            store.record(key.value(), owner.getId());
        }
    }
}
