package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Step of {@code POST /api/owners} run after {@link SaveOwner}, once the owner carries its id:
 * associates the request's {@code Idempotency-Key} (published by {@link RecallIdempotentOwner})
 * with that id, so a later repeat of the same key replays this owner instead of creating another.
 * A request without a key publishes {@code null} and records nothing.
 */
public class RecordIdempotentOwner {

    public void service(@Val String idempotencyKey, @Val Owner owner, IdempotencyStore idempotencyStore) {
        idempotencyStore.record(idempotencyKey, owner.getId());
    }
}
