package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Records the {@code Idempotency-Key} of a just-created owner so a later repeat carrying the
 * same key resolves back to this owner (see {@link IdempotencyStore}) instead of creating a
 * duplicate. Runs after {@link SaveOwner} has assigned the id. A request without a key (the key
 * published as blank by {@link CheckIdempotencyKey}) is a no-op.
 */
public class RecordIdempotencyKey {

    public void service(@Val String idempotencyKey, @Val Owner owner, IdempotencyStore idempotencyStore) {
        idempotencyStore.record(idempotencyKey, owner.getId());
    }
}
