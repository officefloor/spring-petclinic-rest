package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Records the just-created owner against the request's {@code Idempotency-Key}, so a later
 * repeat carrying the same key returns this owner (see {@link CheckIdempotentCreate})
 * rather than creating a duplicate. Runs after {@link SaveOwner} so the owner has an id.
 * A request without a key is a no-op.
 */
public class RecordIdempotentCreate {

    public void service(@Val IdempotencyKey idempotencyKey, @Val Owner owner,
            IdempotencyStore idempotencyStore) {
        String key = idempotencyKey.value();
        if (key != null && !key.isBlank()) {
            idempotencyStore.record(key, owner.getId());
        }
    }
}
