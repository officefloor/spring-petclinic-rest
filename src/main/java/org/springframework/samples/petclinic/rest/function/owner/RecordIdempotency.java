package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.idempotency.IdempotencyStore;

/**
 * Runs after the new owner has been saved (so its id is assigned). When the request carried an
 * {@code Idempotency-Key}, remembers which owner it created so a later repeat with the same key
 * replays that owner instead of creating a duplicate (see {@link CheckIdempotency}).
 */
public class RecordIdempotency {

    public void service(@Val String key, @Val Owner owner, IdempotencyStore idempotencyStore) {
        if (key != null && !key.isBlank()) {
            idempotencyStore.record(key, owner.getId());
        }
    }
}
