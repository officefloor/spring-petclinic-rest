package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.idempotency.IdempotencyStore;

/**
 * Runs after {@link SaveOwner} in {@code POST /api/owners}. When the request carried an
 * {@code Idempotency-Key}, pins it to the newly persisted owner's id so a later create with the
 * same key replays this owner instead of creating a duplicate. A request without a key is a
 * no-op.
 */
public class RecordIdempotencyKey {

    public void service(@Val IdempotencyKey idempotencyKey, @Val Owner owner,
            IdempotencyStore idempotencyStore) {
        if (idempotencyKey.isPresent()) {
            idempotencyStore.record(idempotencyKey.value(), owner.getId());
        }
    }
}
