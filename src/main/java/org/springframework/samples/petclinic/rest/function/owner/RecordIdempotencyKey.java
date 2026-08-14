package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Records the request's {@code Idempotency-Key} (when one was supplied) against the just-created
 * owner's id, so a later repeat with the same key replays this owner instead of creating a duplicate.
 * Runs after {@link SaveOwner} has assigned the id and before the response is sent.
 */
public class RecordIdempotencyKey {

    public void service(@Val IdempotencyKey key, @Val Owner owner, IdempotencyStore store) {
        if (key != null && key.isPresent()) {
            store.record(key.value(), owner.getId());
        }
    }
}
