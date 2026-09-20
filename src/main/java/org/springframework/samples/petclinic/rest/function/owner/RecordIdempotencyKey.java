package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Registers the just-saved owner under the request's {@code Idempotency-Key}, so a later
 * create repeating that key replays this owner (see {@link CheckIdempotencyKey}). Runs after
 * {@link SaveOwner} so the generated id is available; a no-op when the request carried no key.
 */
public class RecordIdempotencyKey {

    public void service(@Val IdempotencyKey key, @Val Owner owner, IdempotencyStore store) {
        if (key != null && key.isPresent()) {
            store.record(key.value(), owner.getId());
        }
    }
}
