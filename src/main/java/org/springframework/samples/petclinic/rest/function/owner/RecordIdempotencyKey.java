package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import net.officefloor.server.http.ServerHttpConnection;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.service.IdempotencyStore;

/**
 * Runs after {@link SaveOwner} (so the owner id is assigned) and remembers the created
 * owner against the request's {@code Idempotency-Key}, so a later repeat with the same key
 * replays this owner. A request without the header records nothing.
 */
public class RecordIdempotencyKey {

    public void service(ServerHttpConnection connection, @Val Owner owner, IdempotencyStore store) {
        store.record(IdempotencyStore.keyFrom(connection), owner.getId());
    }
}
