package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Records the freshly saved {@link Owner} under the request's {@code Idempotency-Key} (published by
 * {@link CheckIdempotencyKey}), so a later create repeating that key replays this owner instead of
 * creating a duplicate. A no-op when the request carried no key.
 *
 * <p>Runs after {@link SaveOwner} — so the owner's generated id is available — and before the
 * responder.
 */
public class RecordIdempotencyKey {

    public void service(@Val IdempotencyKey key, @Val Owner owner, IdempotencyStore store) {
        if (key != null && key.isPresent()) {
            store.record(key.value(), owner.getId());
        }
    }
}
