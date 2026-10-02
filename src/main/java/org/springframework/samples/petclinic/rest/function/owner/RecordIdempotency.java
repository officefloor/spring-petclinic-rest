package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Records the request's {@code Idempotency-Key} against the just-saved owner's id, so a later
 * create repeating the same key is served the original owner by {@link CheckIdempotency}.
 * A no-op when no key was supplied. Runs after {@link SaveOwner} so the owner id is populated.
 */
public class RecordIdempotency {

    public void service(@Val IdempotencyKey key, @Val Owner owner, IdempotencyStore store) {
        if (key.isPresent()) {
            store.record(key.value(), owner.getId());
        }
    }
}
