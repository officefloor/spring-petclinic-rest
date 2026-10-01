package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Runs after {@link SaveOwner} on {@code POST /api/owners}, once the new owner has an id. Remembers the
 * {@code Idempotency-Key} published by {@link CheckIdempotency} against that id in the
 * {@link IdempotencyStore}, so a later create replaying the same key returns this owner with a 200
 * instead of creating a duplicate. When no key was supplied the published value is {@code null} and
 * nothing is recorded.
 */
public class RecordIdempotency {

    public void service(@Val Owner owner, @Val String idempotencyKey, IdempotencyStore store) {
        store.record(idempotencyKey, owner.getId());
    }
}
