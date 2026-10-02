package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * After a new owner is saved, records the request's {@code Idempotency-Key} (published by
 * {@link CheckIdempotency}) against the owner's assigned id, so a later create with the same key replays
 * this owner. A no-op when the request carried no key.
 */
public class RecordIdempotency {

    public void service(@Val IdempotencyKey key, @Val Owner owner, IdempotencyStore store) {
        if (key != null && key.value() != null) {
            store.record(key.value(), owner.getId());
        }
    }
}
