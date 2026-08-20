package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.function.common.IdempotencyStore;

/**
 * After the new owner is saved, pair the request's {@link IdempotencyKey} with the new owner's id in
 * the {@link IdempotencyStore}, so a later create carrying the same key replays this owner. A request
 * without an idempotency key (a {@code null} value) is not recorded.
 */
public class RecordIdempotency {

    public void service(@Val IdempotencyKey key, @Val Owner owner, IdempotencyStore store) {
        store.record(key.value(), owner.getId());
    }
}
