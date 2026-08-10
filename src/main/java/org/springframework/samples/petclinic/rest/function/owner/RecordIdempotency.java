package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import net.officefloor.server.http.ServerHttpConnection;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Records the {@code Idempotency-Key} (when the request carried one) against the just-created
 * owner's id, so a later repeat of the same key returns this owner instead of creating a duplicate
 * (see {@link CheckIdempotency}).
 *
 * <p>Runs after {@link SaveOwner}, so the owner already has its generated id, and only on the create
 * path — a repeat is short-circuited before it reaches here.
 */
public class RecordIdempotency {

    public void service(ServerHttpConnection connection, @Val Owner owner,
            IdempotencyStore idempotencyStore) {
        String key = CheckIdempotency.idempotencyKey(connection);
        if (key != null) {
            idempotencyStore.record(key, owner.getId());
        }
    }
}
