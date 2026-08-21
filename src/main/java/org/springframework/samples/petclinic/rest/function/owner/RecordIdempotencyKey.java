package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.idempotency.IdempotencyStore;

/**
 * Runs after the owner is saved (so the id is assigned) and only on the create path. When the
 * request carried an {@code Idempotency-Key}, remember which owner it created so a later repeat of
 * that key replays this owner via {@link CheckIdempotencyKey} instead of creating a duplicate.
 */
public class RecordIdempotencyKey {

    public void service(@Val String idempotencyKey, @Val Owner owner, IdempotencyStore idempotencyStore) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            idempotencyStore.record(idempotencyKey, owner.getId());
        }
    }
}
