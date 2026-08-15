package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Step of {@code POST /api/owners} that runs after {@link SaveOwner} — so the owner has its
 * persisted id — and remembers, in the {@link IdempotencyStore}, which owner the request's
 * {@code Idempotency-Key} created. A later create carrying the same key is then replayed by
 * {@link RouteIdempotentCreate} instead of creating a duplicate. Does nothing when no key was
 * supplied (the published key is the empty string).
 */
public class RecordIdempotencyKey {

    public void service(@Val String idempotencyKey, @Val Owner owner, IdempotencyStore store) {
        if (idempotencyKey != null && !idempotencyKey.isEmpty()) {
            store.record(idempotencyKey, owner.getId());
        }
    }
}
