package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Records that the supplied {@code Idempotency-Key} created this owner, so a later repeat of the
 * create with the same key replays this owner (see {@link CheckIdempotencyKey}) instead of creating
 * a duplicate. A no-op when the request supplied no key.
 */
public class RecordIdempotencyKey {

    public void service(@Val IdempotencyKey key, @Val Owner owner, IdempotencyStore store) {
        if (key.isPresent()) {
            store.record(key.value(), owner.getId());
        }
    }
}
